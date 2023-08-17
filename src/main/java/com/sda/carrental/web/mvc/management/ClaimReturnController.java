package com.sda.carrental.web.mvc.management;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.ConstantValues;
import com.sda.carrental.global.enums.Country;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.property.PaymentDetails;
import com.sda.carrental.service.*;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.ConfirmClaimForm;
import com.sda.carrental.web.mvc.form.SearchCarForm;
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
import java.util.ArrayList;

@Controller
@RequiredArgsConstructor
@RequestMapping("/c-ret")
public class ClaimReturnController {

    private final ConstantValues cv;
    private final ReservationService reservationService;
    private final RentingService rentingService;
    private final ReturningService returningService;
    private final PaymentDetailsService paymentDetailsService;
    private final DepartmentService departmentService;

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String showClaimReturnPage(ModelMap map, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            map.addAttribute("countries", Country.values());
            map.addAttribute("searchCarForm", map.getOrDefault("searchCarForm", new SearchCarForm()));

            if (map.containsKey("reservation")) {
                Reservation reservation = (Reservation) map.get("reservation");

                map.addAttribute("confirm_claim_form", map.getOrDefault("confirm_claim_form", new ConfirmClaimForm(reservation.getDepartmentBack().getId(), LocalDate.now())));
                map.addAttribute("departments", departmentService.getDepartmentsByRole(cud));

                PaymentDetails receipt = paymentDetailsService.getOptionalPaymentDetails(reservation).orElseThrow(ResourceNotFoundException::new);

                map.addAttribute("diff_return_price", receipt.getRequiredReturnValue());
                map.addAttribute("raw_price", receipt.getRequiredRawValue());
                map.addAttribute("total_price", receipt.getRequiredRawValue() + receipt.getRequiredReturnValue());
                map.addAttribute("deposit_value", receipt.getRequiredDeposit());

                map.addAttribute("reservation", reservation);
                map.addAttribute("fee_percentage", cv.getCancellationFeePercentage() * 100);
                map.addAttribute("refund_fee_days", cv.getRefundSubtractDaysDuration());
            }

            return "management/claimReturn";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "Failed: Missing payment - possible server side issues");
            return "redirect:/c-ret";
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again.");
            return "redirect:/";
        }
    }

    //Claim return buttons
    @RequestMapping(value = "/check", method = RequestMethod.POST)
    public String checkReservationButton(final ModelMap map, @ModelAttribute @Valid SearchCarForm form, Errors errors, RedirectAttributes redAtt) {
        try {
            redAtt.addFlashAttribute("searchCarForm", form);

            if (errors.hasErrors()) {
                map.addAttribute("countries", Country.values());
                return "management/claimReturn";
            }

            Reservation reservation = reservationService.findActiveReservationByPlate(form.getCountry().getCode() + "-" + form.getPlate());
            redAtt.addFlashAttribute("reservation", reservation);
            redAtt.addFlashAttribute("rent_details", rentingService.findById(reservation.getId()));
            return "redirect:/c-ret";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "Failed: No active rent found for: " + form.getCountry().getCode() + '-' + form.getPlate());
            return "redirect:/c-ret";
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again.");
            return "redirect:/";
        }
    }

    @RequestMapping(value = "/claim", method = RequestMethod.POST)
    public String claimReservationButton(@ModelAttribute @Valid ConfirmClaimForm form, Errors err, RedirectAttributes redAtt,
                                         @RequestParam("plate") String plate, @RequestParam("department") Long departmentId,
                                         @RequestParam("reservation_id") Long reservationId, @RequestParam("customer") Long customerId) {
        if (err.hasErrors()) {
            Reservation reservation = reservationService.findActiveReservationByPlate(plate);
            redAtt.addFlashAttribute("reservation", reservation);
            redAtt.addFlashAttribute("rent_details", rentingService.findById(reservation.getId()));
            String[] plateValues = plate.split("-", 2);
            redAtt.addFlashAttribute("searchCarForm", new SearchCarForm(Country.valueOf("COUNTRY_" + plateValues[0]), plateValues[1]));
            redAtt.addFlashAttribute("message", err.getAllErrors().get(0).getDefaultMessage());
            redAtt.addFlashAttribute("confirm_claim_form", form);
            return "redirect:/c-ret";
        }

        HttpStatus response = returningService.handleReturn(customerId, reservationId, departmentId, form);

        redAtt.addAttribute("reservation", reservationId);
        redAtt.addFlashAttribute("customer", customerId);
        redAtt.addFlashAttribute("department", form.getDepartmentId());

        if (response.equals(HttpStatus.ACCEPTED)) {
            redAtt.addFlashAttribute("message", "Success: Return claimed");
        } else if (response.equals(HttpStatus.NOT_FOUND)) {
            redAtt.addFlashAttribute("message", "Failure: Value mismatch");
        } else if (response.equals(HttpStatus.BAD_REQUEST)) {
            redAtt.addFlashAttribute("message", "Failure: Not allowed action");
        } else {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again later.");
        }
        return "redirect:/mg-res/reservation/{reservation}";
    }
}
