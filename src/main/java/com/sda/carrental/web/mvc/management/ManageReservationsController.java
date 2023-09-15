package com.sda.carrental.web.mvc.management;

import com.sda.carrental.global.ConstantValues;
import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.enums.Country;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.property.Car;
import com.sda.carrental.model.property.PaymentDetails;
import com.sda.carrental.model.users.Customer;
import com.sda.carrental.model.users.auth.Verification;
import com.sda.carrental.service.*;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mg-res")
public class ManageReservationsController {

    private final CustomerService customerService;
    private final DepartmentService departmentService;
    private final ReservationService reservationService;
    private final PaymentDetailsService paymentDetailsService;
    private final VerificationService verificationService;
    private final CarService carService;
    private final RentService rentService;
    private final RetrieveService retrieveService;
    private final UserService userService;
    private final ConstantValues cv;

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String redirectPage(ModelMap map, RedirectAttributes redAtt) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return "redirect:/mg-cus";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{customer}")
    public String customerReservationsPage(final ModelMap map, RedirectAttributes redAtt, @PathVariable(value = "customer") Long customerId, @ModelAttribute("department") Long departmentId) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (userService.hasNoAccessToUserData(cud, customerId, departmentId)) {
                redAtt.addFlashAttribute("message", "Access rejected.");
                return "redirect:/mg-res";
            }

            Customer customer = customerService.findById(customerId);
            map.addAttribute("department", departmentService.findDepartmentWhereId(departmentId));
            map.addAttribute("customer", customer);
            map.addAttribute("reservations", reservationService.findUserReservationsByDepartmentTake(customer.getId(), departmentId));
            map.addAttribute("reservations_incoming", reservationService.findUserReservationsByDepartmentBack(customer.getId(), departmentId));

            Optional<Verification> verification = verificationService.getOptionalVerificationByCustomer(customerId);
            if (verification.isPresent()) {
                map.addAttribute("verification", verificationService.maskVerification(verification.get()));
            } else {
                map.addAttribute("verification", new Verification(customerId, Country.COUNTRY_NONE, "N/D", "N/D"));
            }
            return "management/viewCustomerReservations";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again.");
            return "redirect:/mg-res";
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/reservation/{reservation}")
    public String reservationDetailsPage(final ModelMap map, RedirectAttributes redAtt, @PathVariable(value = "reservation") Long reservationId, @ModelAttribute("customer") Long customerId, @ModelAttribute("department") Long departmentId) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (userService.hasNoAccessToUserReservation(cud, customerId, reservationId)) {
                redAtt.addFlashAttribute("message", "Access rejected.");
                return "redirect:/mg-res";
            }

            Reservation reservation = reservationService.findCustomerReservation(customerId, reservationId);

            if (departmentService.departmentAccess(cud, reservation.getDepartmentTake().getId()).equals(HttpStatus.FORBIDDEN)) {
                redAtt.addFlashAttribute("message", "Incorrect data. Access not allowed.");
                return "redirect:/mg-res";
            }
            Optional<PaymentDetails> receipt = paymentDetailsService.getOptionalPaymentDetails(reservation);

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

            map.addAttribute("confirmation_form", new ConfirmationForm());
            if (reservation.getStatus().equals(Reservation.ReservationStatus.STATUS_RESERVED)) {
                map.addAttribute("rental_confirmation_form", new ConfirmRentalForm(LocalDate.now()));
            } else if (reservation.getStatus().equals(Reservation.ReservationStatus.STATUS_PROGRESS)) {
                map.addAttribute("rent_details", rentService.findById(reservation.getId()));
                if (departmentId.equals(reservation.getDepartmentBack().getId())) {
                    map.addAttribute("retrieve_confirmation_form", new ConfirmClaimForm(departmentId, LocalDate.now()));
                }
            } else if (reservation.getStatus().equals(Reservation.ReservationStatus.STATUS_COMPLETED)) {
                map.addAttribute("rent_details", rentService.findById(reservation.getId()));
                map.addAttribute("retrieve_details", retrieveService.findById(reservation.getId()));
            }
            return "management/reservationDetailsManagement";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again.");
            return "redirect:/mg-res";
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/reservation/{reservation}/car")
    public String substituteCarPage(final ModelMap map, RedirectAttributes redAtt, @PathVariable(value = "reservation") Long reservationId, @ModelAttribute("customer") Long customerId, @ModelAttribute("department") Long departmentId) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (userService.hasNoAccessToUserReservation(cud, customerId, reservationId)) {
                redAtt.addFlashAttribute("message", "Access rejected.");
                return "redirect:/mg-res";
            }

