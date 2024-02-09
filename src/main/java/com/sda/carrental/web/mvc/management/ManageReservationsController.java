package com.sda.carrental.web.mvc.management;

import com.sda.carrental.exceptions.IllegalActionException;
import com.sda.carrental.global.ConstantValues;
import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.enums.Country;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.property.PaymentDetails;
import com.sda.carrental.model.property.car.CarBase;
import com.sda.carrental.model.users.Customer;
import com.sda.carrental.model.users.auth.Verification;
import com.sda.carrental.service.*;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.property.cars.SubstituteCarBaseFilterForm;
import com.sda.carrental.web.mvc.form.common.ConfirmationForm;
import com.sda.carrental.web.mvc.form.operational.ConfirmClaimForm;
import com.sda.carrental.web.mvc.form.operational.ConfirmRentalForm;
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
    private final CarBaseService carBaseService;
    private final RentService rentService;
    private final RetrieveService retrieveService;
    private final EmployeeService employeeService;
    private final ConstantValues cv;

    private final String MSG_KEY = "message";
    private final String MSG_ACCESS_REJECTED = "Failure: Access rejected";
    private final String MSG_GENERIC_EXCEPTION = "Failure: An unexpected error occurred";
    private final String MSG_NO_RESOURCE = "Failure: Resource not found";

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String redirectPage() {
        return "redirect:/mg-cus";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{department}-{customer}")
    public String customerReservationsPage(final ModelMap map, RedirectAttributes redAtt, @PathVariable("customer") Long customerId, @PathVariable("department") Long departmentId) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (employeeService.hasNoAccessToCustomerData(cud, customerId, departmentId)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-cus";
            }

            Customer customer = customerService.findById(customerId);
            map.addAttribute("department", departmentService.findById(departmentId));
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
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
            return "redirect:/mg-cus";
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/mg-cus";
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{department}-{customer}/{reservation}")
    public String reservationDetailsPage(final ModelMap map, RedirectAttributes redAtt, @PathVariable("reservation") Long reservationId, @PathVariable("customer") Long customerId, @PathVariable("department") Long departmentId) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (employeeService.hasNoAccessToCustomerOperation(cud, customerId, reservationId) ||
                    employeeService.departmentAccess(cud, departmentId).equals(HttpStatus.FORBIDDEN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-cus";
            }

            Reservation reservation = reservationService.findCustomerReservation(customerId, reservationId);
            if (!(reservation.getDepartmentTake().getId().equals(departmentId) || reservation.getDepartmentBack().getId().equals(departmentId)))
                throw new IllegalActionException();

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
                    map.addAttribute("total_price", cv.getDeptReturnPriceDiff() + (days * reservation.getCarBase().getPriceDay()));
                } else {
                    map.addAttribute("diff_return_price", 0.0);
                    map.addAttribute("total_price", days * reservation.getCarBase().getPriceDay());
                }
                map.addAttribute("raw_price", days * reservation.getCarBase().getPriceDay());
                map.addAttribute("deposit_value", reservation.getCarBase().getDepositValue());
            }

            map.addAttribute("reservation", reservation);
            map.addAttribute("fee_percentage", cv.getCancellationFeePercentage() * 100);
            map.addAttribute("refund_fee_days", cv.getRefundSubtractDaysDuration());
            map.addAttribute("deposit_deadline", cv.getRefundDepositDeadlineDays());

            map.addAttribute("confirmation_form", new ConfirmationForm());
            if (reservation.getStatus().equals(Reservation.ReservationStatus.STATUS_RESERVED)) {
                map.addAttribute("carOptions", carService.findAvailableCarsInDepartment(reservation));
                map.addAttribute("rental_confirmation_form", new ConfirmRentalForm(reservationId, LocalDate.now()));
            } else if (reservation.getStatus().equals(Reservation.ReservationStatus.STATUS_PROGRESS)) {
                map.addAttribute("rent_details", rentService.findById(reservation.getId()));
                if (departmentId.equals(reservation.getDepartmentBack().getId())) {
                    map.addAttribute("retrieve_confirmation_form", new ConfirmClaimForm(reservationId, departmentId, LocalDate.now()));
                }
            } else if (reservation.getStatus().equals(Reservation.ReservationStatus.STATUS_COMPLETED)) {
                map.addAttribute("rent_details", rentService.findById(reservation.getId()));
                map.addAttribute("retrieve_details", retrieveService.findById(reservation.getId()).orElseThrow(ResourceNotFoundException::new));
            }
            return "management/viewReservation";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
            return "redirect:/mg-cus";
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/mg-cus";
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{department}-{customer}/{reservation}/change-car")
    public String substituteCarBasePage(final ModelMap map, RedirectAttributes redAtt, @PathVariable("reservation") Long reservationId, @PathVariable("customer") Long customerId, @PathVariable("department") Long departmentId) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            Reservation reservation = reservationService.findCustomerReservation(customerId, reservationId);
            if (!reservation.getDepartmentTake().getId().equals(departmentId)) throw new IllegalActionException();
            if (employeeService.departmentAccess(cud, reservation.getDepartmentTake().getId()).equals(HttpStatus.FORBIDDEN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-cus";
            }


            if (!(reservation.getStatus().equals(Reservation.ReservationStatus.STATUS_RESERVED) || reservation.getStatus().equals(Reservation.ReservationStatus.STATUS_PENDING))
                    && !reservation.getDepartmentTake().getId().equals(departmentId)) {
                throw new RuntimeException();
            }

            List<CarBase> carList = carBaseService.findAvailableCarBasesInDepartment(reservation.getDepartmentTake().getId());
            if (carList.isEmpty()) throw new IllegalActionException();

            map.addAttribute("carBases", map.getOrDefault("filteredCarBases", carList));

            Map<String, Object> carProperties = carBaseService.getFilterProperties(carList, false);

            map.addAttribute("brands", carProperties.get("brands"));
            map.addAttribute("types", carProperties.get("types"));
            map.addAttribute("seats", carProperties.get("seats"));

            map.addAttribute("days", (reservation.getDateFrom().until(reservation.getDateTo(), ChronoUnit.DAYS) + 1));

            map.addAttribute("confirmation_form", new ConfirmationForm());
            map.addAttribute("carFilterForm", map.getOrDefault("carFilterForm", new SubstituteCarBaseFilterForm(reservation.getDepartmentTake().getId())));
            return "management/substituteCar";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
            return "redirect:/mg-cus";
        } catch (IllegalActionException err) {
            redAtt.addAttribute("customer", customerId);
            redAtt.addAttribute("reservation", reservationId);
            redAtt.addAttribute("department", departmentId);
            redAtt.addFlashAttribute(MSG_KEY, "No cars available for selected department and dates.");
            return "redirect:/mg-res/{department}-{customer}/{reservation}";
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/mg-cus";
        }
    }

    //Reservation details page buttons
    @RequestMapping(method = RequestMethod.POST, value = "/{department}-{customer}/{reservation}/refund")
    public String reservationRefundButton(@ModelAttribute("confirmation_form") @Valid ConfirmationForm form, Errors err, RedirectAttributes redAtt, @PathVariable("reservation") Long reservationId, @PathVariable("customer") Long customerId, @PathVariable("department") Long departmentId) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Reservation r = reservationService.findCustomerReservation(customerId, reservationId);

            if (!r.getDepartmentTake().getId().equals(departmentId) ||
                    employeeService.departmentAccess(cud, r.getDepartmentTake().getId()).equals(HttpStatus.FORBIDDEN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-cus";
            }

            if (err.hasErrors()) {
                redAtt.addAttribute("reservation", reservationId);
                redAtt.addAttribute("customer", customerId);
                redAtt.addAttribute("department", departmentId);
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-res/{department}-{customer}/{reservation}";
            }

            HttpStatus response = reservationService.handleReservationStatus(customerId, reservationId, Reservation.ReservationStatus.STATUS_REFUNDED);

            if (response.equals(HttpStatus.ACCEPTED)) {
                redAtt.addAttribute("reservation", reservationId);
                redAtt.addAttribute("customer", customerId);
                redAtt.addAttribute("department", departmentId);
                redAtt.addFlashAttribute(MSG_KEY, "Success: Refund completed successfully");
                return "redirect:/mg-res/{department}-{customer}/{reservation}";
            }
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/mg-cus";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
            return "redirect:/mg-cus";
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/mg-cus";
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{department}-{customer}/{reservation}/cancel")
    public String reservationCancelButton(@ModelAttribute("confirmation_form") @Valid ConfirmationForm form, Errors err, RedirectAttributes redAtt, @PathVariable("reservation") Long reservationId, @PathVariable("customer") Long customerId, @PathVariable("department") Long departmentId) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Reservation r = reservationService.findCustomerReservation(customerId, reservationId);

            if (!r.getDepartmentTake().getId().equals(departmentId) ||
                    employeeService.departmentAccess(cud, r.getDepartmentTake().getId()).equals(HttpStatus.FORBIDDEN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-cus";
            }

            if (err.hasErrors()) {
                redAtt.addAttribute("reservation", reservationId);
                redAtt.addAttribute("customer", customerId);
                redAtt.addAttribute("department", departmentId);
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-res/{department}-{customer}/{reservation}";
            }

            HttpStatus response = reservationService.handleReservationStatus(customerId, reservationId, Reservation.ReservationStatus.STATUS_CANCELED);

            if (response.equals(HttpStatus.ACCEPTED)) {
                redAtt.addAttribute("reservation", reservationId);
                redAtt.addAttribute("customer", customerId);
                redAtt.addAttribute("department", departmentId);
                redAtt.addFlashAttribute(MSG_KEY, "Success: Cancel completed successfully");
                return "redirect:/mg-res/{department}-{customer}/{reservation}";
            }
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/mg-cus";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
            return "redirect:/mg-cus";
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/mg-cus";
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{department}-{customer}/{reservation}/rent")
    public String reservationRentButton(@ModelAttribute("rental_confirmation_form") @Valid ConfirmRentalForm form, Errors err, RedirectAttributes redAtt, @PathVariable("reservation") Long reservationId, @PathVariable("customer") Long customerId, @PathVariable("department") Long departmentId) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Reservation r = reservationService.findCustomerReservation(customerId, reservationId);

            if (!r.getDepartmentTake().getId().equals(departmentId) ||
                    employeeService.departmentAccess(cud, r.getDepartmentTake().getId()).equals(HttpStatus.FORBIDDEN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-cus";
            }

            redAtt.addAttribute("reservation", reservationId);
            redAtt.addAttribute("customer", customerId);
            redAtt.addAttribute("department", departmentId);

            if (err.hasErrors()) {
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-res/{department}-{customer}/{reservation}";
            }

            HttpStatus response = rentService.createRent(customerId, form);

            if (response.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Success: Rent completed successfully");
            } else if (response.equals(HttpStatus.PAYMENT_REQUIRED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Failure: Payment not registered");
            } else if (response.equals(HttpStatus.PRECONDITION_REQUIRED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Failure: Verification not registered");
            } else if (response.equals(HttpStatus.CONFLICT)) {
                redAtt.addFlashAttribute(MSG_KEY, "Failure: Status unavailable");
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }
            return "redirect:/mg-res/{department}-{customer}/{reservation}";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
            return "redirect:/mg-cus";
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/mg-cus";
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{department}-{customer}/{reservation}/retrieve")
    public String reservationRetrieveButton(@ModelAttribute("retrieve_confirmation_form") @Valid ConfirmClaimForm form, Errors err, RedirectAttributes redAtt, @PathVariable("reservation") Long reservationId, @PathVariable("customer") Long customerId, @PathVariable("department") Long departmentId) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Reservation r = reservationService.findCustomerReservation(customerId, reservationId);

            if (!r.getDepartmentTake().getId().equals(departmentId) ||
                    employeeService.departmentAccess(cud, r.getDepartmentBack().getId()).equals(HttpStatus.FORBIDDEN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-cus";
            }
            if (!form.getReservationId().equals(reservationId) ||
                    !form.getDepartmentId().equals(departmentId)) throw new IllegalActionException();

            redAtt.addAttribute("customer", customerId);
            redAtt.addAttribute("department", departmentId);
            redAtt.addAttribute("reservation", reservationId);


            if (err.hasErrors()) {
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-res/{department}-{customer}/{reservation}";
            }

            HttpStatus response = retrieveService.handleRetrieve(customerId, departmentId, form);


            if (response.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Success: Retrieve completed");
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }
            return "redirect:/mg-res/{department}-{customer}/{reservation}";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
            return "redirect:/mg-cus";
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/mg-cus";
        }
    }

    //Substitute car base buttons
    @RequestMapping(method = RequestMethod.POST, value = "/{department}-{customer}/{reservation}/filter")
    public String substituteCarBaseFilterButton(@ModelAttribute("carFilterForm") SubstituteCarBaseFilterForm filterData, RedirectAttributes redAtt, @PathVariable("reservation") Long reservationId, @PathVariable("customer") Long customerId, @PathVariable("department") Long departmentId) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Reservation reservation = reservationService.findCustomerReservation(customerId, reservationId);

            if (!reservation.getDepartmentTake().getId().equals(departmentId) || !filterData.getDepartmentId().equals(departmentId)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
                return "redirect:/mg-cus";
            }
            if (employeeService.departmentAccess(cud, reservation.getDepartmentTake().getId()).equals(HttpStatus.FORBIDDEN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-cus";
            }

            redAtt.addFlashAttribute("filteredCarBases", carBaseService.findCarBasesByForm(filterData));
            redAtt.addFlashAttribute("carFilterForm", filterData);
            redAtt.addAttribute("customer", customerId);
            redAtt.addAttribute("reservation", reservationId);
            redAtt.addAttribute("department", departmentId);

            return "redirect:/mg-res/{department}-{customer}/{reservation}/change-car";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
            return "redirect:/mg-cus";
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/mg-cus";
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{department}-{customer}/{reservation}/select")
    public String substituteCarBaseSelectButton(@RequestParam("select") Long carBaseId, RedirectAttributes redAtt, @PathVariable("reservation") Long reservationId, @PathVariable("customer") Long customerId, @PathVariable("department") Long departmentId) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Reservation reservation = reservationService.findCustomerReservation(customerId, reservationId);

            if (!reservation.getDepartmentTake().getId().equals(departmentId)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
                return "redirect:/mg-cus";
            }
            if (employeeService.departmentAccess(cud, reservation.getDepartmentTake().getId()).equals(HttpStatus.FORBIDDEN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-cus";
            }

            HttpStatus status = reservationService.substituteCarBase(reservation, carBaseId);

            redAtt.addAttribute("customer", customerId);
            redAtt.addAttribute("reservation", reservationId);
            redAtt.addAttribute("department", departmentId);

            if (status.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Success: Car successfully substituted");
                return "redirect:/mg-res/{department}-{customer}/{reservation}";
            }
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/mg-cus";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
            return "redirect:/mg-cus";
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/mg-cus";
        }
    }
}
