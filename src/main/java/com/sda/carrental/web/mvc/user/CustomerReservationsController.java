package com.sda.carrental.web.mvc.user;

import com.sda.carrental.global.ConstantValues;
import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.property.PaymentDetails;
import com.sda.carrental.service.PaymentDetailsService;
import com.sda.carrental.service.ReservationService;
import com.sda.carrental.service.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reservations")
public class CustomerReservationsController {
    private final ReservationService reservationService;
    private final PaymentDetailsService paymentDetailsService;
    private final ConstantValues cv;

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String reservationsPage(ModelMap map) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        map.addAttribute("reservations", reservationService.findCustomerReservations(cud.getId()));
        return "user/reservationsCustomer";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{details_button}")
    public String reservationDetailsPage(final ModelMap map, RedirectAttributes redAtt, @PathVariable(value = "details_button") Long detailsButton) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Reservation reservation = reservationService.findCustomerReservation(cud.getId(), detailsButton);
            Optional<PaymentDetails> receipt = paymentDetailsService.getOptionalPaymentDetails(reservation.getId());

            if (receipt.isPresent()) {
                map.addAttribute("diff_return_price", receipt.get().getInitialDivergenceFee());
                map.addAttribute("raw_price", receipt.get().getInitialCarFee());
                map.addAttribute("total_price", receipt.get().getInitialCarFee() + receipt.get().getInitialDivergenceFee());
                map.addAttribute("deposit_value", receipt.get().getInitialDeposit());
            } else {
                long days = reservation.getDateFrom().until(reservation.getDateTo(), ChronoUnit.DAYS) + 1;
                if (!reservation.getDepartmentTake().equals(reservation.getDepartmentBack())) {
                    map.addAttribute("diff_return_price", cv.getDeptReturnPriceDiff());
                    map.addAttribute("total_price", cv.getDeptReturnPriceDiff() + (days * reservation.getCar().getPriceDay()));
                } else {
                    map.addAttribute("diff_return_price", 0.0);
                    map.addAttribute("total_price", days * reservation.getCar().getPriceDay());
                }
                map.addAttribute("raw_price", days * reservation.getCar().getPriceDay());
                map.addAttribute("deposit_value", reservation.getCar().getDepositValue());
            }

            map.addAttribute("reservation", reservation);
            map.addAttribute("fee_percentage", cv.getCancellationFeePercentage() * 100);
            map.addAttribute("refund_fee_days", cv.getRefundSubtractDaysDuration());
            map.addAttribute("deposit_deadline", cv.getRefundDepositDeadlineDays());
            return "user/reservationDetailsCustomer";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again later or contact customer service.");
            return "redirect:/reservations";
        }
    }

    //Reservations page buttons
    @RequestMapping(method = RequestMethod.POST)
    public String reservationsDetailButton(RedirectAttributes redAtt, @RequestParam("details_button") Long reservationId) {
        redAtt.addAttribute("details_button", reservationId);
        return "redirect:/reservations/{details_button}";
    }

    //Reservation details page buttons
    @RequestMapping(method = RequestMethod.POST, value = "/refund")
    public String reservationRefundButton(RedirectAttributes redAtt, @RequestParam(value = "id") Long reservationId) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        HttpStatus response = reservationService.handleReservationStatus(cud.getId(), reservationId, Reservation.ReservationStatus.STATUS_REFUNDED);

        if (response.equals(HttpStatus.ACCEPTED)) {
            redAtt.addFlashAttribute("message", "Refund completed successfully!");
            return "redirect:/reservations";
        }
        redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again later.");
        return "redirect:/";
    }
}
