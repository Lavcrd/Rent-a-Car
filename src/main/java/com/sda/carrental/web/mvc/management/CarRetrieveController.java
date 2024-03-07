package com.sda.carrental.web.mvc.management;

import com.sda.carrental.exceptions.IllegalActionException;
import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.CompanySettings;
import com.sda.carrental.model.operational.Rent;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.property.payments.PaymentDetails;
import com.sda.carrental.service.*;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.operational.ConfirmClaimForm;
import com.sda.carrental.web.mvc.form.property.cars.SearchCarForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@RequestMapping("/c-ret")
public class CarRetrieveController {

    private final CompanySettings cs;
    private final RentService rentService;
    private final RetrieveService retrieveService;
    private final PaymentDetailsService paymentDetailsService;
    private final EmployeeService employeeService;
    private final CountryService countryService;

    private final String MSG_KEY = "message";
    private final String MSG_GENERIC_EXCEPTION = "Failure: An unexpected error occurred";
    private final String MSG_NO_RESOURCE = "Failure: Resource not found";

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String showRetrievePage(ModelMap map, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            map.addAttribute("countries", countryService.findAll());
            map.addAttribute("searchCarForm", map.getOrDefault("searchCarForm", new SearchCarForm()));

            if (map.containsKey("reservation")) {
                Reservation reservation = (Reservation) map.get("reservation");

                map.addAttribute("confirm_claim_form", map.getOrDefault("confirm_claim_form", new ConfirmClaimForm(reservation.getId(), reservation.getDepartmentBack().getId(), LocalDate.now())));
                map.addAttribute("departments", employeeService.getDepartmentsByUserContext(cud));

                PaymentDetails receipt = paymentDetailsService.getOptionalPaymentDetails(reservation.getId()).orElseThrow(ResourceNotFoundException::new);

                map.addAttribute("diff_return_price", receipt.getInitialDivergenceFee());
                map.addAttribute("raw_price", receipt.getInitialCarFee());
                map.addAttribute("total_price", receipt.getInitialCarFee() + receipt.getInitialDivergenceFee());
                map.addAttribute("deposit_value", receipt.getInitialDeposit());

                map.addAttribute("reservation", reservation);
                map.addAttribute("fee_percentage", cs.getCancellationFeePercentage() * 100);
                map.addAttribute("refund_fee_days", cs.getRefundSubtractDaysDuration());
            }
            return "management/carRetrieve";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
            return "redirect:/c-ret";
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/";
        }
    }

    //Retrieve page buttons
    @RequestMapping(value = "/check", method = RequestMethod.POST)
    public String checkReservationButton(final ModelMap map, @ModelAttribute @Valid SearchCarForm form, Errors errors, RedirectAttributes redAtt) {
        try {
            redAtt.addFlashAttribute("searchCarForm", form);

            if (errors.hasErrors()) {
                map.addAttribute("countries", countryService.findAll());
                return "management/carRetrieve";
            }

            Rent rent = rentService.findActiveOperationByCarPlate(form.getCountry() + "-" + form.getPlate());
            redAtt.addFlashAttribute("rent_details", rent);
            redAtt.addFlashAttribute("reservation", rent.getReservation());
            return "redirect:/c-ret";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute(MSG_KEY, "Failed: No active rent found for: " + form.getCountry() + '-' + form.getPlate());
            return "redirect:/c-ret";
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/";
        }
    }

    @RequestMapping(value = "/claim", method = RequestMethod.POST)
    public String claimReservationButton(@ModelAttribute @Valid ConfirmClaimForm form, Errors err, RedirectAttributes redAtt,
                                         @RequestParam("plate") String plate, @RequestParam("department") Long departmentId,
                                         @RequestParam("customer") Long customerId) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Rent r = rentService.findActiveOperationByCarPlate(plate);
            if (!form.getReservationId().equals(r.getId()) ||
                    !form.getDepartmentId().equals(departmentId) ||
                    employeeService.departmentAccess(cud, departmentId).equals(HttpStatus.FORBIDDEN)) throw new IllegalActionException();

            if (err.hasErrors()) {
                redAtt.addFlashAttribute("rent_details", r);
                redAtt.addFlashAttribute("reservation", r.getReservation());
                String[] plateValues = plate.split("-", 2);
                redAtt.addFlashAttribute("searchCarForm", new SearchCarForm(plateValues[0], plateValues[1]));
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                redAtt.addFlashAttribute("confirm_claim_form", form);
                return "redirect:/c-ret";
            }

            HttpStatus response = retrieveService.handleRetrieve(customerId, departmentId, form);

            redAtt.addAttribute("reservation", form.getReservationId());
            redAtt.addAttribute("customer", customerId);
            redAtt.addAttribute("department", form.getDepartmentId());

            if (response.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Success: Return claimed");
            } else if (response.equals(HttpStatus.NOT_FOUND)) {
                redAtt.addFlashAttribute(MSG_KEY, "Failure: Value mismatch");
            } else if (response.equals(HttpStatus.BAD_REQUEST)) {
                redAtt.addFlashAttribute(MSG_KEY, "Failure: Action not allowed");
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }
            return "redirect:/mg-res/{department}-{customer}/{reservation}";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
            return "redirect:/c-ret";
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/c-ret";
        }
    }
}