            Reservation reservation = reservationService.findCustomerReservation(customerId, reservationId);
            List<Car> carList = carService.findAvailableCarsInDepartment(reservation);
            if (carList.isEmpty()) throw new RuntimeException();

            map.addAttribute("cars", map.getOrDefault("filteredCars", carList));

            Map<String, Object> carProperties = carService.getFilterProperties(carList);

            map.addAttribute("brands", carProperties.get("brands"));
            map.addAttribute("types", carProperties.get("types"));
            map.addAttribute("seats", carProperties.get("seats"));

            map.addAttribute("days", (reservation.getDateFrom().until(reservation.getDateTo(), ChronoUnit.DAYS) + 1));

            map.addAttribute("confirmation_form", new ConfirmationForm());
            map.addAttribute("carFilterForm", map.getOrDefault("carFilterForm", new SubstituteCarFilterForm(reservation.getDateFrom(), reservation.getDateTo(), reservation.getDepartmentTake().getId())));
            return "management/substituteCar";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "Error occurred. Resource not found.");
            return "redirect:/mg-res";
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute("customer", customerId);
            redAtt.addAttribute("reservation", reservationId);
            redAtt.addAttribute("department", departmentId);
            redAtt.addFlashAttribute("message", "No cars available for selected department and dates.");
            return "redirect:/mg-res/reservation/{reservation}";
        }
    }

    //Customer reservations page buttons
    @RequestMapping(method = RequestMethod.POST, value = "/reservation")
    public String reservationsDetailButton(RedirectAttributes redAtt, @RequestParam("details_button") Long reservationId, @RequestParam("customer") Long customerId, @RequestParam("department") Long departmentId) {
        redAtt.addAttribute("reservation", reservationId);
        redAtt.addFlashAttribute("customer", customerId);
        redAtt.addFlashAttribute("department", departmentId);
        return "redirect:/mg-res/reservation/{reservation}";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/mg-cus")
    public String manageCustomerButton(RedirectAttributes redAtt, @RequestParam("customer") Long customerId, @RequestParam("department") Long departmentId) {
        redAtt.addAttribute("customer", customerId);
        redAtt.addFlashAttribute("department", departmentId);
        return "redirect:/mg-cus/{customer}";
    }

    //Reservation details page buttons
    @RequestMapping(method = RequestMethod.POST, value = "/reservation/back")
    public String reservationBackButton(RedirectAttributes redAtt, @RequestParam("customer") Long customerId, @RequestParam("department") Long departmentId) {
        redAtt.addAttribute("customer", customerId);
        redAtt.addFlashAttribute("department", departmentId);
        return "redirect:/mg-res/{customer}";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/reservation/refund")
    public String reservationRefundButton(@ModelAttribute("confirmation_form") @Valid ConfirmationForm form, Errors err, RedirectAttributes redAtt, @RequestParam("reservation") Long reservationId, @RequestParam("customer") Long customerId, @RequestParam("department") Long departmentId) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userService.hasNoAccessToUserReservation(cud, customerId, reservationId)) {
            redAtt.addFlashAttribute("message", "Access rejected.");
            return "redirect:/mg-res";
        }

        if (err.hasErrors()) {
            redAtt.addAttribute("reservation", reservationId);
            redAtt.addFlashAttribute("customer", customerId);
            redAtt.addFlashAttribute("department", departmentId);
            redAtt.addFlashAttribute("message", err.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/mg-res/reservation/{reservation}";
        }

        HttpStatus response = reservationService.handleReservationStatus(customerId, reservationId, Reservation.ReservationStatus.STATUS_REFUNDED);

        if (response.equals(HttpStatus.ACCEPTED)) {
            redAtt.addAttribute("reservation", reservationId);
            redAtt.addFlashAttribute("customer", customerId);
            redAtt.addFlashAttribute("department", departmentId);
            redAtt.addFlashAttribute("message", "Refund completed successfully!");
            return "redirect:/mg-res/reservation/{reservation}";
        }
        redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again later or contact customer service.");
        return "redirect:/mg-res";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/reservation/cancel")
    public String reservationCancelButton(@ModelAttribute("confirmation_form") @Valid ConfirmationForm form, Errors err, RedirectAttributes redAtt, @RequestParam("reservation") Long reservationId, @RequestParam("customer") Long customerId, @RequestParam("department") Long departmentId) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userService.hasNoAccessToUserReservation(cud, customerId, reservationId)) {
            redAtt.addFlashAttribute("message", "Access rejected.");
            return "redirect:/mg-res";
        }

        if (err.hasErrors()) {
            redAtt.addAttribute("reservation", reservationId);
            redAtt.addFlashAttribute("customer", customerId);
            redAtt.addFlashAttribute("department", departmentId);
            redAtt.addFlashAttribute("message", err.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/mg-res/reservation/{reservation}";
        }

        HttpStatus response = reservationService.handleReservationStatus(customerId, reservationId, Reservation.ReservationStatus.STATUS_CANCELED);

        if (response.equals(HttpStatus.ACCEPTED)) {
            redAtt.addAttribute("reservation", reservationId);
            redAtt.addFlashAttribute("customer", customerId);
            redAtt.addFlashAttribute("department", departmentId);
            redAtt.addFlashAttribute("message", "Cancel completed successfully!");
            return "redirect:/mg-res/reservation/{reservation}";
        }
        redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again later or contact customer service.");
        return "redirect:/mg-res";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/reservation/rent")
    public String reservationRentButton(@ModelAttribute("rental_confirmation_form") @Valid ConfirmRentalForm form, Errors err, RedirectAttributes redAtt, @RequestParam("reservation") Long reservationId, @RequestParam("customer") Long customerId, @RequestParam("department") Long departmentId) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userService.hasNoAccessToUserReservation(cud, customerId, reservationId)) {
            redAtt.addFlashAttribute("message", "Access rejected.");
            return "redirect:/mg-res";
        }

        if (err.hasErrors()) {
            redAtt.addAttribute("reservation", reservationId);
            redAtt.addFlashAttribute("customer", customerId);
            redAtt.addFlashAttribute("department", departmentId);
            redAtt.addFlashAttribute("message", err.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/mg-res/reservation/{reservation}";
        }

        HttpStatus response = rentService.createRent(customerId, reservationId, form);

        redAtt.addAttribute("reservation", reservationId);
        redAtt.addFlashAttribute("customer", customerId);
        redAtt.addFlashAttribute("department", departmentId);

        if (response.equals(HttpStatus.ACCEPTED)) {
            redAtt.addFlashAttribute("message", "Rent completed successfully!");
        } else if (response.equals(HttpStatus.PAYMENT_REQUIRED)) {
            redAtt.addFlashAttribute("message", "Payment not registered. Please try again later.");
        } else if (response.equals(HttpStatus.PRECONDITION_REQUIRED)) {
            redAtt.addFlashAttribute("message", "Verification not registered. Please try again later.");
        } else {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again later.");
        }
        return "redirect:/mg-res/reservation/{reservation}";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/reservation/retrieve")
    public String reservationRetrieveButton(@ModelAttribute("retrieve_confirmation_form") @Valid ConfirmClaimForm form, Errors err, RedirectAttributes redAtt, @RequestParam("reservation") Long reservationId, @RequestParam("customer") Long customerId, @RequestParam("department") Long departmentId) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userService.hasNoAccessToUserReservation(cud, customerId, reservationId)) {
            redAtt.addFlashAttribute("message", "Access rejected.");
            return "redirect:/mg-res";
        }

        if (err.hasErrors()) {
            redAtt.addAttribute("reservation", reservationId);
            redAtt.addFlashAttribute("customer", customerId);
            redAtt.addFlashAttribute("department", departmentId);
            redAtt.addFlashAttribute("message", err.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/mg-res/reservation/{reservation}";
        }

        HttpStatus response = retrieveService.handleRetrieve(customerId, reservationId, departmentId, form);

        redAtt.addAttribute("reservation", reservationId);
        redAtt.addFlashAttribute("customer", customerId);
        redAtt.addFlashAttribute("department", departmentId);

        if (response.equals(HttpStatus.ACCEPTED)) {
            redAtt.addFlashAttribute("message", "Success: Retrieve completed");
        } else {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again later.");
        }
        return "redirect:/mg-res/reservation/{reservation}";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/reservation/car")
    public String reservationCarButton(RedirectAttributes redAtt, @RequestParam("reservation") Long reservationId, @RequestParam("customer") Long customerId, @RequestParam("department") Long departmentId) {
        redAtt.addAttribute("reservation", reservationId);
        redAtt.addFlashAttribute("customer", customerId);
        redAtt.addFlashAttribute("department", departmentId);
        return "redirect:/mg-res/reservation/{reservation}/car";
    }

    //Substitute car buttons
    @RequestMapping(method = RequestMethod.POST, value = "/reservation/{reservation}/back")
    public String substituteCarBackButton(RedirectAttributes redAtt, @RequestParam("reservation") Long reservationId, @RequestParam("customer") Long customerId, @RequestParam("department") Long departmentId) {
        redAtt.addFlashAttribute("customer", customerId);
        redAtt.addAttribute("reservation", reservationId);
        redAtt.addAttribute("department", departmentId);
        return "redirect:/mg-res/reservation/{reservation}";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/reservation/{reservation}/filter")
    public String substituteCarFilterButton(@ModelAttribute("carFilterForm") SubstituteCarFilterForm filterData, RedirectAttributes redAtt, @RequestParam("reservation") Long reservationId, @RequestParam("customer") Long customerId, @RequestParam("department") Long departmentId) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userService.hasNoAccessToUserReservation(cud, customerId, reservationId)) {
            redAtt.addFlashAttribute("message", "Access rejected.");
            return "redirect:/mg-res";
        }

        redAtt.addFlashAttribute("filteredCars", carService.filterCars(filterData));
        redAtt.addFlashAttribute("carFilterForm", filterData);
        redAtt.addFlashAttribute("customer", customerId);
        redAtt.addAttribute("reservation", reservationId);
        redAtt.addAttribute("department", departmentId);

        return "redirect:/mg-res/reservation/{reservation}/car";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/reservation/{reservation}/select")
    public String substituteCarSelectButton(@RequestParam("select") Long carId, RedirectAttributes redAtt, @RequestParam("reservation") Long reservationId, @RequestParam("customer") Long customerId, @RequestParam("department") Long departmentId) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userService.hasNoAccessToUserReservation(cud, customerId, reservationId)) {
            redAtt.addFlashAttribute("message", "Access rejected.");
            return "redirect:/mg-res";
        }

        HttpStatus status = reservationService.substituteCar(reservationId, customerId, carId);

        redAtt.addFlashAttribute("customer", customerId);
        redAtt.addAttribute("reservation", reservationId);
        redAtt.addAttribute("department", departmentId);

        if (status.equals(HttpStatus.ACCEPTED)) {
            redAtt.addFlashAttribute("message", "Car successfully substituted.");
            return "redirect:/mg-res/reservation/{reservation}";
        }
        redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again later.");
        return "redirect:/mg-res";
    }
}
