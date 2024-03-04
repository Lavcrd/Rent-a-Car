package com.sda.carrental.web.mvc.user;

import com.sda.carrental.global.ConstantValues;
import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.property.department.Country;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.property.department.Department;
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

    private final String MSG_KEY = "message";
    private final String MSG_CUSTOMER_GENERIC_EXCEPTION = "An unexpected error occurred. Please try again later or contact customer service.";

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
            Department department = reservation.getDepartmentTake();
            Country country = department.getCountry();
            Optional<PaymentDetails> receipt = paymentDetailsService.getOptionalPaymentDetails(reservation.getId());

            if (receipt.isPresent()) {
                map.addAttribute("diff_return_price", receipt.get().getInitialDivergenceFee());
                map.addAttribute("raw_price", receipt.get().getInitialCarFee());
                map.addAttribute("total_price", receipt.get().getInitialCarFee() + receipt.get().getInitialDivergenceFee());
                map.addAttribute("deposit_value", receipt.get().getInitialDeposit());
            } else {
                long days = reservation.getDateFrom().until(reservation.getDateTo(), ChronoUnit.DAYS) + 1;
                double multiplier = country.getExchange() * department.getMultiplier();

                if (!reservation.getDepartmentTake().equals(reservation.getDepartmentBack())) {
                    map.addAttribute("diff_return_price", cv.getDeptReturnPriceDiff() * multiplier);
                    map.addAttribute("total_price", (cv.getDeptReturnPriceDiff() + (days * reservation.getCarBase().getPriceDay())) * multiplier);
                } else {
                    map.addAttribute("diff_return_price", 0.0);
                    map.addAttribute("total_price", days * reservation.getCarBase().getPriceDay() * multiplier);
                }
                map.addAttribute("raw_price", days * reservation.getCarBase().getPriceDay() * multiplier);
                map.addAttribute("deposit_value", reservation.getCarBase().getDepositValue() * country.getExchange());
            }

            map.addAttribute("reservation", reservation);
            map.addAttribute("fee_percentage", cv.getCancellationFeePercentage() * 100);
            map.addAttribute("refund_fee_days", cv.getRefundSubtractDaysDuration());
            map.addAttribute("deposit_deadline", cv.getRefundDepositDeadlineDays());
            return "user/reservationDetailsCustomer";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_CUSTOMER_GENERIC_EXCEPTION);
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
            redAtt.addFlashAttribute(MSG_KEY, "Refund completed successfully!");
            return "redirect:/reservations";
        }
        redAtt.addFlashAttribute(MSG_KEY, MSG_CUSTOMER_GENERIC_EXCEPTION);
        return "redirect:/";
    }
}
