package com.sda.rentacar.web.mvc.management;

import com.sda.rentacar.exceptions.ResourceNotFoundException;
import com.sda.rentacar.model.operational.Reservation;
import com.sda.rentacar.model.property.department.Department;
import com.sda.rentacar.model.users.Customer;
import com.sda.rentacar.model.users.auth.Verification;
import com.sda.rentacar.service.*;
import com.sda.rentacar.service.auth.CustomUserDetails;
import com.sda.rentacar.web.mvc.form.common.ConfirmationForm;
import com.sda.rentacar.web.mvc.form.users.ChangeContactForm;
import com.sda.rentacar.web.mvc.form.users.customer.FindVerifiedForm;
import com.sda.rentacar.web.mvc.form.users.customer.SearchCustomersForm;
import com.sda.rentacar.web.mvc.form.users.customer.VerificationForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mg-cus")
public class ManageCustomersController {
    private final DepartmentService departmentService;
    private final CountryService countryService;
    private final UserService userService;
    private final CustomerService customerService;
    private final VerificationService verificationService;
    private final EmployeeService employeeService;

    private final String MSG_KEY = "message";
    private final String MSG_ACCESS_REJECTED = "Failure: Access rejected";
    private final String MSG_GENERIC_EXCEPTION = "Failure: An unexpected error occurred";
    private final String MSG_NO_RESOURCE = "Failure: Customer not found";

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String searchCustomersPage(ModelMap map, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<Department> employeeDepartments = employeeService.getDepartmentsByUserContext(cud);
            map.addAttribute("departments", employeeDepartments);
            map.addAttribute("departmentsCountry", departmentService.findAllWhereCountry(employeeDepartments.get(0).getCountry()));
            map.addAttribute("reservationStatuses", Reservation.ReservationStatus.values());
            map.addAttribute("searchCustomersForm", map.getOrDefault("searchCustomersForm", new SearchCustomersForm()));
            map.addAttribute("isArrival", map.getOrDefault("isArrival", false));

            return "management/searchCustomers";
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/";
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{department}-{customer}")
    public String viewCustomerPage(ModelMap map, RedirectAttributes redAtt, @PathVariable(value = "customer") Long customerId, @PathVariable("department") Long departmentId) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (employeeService.hasNoAccessToCustomerData(cud, customerId, departmentId)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-cus";
            }

            Customer customer = customerService.findById(customerId);
            map.addAttribute("department", departmentId);
            map.addAttribute("customer", customer);

            Optional<Verification> verification = verificationService.getOptionalVerificationByCustomer(customerId);
            map.addAttribute("is_verified", verification.isPresent());
            if (verification.isPresent()) {
                map.addAttribute("verification", verificationService.maskVerification(verification.get()));
                map.addAttribute("unverifyConfirmationForm", new ConfirmationForm());
            } else {
                map.addAttribute("verification", new Verification(customerId, countryService.placeholder(), "N/D", "N/D"));
                map.addAttribute("countries", countryService.findAll());
                map.addAttribute("findVerifiedForm", new FindVerifiedForm());
                map.addAttribute("verification_form", new VerificationForm());
            }
            if (!customer.getStatus().equals(Customer.Status.STATUS_DELETED)) {
                map.addAttribute("is_deleted", false);
                map.addAttribute("deleteConfirmationForm", new ConfirmationForm());
                map.addAttribute("changeContactForm", new ChangeContactForm());
            } else {
                map.addAttribute("is_deleted", true);
            }
            return "management/viewCustomer";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
            return "redirect:/mg-cus";
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/mg-cus";
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{department}-{customer}/merge")
    public String mergePage(ModelMap map, @ModelAttribute("customer") Long customerId, @ModelAttribute("department") Long departmentId, RedirectAttributes redAtt, @ModelAttribute("verificationData") FindVerifiedForm verificationData) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (employeeService.hasNoAccessToCustomerData(cud, customerId, departmentId)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-cus";
            }

            map.addAttribute("unverified_customer", customerService.findById(customerId));
            map.addAttribute("verified_customer", customerService.findCustomerByVerification(verificationData));
            map.addAttribute("confirmation_form", new ConfirmationForm());
            return "management/mergeCustomer";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
            redAtt.addAttribute("customer", customerId);
            redAtt.addAttribute("department", departmentId);
            return "redirect:/mg-cus/{department}-{customer}";
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/mg-cus";
        }
    }

    //Search page buttons
    @RequestMapping(value = "/search-departure", method = RequestMethod.POST)
    public String searchDepartureButton(@ModelAttribute("searchCustomersForm") @Valid SearchCustomersForm form, Errors err, RedirectAttributes redAtt) {
        try {
            redAtt.addFlashAttribute("searchCustomersForm", form);
            redAtt.addFlashAttribute("isArrival", false);

            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (employeeService.departmentAccess(cud, form.getPrimaryDepartment()).equals(HttpStatus.FORBIDDEN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-cus";
            }

            if (err.hasErrors()) {
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-cus";
            }

            redAtt.addFlashAttribute("results", customerService.findCustomersWithReservationCount(form, false));
            return "redirect:/mg-cus";
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/mg-cus";
        }
    }

    @RequestMapping(value = "/search-arrival", method = RequestMethod.POST)
    public String searchArrivalButton(@ModelAttribute("searchCustomersForm") @Valid SearchCustomersForm form, Errors err, RedirectAttributes redAtt) {
        try {
            redAtt.addFlashAttribute("searchCustomersForm", form);
            redAtt.addFlashAttribute("isArrival", true);

            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (employeeService.departmentAccess(cud, form.getPrimaryDepartment()).equals(HttpStatus.FORBIDDEN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-cus";
            }

            if (err.hasErrors()) {
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-cus";
            }

            redAtt.addFlashAttribute("results", customerService.findCustomersWithReservationCount(form, true));
            return "redirect:/mg-cus";
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/mg-cus";
        }
    }

    @RequestMapping(value = "/select", method = RequestMethod.POST)
    public String customerViewButton(RedirectAttributes redAtt, @RequestParam("select_button") Long customerId, @RequestParam("department") Long departmentId) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (employeeService.hasNoAccessToCustomerData(cud, customerId, departmentId)) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
            return "redirect:/mg-cus";
        }

        redAtt.addAttribute("customer", customerId);
        redAtt.addAttribute("department", departmentId);
        return "redirect:/mg-cus/{department}-{customer}";
    }

    //Manage customer page buttons
    @RequestMapping(method = RequestMethod.POST, value = "/{department}-{customer}/verify")
    public String verifyButton(RedirectAttributes redAtt, @ModelAttribute("verification_form") @Valid VerificationForm form, Errors err, @PathVariable("customer") Long customerId, @PathVariable("department") Long departmentId) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (employeeService.hasNoAccessToCustomerData(cud, customerId, departmentId)) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
            return "redirect:/mg-cus";
        }

        redAtt.addAttribute("department", departmentId);
        redAtt.addAttribute("customer", customerId);

        if (err.hasErrors()) {
            redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/mg-cus/{department}-{customer}";
        }

        HttpStatus status = verificationService.appendVerification(customerId, form);
        if (status.equals(HttpStatus.CREATED)) {
            redAtt.addFlashAttribute(MSG_KEY, "Success: Verification registered");
        } else if (status.equals(HttpStatus.EXPECTATION_FAILED)) {
            redAtt.addFlashAttribute(MSG_KEY, "Failure: Customer is already verified");
        } else if (status.equals(HttpStatus.CONFLICT)) {
            redAtt.addFlashAttribute(MSG_KEY, "Failure: Already linked to another account.");
        } else {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }

        return "redirect:/mg-cus/{department}-{customer}";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{department}-{customer}/unverify")
    public String unverifyConfirmButton(RedirectAttributes redAtt, @ModelAttribute("unverifyConfirmationForm") @Valid ConfirmationForm form, Errors err, @PathVariable("customer") Long customerId, @PathVariable("department") Long departmentId) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (employeeService.hasNoAccessToCustomerData(cud, customerId, departmentId)) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
            return "redirect:/mg-cus";
        }

        if (err.hasErrors()) {
            redAtt.addAttribute("department", departmentId);
            redAtt.addAttribute("customer", customerId);
            redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/mg-cus/{department}-{customer}";
        }

        HttpStatus status = customerService.unverifyCustomer(customerId);
        if (status.equals(HttpStatus.OK)) {
            redAtt.addAttribute("customer", customerId);
            redAtt.addAttribute("department", departmentId);
            redAtt.addFlashAttribute(MSG_KEY, "Success: Verification removed");
        } else if (status.equals(HttpStatus.CONFLICT)) {
            redAtt.addAttribute("customer", customerId);
            redAtt.addAttribute("department", departmentId);
            redAtt.addFlashAttribute(MSG_KEY, "Failure: Active reservations");
        } else {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/mg-cus";
        }
        return "redirect:/mg-cus/{department}-{customer}";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{department}-{customer}/merge")
    public String mergeCustomersButton(@ModelAttribute("findVerifiedForm") @Valid FindVerifiedForm form, Errors err, RedirectAttributes redAtt, @PathVariable("customer") Long customerId, @PathVariable("department") Long departmentId) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (employeeService.hasNoAccessToCustomerData(cud, customerId, departmentId)) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
            return "redirect:/mg-cus";
        }

        redAtt.addAttribute("department", departmentId);
        redAtt.addAttribute("customer", customerId);

        if (err.hasErrors()) {
            redAtt.addFlashAttribute(MSG_KEY, err.getFieldErrors().get(0).getDefaultMessage());
            return "redirect:/mg-cus/{department}-{customer}";
        }

        redAtt.addFlashAttribute("verificationData", form);
        return "redirect:/mg-cus/{department}-{customer}/merge";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{department}-{customer}/delete")
    public String deleteCustomerButton(RedirectAttributes redAtt, @ModelAttribute("deleteConfirmationForm") @Valid ConfirmationForm form, Errors err, @PathVariable("customer") Long customerId, @PathVariable("department") Long departmentId) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (employeeService.hasNoAccessToCustomerData(cud, customerId, departmentId)) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
            return "redirect:/mg-cus";
        }

        redAtt.addAttribute("department", departmentId);
        redAtt.addAttribute("customer", customerId);

        if (err.hasErrors()) {
            redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/mg-cus/{department}-{customer}";
        }

        HttpStatus status = userService.deleteUser(customerId);

        if (status.equals(HttpStatus.OK)) {
            redAtt.addFlashAttribute(MSG_KEY, "Success: Data removed");
        } else if (status.equals(HttpStatus.CONFLICT)) {
            redAtt.addFlashAttribute(MSG_KEY, "Failure: Active reservations");
        } else {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-cus/{department}-{customer}";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{department}-{customer}/contact")
    public String changeContactButton(RedirectAttributes redAtt, @ModelAttribute("changeContactForm") @Valid ChangeContactForm form, Errors err, @PathVariable("customer") Long customerId, @PathVariable("department") Long departmentId) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (employeeService.hasNoAccessToCustomerData(cud, customerId, departmentId)) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
            return "redirect:/mg-cus";
        }

        redAtt.addAttribute("department", departmentId);
        redAtt.addAttribute("customer", customerId);

        if (err.hasErrors()) {
            redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/mg-cus/{department}-{customer}";
        }

        HttpStatus status = userService.changeContact(form.getContactNumber(), customerId);

        if (status.equals(HttpStatus.ACCEPTED)) {
            redAtt.addFlashAttribute(MSG_KEY, "Success: Data changed");
        } else if (status.equals(HttpStatus.NOT_FOUND)) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
            return "redirect:/mg-cus";
        } else {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-cus/{department}-{customer}";
    }

    //Merge page buttons
    @RequestMapping(method = RequestMethod.POST, value = "/{department}-{customer}/merge/{target}")
    public String mergeConfirmButton(RedirectAttributes redAtt, @ModelAttribute("confirmation_form") @Valid ConfirmationForm form, Errors errors, @PathVariable("customer") Long customerId, @PathVariable("department") Long departmentId, @PathVariable("target") Long verifiedCustomer) {
        if (errors.hasErrors()) {
            redAtt.addAttribute("department", departmentId);
            redAtt.addAttribute("customer", customerId);

            redAtt.addFlashAttribute(MSG_KEY, errors.getAllErrors().get(0));
            return "redirect:/mg-cus/{department}-{customer}";
        }

        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (employeeService.hasNoAccessToCustomerData(cud, customerId, departmentId)) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
            return "redirect:/mg-cus";
        }

        HttpStatus status = customerService.mergeCustomer(customerId, verifiedCustomer);
        if (status.equals(HttpStatus.OK)) {
            redAtt.addFlashAttribute(MSG_KEY, "Success: Accounts merged");
        } else {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }

        redAtt.addAttribute("customer", customerId);
        redAtt.addAttribute("department", departmentId);
        return "redirect:/mg-cus/{department}-{customer}";
    }
}
