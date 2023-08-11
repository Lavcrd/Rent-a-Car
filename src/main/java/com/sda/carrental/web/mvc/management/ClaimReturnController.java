package com.sda.carrental.web.mvc.management;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.ConstantValues;
import com.sda.carrental.global.enums.Country;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.property.PaymentDetails;
import com.sda.carrental.service.*;
import com.sda.carrental.web.mvc.form.ConfirmReturnForm;
import com.sda.carrental.web.mvc.form.ReclaimCarForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.time.LocalDate;

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
            map.addAttribute("countries", Country.values());

            if (map.containsKey("reclaimCarForm")) {
                if (map.containsKey("reservation")) {
                    ConfirmReturnForm form = new ConfirmReturnForm();
                    form.setDateTo(LocalDate.now());
                    map.addAttribute("return_confirmation_form", form);

                    Reservation reservation = (Reservation) map.get("reservation");
                    PaymentDetails receipt = paymentDetailsService.getOptionalPaymentDetails(reservation).orElseThrow(RuntimeException::new);

                    map.addAttribute("diff_return_price", receipt.getRequiredReturnValue());
                    map.addAttribute("raw_price", receipt.getRequiredRawValue());
                    map.addAttribute("total_price", receipt.getRequiredRawValue() + receipt.getRequiredReturnValue());
                    map.addAttribute("deposit_value", receipt.getRequiredDeposit());

                    map.addAttribute("reservation", reservation);
                    map.addAttribute("fee_percentage", cv.getCancellationFeePercentage() * 100);
                    map.addAttribute("refund_fee_days", cv.getRefundSubtractDaysDuration());
                }
            } else {
                map.addAttribute("reclaimCarForm", new ReclaimCarForm());
            }
            return "management/claimReturn";
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again.");
            return "redirect:/";
        }
    }

    //Claim return buttons
    @RequestMapping(value = "/check", method = RequestMethod.POST)
    public String checkReservationButton(final ModelMap map, @ModelAttribute @Valid ReclaimCarForm form, Errors errors, RedirectAttributes redAtt) {
        try {
            redAtt.addFlashAttribute("reclaimCarForm", form);

            if (errors.hasErrors()) {
                map.addAttribute("countries", Country.values());
                return "management/claimReturn";
            }

            Reservation reservation = reservationService.findActiveReservationByPlate(form.getCountry().getCode() + "-" + form.getPlate());
            redAtt.addFlashAttribute("reservation", reservation);
            redAtt.addFlashAttribute("rent_details", rentingService.findById(reservation.getId()));
            return "redirect:/c-ret";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "No active rent found for: " + form.getCountry().getCode() + '-' + form.getPlate());
            return "redirect:/c-ret";
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again.");
            return "redirect:/";
        }
    }
}
