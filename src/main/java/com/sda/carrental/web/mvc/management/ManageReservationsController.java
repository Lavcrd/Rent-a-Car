package com.sda.carrental.web.mvc.management;

import com.sda.carrental.global.ConstantValues;
import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.property.Car;
import com.sda.carrental.model.property.Department;
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
    private final RentingService rentingService;
    private final ReturningService returningService;
    private final UserService userService;

    private final ConstantValues cv;

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String searchReservationsPage(ModelMap map, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<Department> employeeDepartments = departmentService.getDepartmentsByRole(cud);
            map.addAttribute("departments", employeeDepartments);
            map.addAttribute("departmentsCountry", departmentService.findAllWhereCountry(employeeDepartments.get(0).getCountry()));
            map.addAttribute("reservationStatuses", Reservation.ReservationStatus.values());

            if (map.containsKey("searchReservationsForm")) {
                return "management/searchReservations";
            } else {
                SearchReservationsForm form = new SearchReservationsForm();
                form.setDateFrom(LocalDate.now().minusWeeks(1));
                form.setDateTo(LocalDate.now().plusWeeks(1));
                map.addAttribute("searchReservationsForm", form);
            }
            return "management/searchReservations";
        } catch (ResourceNotFoundException | IndexOutOfBoundsException err) {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again.");
            return "redirect:/";
        }
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
            map.addAttribute("reservations", reservationService.getUserReservationsByDepartmentTake(customer.getId(), departmentId));
            map.addAttribute("returns", reservationService.getUserReservationsByDepartmentBack(customer.getId(), departmentId));

            Optional<Verification> verification = verificationService.getOptionalVerificationByCustomer(customerId);
            if (verification.isPresent()) {
                map.addAttribute("verification", verificationService.maskVerification(verification.get()));
            } else {
                map.addAttribute("verification", new Verification(customerId, "N/D", "N/D"));
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

            Reservation reservation = reservationService.getCustomerReservation(customerId, reservationId);

            if (departmentService.departmentAccess(cud, reservation.getDepartmentTake().getDepartmentId()).equals(HttpStatus.FORBIDDEN)) {
                redAtt.addFlashAttribute("message", "Incorrect data. Access not allowed.");
                return "redirect:/mg-res";
            }
            Optional<PaymentDetails> receipt = paymentDetailsService.getOptionalPaymentDetails(reservation);

            if (receipt.isPresent()) {
                map.addAttribute("diff_return_price", receipt.get().getRequiredReturnValue());
                map.addAttribute("raw_price", receipt.get().getRequiredRawValue());
                map.addAttribute("total_price", receipt.get().getRequiredRawValue() + receipt.get().getRequiredReturnValue());
                map.addAttribute("deposit_value", receipt.get().getRequiredDeposit());
            } else {
                long days = reservation.getDateFrom().until(reservation.getDateTo(), ChronoUnit.DAYS) + 1;
                if (!reservation.getDepartmentTake().equals(reservation.getDepartmentBack())) {
                    map.addAttribute("diff_return_price", cv.getDeptReturnPriceDiff());
                    map.addAttribute("total_price", cv.getDeptReturnPriceDiff() + (days * reservation.getCar().getPrice_day()));
                } else {
                    map.addAttribute("diff_return_price", 0.0);
                    map.addAttribute("total_price", days * reservation.getCar().getPrice_day());
                }
                map.addAttribute("raw_price", days * reservation.getCar().getPrice_day());
                map.addAttribute("deposit_value", reservation.getCar().getDepositValue());
            }

            map.addAttribute("reservation", reservation);
            map.addAttribute("deposit_percentage", cv.getDepositPercentage() * 100);
            map.addAttribute("refund_fee_days", cv.getRefundSubtractDaysDuration());

            map.addAttribute("confirmation_form", new ConfirmationForm());
            if (reservation.getStatus().equals(Reservation.ReservationStatus.STATUS_RESERVED)) {
                map.addAttribute("rental_confirmation_form", new ConfirmRentalForm());
            } else if (reservation.getStatus().equals(Reservation.ReservationStatus.STATUS_PROGRESS)) {
                map.addAttribute("rent_details", rentingService.findById(reservation.getReservationId()));
            } else if (reservation.getStatus().equals(Reservation.ReservationStatus.STATUS_COMPLETED)) {
                map.addAttribute("rent_details", rentingService.findById(reservation.getReservationId()));
                map.addAttribute("return_details", returningService.findById(reservation.getReservationId()));
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

            Reservation reservation = reservationService.getCustomerReservation(customerId, reservationId);
            List<Car> carList = carService.findAvailableCarsInDepartment(reservation);
            if (carList.isEmpty()) throw new RuntimeException();

            if (!map.containsKey("filteredCars")) {
                map.addAttribute("cars", carList);
            } else {
                map.addAttribute("cars", map.getAttribute("filteredCars"));
            }
            Map<String, Object> carProperties = carService.getFilterProperties(carList);

            map.addAttribute("brand", carProperties.get("brand"));
            map.addAttribute("type", carProperties.get("type"));
            map.addAttribute("seats", carProperties.get("seats"));

            map.addAttribute("days", (reservation.getDateFrom().until(reservation.getDateTo(), ChronoUnit.DAYS) + 1));

            SubstituteCarFilterForm carFilterForm = new SubstituteCarFilterForm();
            carFilterForm.setDateFrom(reservation.getDateFrom());
            carFilterForm.setDateTo(reservation.getDateTo());
            carFilterForm.setDepartmentId(reservation.getDepartmentTake().getDepartmentId());

            map.addAttribute("confirmation_form", new ConfirmationForm());
            map.addAttribute("carFilterForm", carFilterForm);
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

    //Search reservations page buttons
    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public String reservationSearchButton(@ModelAttribute("searchReservationsForm") SearchReservationsForm reservationsData, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (departmentService.departmentAccess(cud, reservationsData.getDepartmentTake()).equals(HttpStatus.FORBIDDEN)) {
                redAtt.addFlashAttribute("message", "Incorrect data provided. Search rejected.");
                return "redirect:/mg-res";
            }

            redAtt.addFlashAttribute("searchReservationsForm", reservationsData);
            redAtt.addFlashAttribute("departments", departmentService.getDepartmentsByRole(cud));

            redAtt.addFlashAttribute("reservations", reservationService.findReservationsByDetails(reservationsData));
            return "redirect:/mg-res";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again.");
            return "redirect:/";
        }
    }

    @RequestMapping(value = "/reservations", method = RequestMethod.POST)
    public String customerViewButton(RedirectAttributes redAtt, @RequestParam("select_button") Long customerId, @RequestParam("department") Long departmentId) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userService.hasNoAccessToUserData(cud, customerId, departmentId)) {
            redAtt.addFlashAttribute("message", "Access rejected.");
            return "redirect:/mg-res";
        }

        redAtt.addAttribute("customer", customerId);
        redAtt.addFlashAttribute("department", departmentId);
        return "redirect:/mg-res/{customer}";
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

        HttpStatus response = rentingService.createRent(customerId, reservationId, form.getRemarks());

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
