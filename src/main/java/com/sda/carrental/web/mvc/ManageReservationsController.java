package com.sda.carrental.web.mvc;

import com.sda.carrental.constants.GlobalValues;
import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.property.Car;
import com.sda.carrental.model.property.PaymentDetails;
import com.sda.carrental.model.users.Customer;
import com.sda.carrental.model.users.Verification;
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

    private final GlobalValues gv;

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String searchCustomersPage(ModelMap map, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            map.addAttribute("departments", departmentService.getDepartmentsByRole(cud));

            if (map.containsKey("searchCustomerForm")) {
                return "management/searchCustomers";
            } else {
                map.addAttribute("searchCustomerForm", new SearchCustomerForm());
            }
            return "management/searchCustomers";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again.");
            return "redirect:/";
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{customer}")
    public String customerReservationsPage(final ModelMap map, RedirectAttributes redAtt, @PathVariable(value = "customer") Long customerId, @ModelAttribute("department") Long departmentId) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (HttpStatus.FORBIDDEN.equals(departmentService.departmentAccess(cud, departmentId))) {
                redAtt.addFlashAttribute("message", "Incorrect data. Access not allowed.");
                return "redirect:/mg-res";
            }

            Customer customer = customerService.findById(customerId);
            map.addAttribute("department", departmentId);
            map.addAttribute("customer", customer);
            map.addAttribute("reservations", reservationService.getUserReservationsByDepartmentTake(customer.getEmail(), departmentId));

            Optional<Verification> verification = verificationService.getOptionalVerificationByCustomer(customer);
            map.addAttribute("is_verified", verification.isPresent());
            if (verification.isPresent()) {
                map.addAttribute("verification", verificationService.maskVerification(verification.get()));
            } else {
                map.addAttribute("verification", new Verification(customer, "N/D", "N/D"));
            }
            return "management/viewCustomerReservations";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again.");
            return "redirect:/mg-res";
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/reservation/{reservation}")
    public String reservationDetailsPage(final ModelMap map, RedirectAttributes redAtt, @PathVariable(value = "reservation") Long reservationId, @ModelAttribute("customer") Long customerId) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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
                    map.addAttribute("diff_return_price", gv.getDeptReturnPriceDiff());
                    map.addAttribute("total_price", gv.getDeptReturnPriceDiff() + (days * reservation.getCar().getPrice_day()));
                } else {
                    map.addAttribute("diff_return_price", 0.0);
                    map.addAttribute("total_price", days * reservation.getCar().getPrice_day());
                }
                map.addAttribute("raw_price", days * reservation.getCar().getPrice_day());
                map.addAttribute("deposit_value", reservation.getCar().getDepositValue());
            }

            map.addAttribute("reservation", reservation);
            map.addAttribute("deposit_percentage", gv.getDepositPercentage() * 100);
            map.addAttribute("refund_fee_days", gv.getRefundSubtractDaysDuration());
            return "management/reservationDetailsManagement";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again.");
            return "redirect:/mg-res";
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/reservation/{reservation}/car")
    public String substituteCarPage(final ModelMap map, RedirectAttributes redAtt, @PathVariable(value = "reservation") Long reservationId, @ModelAttribute("customer") Long customerId) {

        Reservation reservation = reservationService.getCustomerReservation(customerId, reservationId);
        List<Car> carList = carService.findAvailableCarsInDepartment(reservation);

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

        map.addAttribute("carFilterForm", carFilterForm);
        return "management/substituteCar";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/verify")
    public String verifyPage(ModelMap map, @ModelAttribute("customer") Long customerId, @ModelAttribute("department") Long departmentId) {
        map.addAttribute("verification_form", new VerificationForm(customerId));
/*        map.addAttribute("department", departmentId);
        map.addAttribute("customer", customerId);*/
        return "management/verifyCustomer";
    }

    //Search customers page buttons
    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public String customerSearchButton(@ModelAttribute("searchCustomerForm") SearchCustomerForm customersData, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            redAtt.addFlashAttribute("searchCustomerForm", customersData);
            redAtt.addFlashAttribute("departments", departmentService.getDepartmentsByRole(cud));

            redAtt.addFlashAttribute("customers", customerService.findCustomersByDepartmentAndName(customersData));
            return "redirect:/mg-res";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again.");
            return "redirect:/";
        }
    }

    @RequestMapping(value = "/reservations", method = RequestMethod.POST)
    public String customerViewButton(RedirectAttributes redAtt, @RequestParam("select_button") Long customerId, @RequestParam("department") Long departmentId) {
        redAtt.addAttribute("customer", customerId);
        redAtt.addFlashAttribute("department", departmentId);
        return "redirect:/mg-res/{customer}";
    }

    //Customer reservations page buttons
    @RequestMapping(method = RequestMethod.POST, value = "/reservation")
    public String reservationsDetailButton(RedirectAttributes redAtt, @RequestParam("details_button") Long reservationId, @RequestParam("customer") Long customerId) {
        redAtt.addAttribute("reservation", reservationId);
        redAtt.addFlashAttribute("customer", customerId);
        return "redirect:/mg-res/reservation/{reservation}";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/verify")
    public String verifyButton(RedirectAttributes redAtt, @RequestParam("customer") Long customerId, @RequestParam("department") Long departmentId) {
        redAtt.addFlashAttribute("customer", customerId);
        redAtt.addFlashAttribute("department", departmentId);
        return "redirect:/mg-res/verify";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/unverify")
    public String unverifyButton(RedirectAttributes redAtt, @RequestParam("customer") Long customerId, @RequestParam("department") Long departmentId) {
        HttpStatus status = verificationService.deleteByCustomerId(customerId);
        if (status.equals(HttpStatus.OK)) {
            redAtt.addAttribute("customer", customerId);
            redAtt.addFlashAttribute("department", departmentId);
            redAtt.addFlashAttribute("message", "Verification successfully removed");
        } else {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again.");
            return "redirect:/mg-res";
        }
        return "redirect:/mg-res/{customer}";
    }


    //Verification page buttons
    @RequestMapping(method = RequestMethod.POST, value = "/verify/create")
    public String verifyConfirmButton(RedirectAttributes redAtt, @ModelAttribute("verification_form") @Valid VerificationForm form, Errors errors, @RequestParam("department") Long departmentId) {
        if (errors.hasErrors()) {
            redAtt.addFlashAttribute("department", departmentId);
            redAtt.addFlashAttribute("customer", form.getCustomerId());

            redAtt.addFlashAttribute("message", "Provided values are incorrect.");
            return "redirect:/mg-res/verify";
        }


        HttpStatus status = verificationService.createVerification(form);
        if (status.equals(HttpStatus.CREATED)) {
            redAtt.addFlashAttribute("message", "Verification successfully registered.");
        } else if (status.equals(HttpStatus.EXPECTATION_FAILED)) {
            redAtt.addFlashAttribute("message", "Customer is already verified.");
        } else if (status.equals(HttpStatus.NOT_FOUND)) {
            redAtt.addFlashAttribute("message", "Customer not found. Try again.");
            return "redirect:/mg-res";
        }


        redAtt.addAttribute("customer", form.getCustomerId());
        redAtt.addFlashAttribute("department", departmentId);
        return "redirect:/mg-res/{customer}";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/verify/back")
    public String verifyBackButton(RedirectAttributes redAtt, @ModelAttribute("verification_form") VerificationForm form, @RequestParam("department") Long departmentId) {
        redAtt.addAttribute("customer", form.getCustomerId());
        redAtt.addFlashAttribute("department", departmentId);
        return "redirect:/mg-res/{customer}";
    }


    //Reservation page buttons
    @RequestMapping(method = RequestMethod.POST, value = "/reservation/back")
    public String reservationBackButton(RedirectAttributes redAtt, @RequestParam("reservation") Long reservationId, @RequestParam("customer") Long customerId) {
        reservationService.getCustomerReservation(customerId, reservationId);
        redAtt.addAttribute("customer", customerId);
        redAtt.addFlashAttribute("department", reservationService.getCustomerReservation(customerId, reservationId).getDepartmentTake().getDepartmentId());
        return "redirect:/mg-res/{customer}";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/reservation/refund")
    public String reservationRefundButton(RedirectAttributes redAtt, @RequestParam("reservation") Long reservationId, @RequestParam("customer") Long customerId) {
        HttpStatus response = reservationService.handleReservationStatus(customerId, reservationId, Reservation.ReservationStatus.STATUS_REFUNDED);

        if (response.equals(HttpStatus.ACCEPTED)) {
            redAtt.addAttribute("reservation", reservationId);
            redAtt.addFlashAttribute("customer", customerId);
            redAtt.addFlashAttribute("message", "Refund completed successfully!");
            return "redirect:/mg-res/reservation/{reservation}";
        }
        redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again later or contact customer service.");
        return "redirect:/mg-res";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/reservation/cancel")
    public String reservationCancelButton(RedirectAttributes redAtt, @RequestParam("reservation") Long reservationId, @RequestParam("customer") Long customerId) {
        HttpStatus response = reservationService.handleReservationStatus(customerId, reservationId, Reservation.ReservationStatus.STATUS_CANCELED);

        if (response.equals(HttpStatus.ACCEPTED)) {
            redAtt.addAttribute("reservation", reservationId);
            redAtt.addFlashAttribute("customer", customerId);
            redAtt.addFlashAttribute("message", "Cancel completed successfully!");
            return "redirect:/mg-res/reservation/{reservation}";
        }
        redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again later or contact customer service.");
        return "redirect:/mg-res";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/reservation/rent")
    public String reservationRentButton(RedirectAttributes redAtt, @RequestParam("reservation") Long reservationId, @RequestParam("customer") Long customerId) {
        HttpStatus response = reservationService.handleReservationStatus(customerId, reservationId, Reservation.ReservationStatus.STATUS_PROGRESS);

        redAtt.addAttribute("reservation", reservationId);
        redAtt.addFlashAttribute("customer", customerId);

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
    public String reservationCarButton(RedirectAttributes redAtt, @RequestParam("reservation") Long reservationId, @RequestParam("customer") Long customerId) {

        //TODO page

        redAtt.addAttribute("reservation", reservationId);
        redAtt.addFlashAttribute("customer", customerId);
        return "redirect:/mg-res/reservation/{reservation}/car";
    }

    //Substitute car buttons
    @RequestMapping(method = RequestMethod.POST, value = "/reservation/{reservation}/back")
    public String substituteCarBackButton(RedirectAttributes redAtt, @RequestParam("reservation") Long reservationId, @RequestParam("customer") Long customerId) {
        redAtt.addFlashAttribute("customer", customerId);
        redAtt.addAttribute("reservation", reservationId);
        return "redirect:/mg-res/reservation/{reservation}";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/reservation/{reservation}/filter")
    public String substituteCarFilterButton(@ModelAttribute("carFilterForm") SubstituteCarFilterForm filterData, RedirectAttributes redAtt, @RequestParam("reservation") Long reservationId, @RequestParam("customer") Long customerId) {
        redAtt.addFlashAttribute("filteredCars", carService.filterCars(filterData, reservationService.getCustomerReservation(customerId, reservationId)));
        redAtt.addFlashAttribute("customer", customerId);
        redAtt.addAttribute("reservation", reservationId);
        return "redirect:/mg-res/reservation/{reservation}/car";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/reservation/{reservation}/select")
    public String substituteCarSelectButton(@RequestParam("select") Long carId, RedirectAttributes redAtt, @RequestParam("reservation") Long reservationId, @RequestParam("customer") Long customerId) {

        HttpStatus status = reservationService.substituteCar(reservationId, customerId, carId);

        redAtt.addFlashAttribute("customer", customerId);
        redAtt.addAttribute("reservation", reservationId);

        if (status.equals(HttpStatus.ACCEPTED)) {
            redAtt.addFlashAttribute("message", "Car successfully substituted.");
            return "redirect:/mg-res/reservation/{reservation}";
        }
        redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again later.");
        return "redirect:/mg-res";
    }

}
