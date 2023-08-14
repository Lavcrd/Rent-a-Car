package com.sda.carrental.web.mvc.management;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.enums.Country;
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
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mg-cus")
public class ManageCustomersController {
    private final DepartmentService departmentService;
    private final UserService userService;
    private final CustomerService customerService;
    private final VerificationService verificationService;

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String searchCustomerPage(ModelMap map, RedirectAttributes redAtt) {
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
    public String manageCustomerPage(ModelMap map, RedirectAttributes redAtt, @PathVariable(value = "customer") Long customerId, @ModelAttribute("department") Long departmentId) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (userService.hasNoAccessToUserData(cud, customerId, departmentId)) {
                redAtt.addFlashAttribute("message", "Access rejected.");
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
                map.addAttribute("verification", new Verification(customerId, Country.COUNTRY_NONE, "N/D", "N/D"));
                map.addAttribute("countries", Country.values());
                map.addAttribute("findVerifiedForm", new FindVerifiedForm());
                map.addAttribute("verification_form", new VerificationForm(customerId));
            }
            if (!customer.getStatus().equals(Customer.CustomerStatus.STATUS_DELETED)) {
                map.addAttribute("is_deleted", false);
                map.addAttribute("deleteConfirmationForm", new ConfirmationForm());
                map.addAttribute("changeContactForm", new ChangeContactForm());
            } else {
                map.addAttribute("is_deleted", true);
            }
            return "management/viewCustomer";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again.");
            return "redirect:/mg-cus";
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/merge")
    public String mergePage(ModelMap map, @ModelAttribute("customer") Long customerId, @ModelAttribute("department") Long departmentId, RedirectAttributes redAtt, @ModelAttribute("verificationData") FindVerifiedForm verificationData) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (userService.hasNoAccessToUserData(cud, customerId, departmentId)) {
                redAtt.addFlashAttribute("message", "Access rejected.");
                return "redirect:/mg-cus";
            }

            map.addAttribute("unverified_customer", customerService.findById(customerId));
            map.addAttribute("verified_customer", customerService.findCustomerByVerification(verificationData.getCountry(), verificationData.getPersonalId()));
            map.addAttribute("confirmation_form", new ConfirmationForm());
            return "management/mergeCustomer";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "No verified account found");
            redAtt.addAttribute("customer", customerId);
            redAtt.addFlashAttribute("department", departmentId);
            return "redirect:/mg-cus/{customer}";
        }
    }

    //Search customers page buttons
    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public String customerSearchButton(@ModelAttribute("searchCustomerForm") SearchCustomerForm customersData, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (departmentService.departmentAccess(cud, customersData.getDepartmentId()).equals(HttpStatus.FORBIDDEN)) {
                redAtt.addFlashAttribute("message", "Incorrect data provided. Search rejected.");
                return "redirect:/mg-cus";
            }

            redAtt.addFlashAttribute("searchCustomerForm", customersData);
            redAtt.addFlashAttribute("departments", departmentService.getDepartmentsByRole(cud));

            redAtt.addFlashAttribute("customers", customerService.findCustomersByDepartmentAndName(customersData));
            return "redirect:/mg-cus";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again.");
            return "redirect:/";
        }
    }

    @RequestMapping(value = "/select", method = RequestMethod.POST)
    public String customerViewButton(RedirectAttributes redAtt, @RequestParam("select_button") Long customerId, @RequestParam("department") Long departmentId) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userService.hasNoAccessToUserData(cud, customerId, departmentId)) {
            redAtt.addFlashAttribute("message", "Access rejected.");
            return "redirect:/mg-cus";
        }

        redAtt.addAttribute("customer", customerId);
        redAtt.addFlashAttribute("department", departmentId);
        return "redirect:/mg-cus/{customer}";
    }

    //Manage customer page buttons
    @RequestMapping(method = RequestMethod.POST, value = "/verify")
    public String verifyButton(RedirectAttributes redAtt, @ModelAttribute("verification_form") @Valid VerificationForm form, Errors err, @RequestParam("department") Long departmentId) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userService.hasNoAccessToUserData(cud, form.getCustomerId(), departmentId)) {
            redAtt.addFlashAttribute("message", "Access rejected.");
            return "redirect:/mg-cus";
        }

        if (err.hasErrors()) {
            redAtt.addFlashAttribute("department", departmentId);
            redAtt.addAttribute("customer", form.getCustomerId());
            redAtt.addFlashAttribute("message", err.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/mg-cus/{customer}";
        }

        HttpStatus status = verificationService.createVerification(form);
        if (status.equals(HttpStatus.CREATED)) {
            redAtt.addFlashAttribute("message", "Verification successfully registered.");
        } else if (status.equals(HttpStatus.EXPECTATION_FAILED)) {
            redAtt.addFlashAttribute("message", "Customer is already verified.");
        } else if (status.equals(HttpStatus.NOT_FOUND)) {
            redAtt.addFlashAttribute("message", "Customer not found. Try again.");
            return "redirect:/mg-cus";
        } else if (status.equals(HttpStatus.CONFLICT)) {
            redAtt.addFlashAttribute("department", departmentId);
            redAtt.addAttribute("customer", form.getCustomerId());

            redAtt.addFlashAttribute("message", "The identification is already linked to another account.");
            return "redirect:/mg-cus/{customer}";
        }

        redAtt.addAttribute("customer", form.getCustomerId());
        redAtt.addFlashAttribute("department", departmentId);
        return "redirect:/mg-cus/{customer}";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/unverify")
    public String unverifyConfirmButton(RedirectAttributes redAtt, @ModelAttribute("unverifyConfirmationForm") @Valid ConfirmationForm form, Errors err, @RequestParam("customer") Long customerId, @RequestParam("department") Long departmentId) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userService.hasNoAccessToUserData(cud, customerId, departmentId)) {
            redAtt.addFlashAttribute("message", "Access rejected.");
            return "redirect:/mg-cus";
        }

        if (err.hasErrors()) {
            redAtt.addFlashAttribute("department", departmentId);
            redAtt.addAttribute("customer", customerId);
            redAtt.addFlashAttribute("message", err.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/mg-cus/{customer}";
        }

        HttpStatus status = customerService.unverifyCustomer(customerId);
        if (status.equals(HttpStatus.OK)) {
            redAtt.addAttribute("customer", customerId);
            redAtt.addFlashAttribute("department", departmentId);
            redAtt.addFlashAttribute("message", "Verification successfully removed");
        } else if (status.equals(HttpStatus.CONFLICT)) {
            redAtt.addAttribute("customer", customerId);
            redAtt.addFlashAttribute("department", departmentId);
            redAtt.addFlashAttribute("message", "Verification removal unsuccessful: Active reservations");
        } else {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again.");
            return "redirect:/mg-cus";
        }
        return "redirect:/mg-cus/{customer}";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/mg-res")
    public String manageReservationsButton(RedirectAttributes redAtt, @RequestParam("customer") Long customerId, @RequestParam("department") Long departmentId) {
        redAtt.addAttribute("customer", customerId);
        redAtt.addFlashAttribute("department", departmentId);
        return "redirect:/mg-res/{customer}";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/merge")
    public String mergeCustomersButton(@ModelAttribute("findVerifiedForm") @Valid FindVerifiedForm form, Errors err, RedirectAttributes redAtt, @RequestParam("customer") Long customerId, @RequestParam("department") Long departmentId) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userService.hasNoAccessToUserData(cud, customerId, departmentId)) {
            redAtt.addFlashAttribute("message", "Access rejected.");
            return "redirect:/mg-cus";
        }

        redAtt.addFlashAttribute("department", departmentId);
        if (err.hasErrors()) {
            redAtt.addFlashAttribute("message", err.getFieldErrors().get(0).getDefaultMessage());
            redAtt.addAttribute("customer", customerId);
            return "redirect:/mg-cus/{customer}";
        }

        redAtt.addFlashAttribute("customer", customerId);
        redAtt.addFlashAttribute("verificationData", form);
        return "redirect:/mg-cus/merge";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/delete")
    public String deleteCustomerButton(RedirectAttributes redAtt, @ModelAttribute("deleteConfirmationForm") @Valid ConfirmationForm form, Errors err, @RequestParam("customer") Long customerId, @RequestParam("department") Long departmentId) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userService.hasNoAccessToUserData(cud, customerId, departmentId)) {
            redAtt.addFlashAttribute("message", "Access rejected.");
            return "redirect:/mg-cus";
        }

        redAtt.addFlashAttribute("department", departmentId);
        redAtt.addAttribute("customer", customerId);

        if (err.hasErrors()) {
            redAtt.addFlashAttribute("message", err.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/mg-cus/{customer}";
        }

        HttpStatus status = userService.deleteUser(customerId);

        if (status.equals(HttpStatus.OK)) {
            redAtt.addFlashAttribute("message", "Data successfully removed");
        } else if (status.equals(HttpStatus.CONFLICT)) {
            redAtt.addFlashAttribute("message", "Removal unsuccessful: Active reservations");
        } else {
            redAtt.addFlashAttribute("message", "Removal unsuccessful: Temporary error");
        }
        return "redirect:/mg-cus/{customer}";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/contact")
    public String changeContactButton(RedirectAttributes redAtt, @ModelAttribute("changeContactForm") @Valid ChangeContactForm form, Errors err, @RequestParam("customer") Long customerId, @RequestParam("department") Long departmentId) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userService.hasNoAccessToUserData(cud, customerId, departmentId)) {
            redAtt.addFlashAttribute("message", "Access rejected.");
            return "redirect:/mg-cus";
        }

        redAtt.addFlashAttribute("department", departmentId);
        redAtt.addAttribute("customer", customerId);

        if (err.hasErrors()) {
            redAtt.addFlashAttribute("message", err.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/mg-cus/{customer}";
        }

        HttpStatus status = customerService.changeContact(form.getContactNumber(), customerId);

        if (status.equals(HttpStatus.ACCEPTED)) {
            redAtt.addFlashAttribute("message", "Data successfully changed");
        } else if (status.equals(HttpStatus.NOT_FOUND)) {
            redAtt.addFlashAttribute("message", "Change unsuccessful: Customer not found");
            return "redirect:/mg-cus";
        } else {
            redAtt.addFlashAttribute("message", "Change unsuccessful: Temporary error");
        }
        return "redirect:/mg-cus/{customer}";
    }

    //Merge page buttons
    @RequestMapping(method = RequestMethod.POST, value = "/merge/proceed")
    public String mergeConfirmButton(RedirectAttributes redAtt, @ModelAttribute("confirmation_form") @Valid ConfirmationForm form, Errors errors, @RequestParam("customer") Long customerId, @RequestParam("department") Long departmentId, @RequestParam("target") Long verifiedCustomer) {
        if (errors.hasErrors()) {
            redAtt.addFlashAttribute("department", departmentId);
            redAtt.addAttribute("customer", customerId);

            redAtt.addFlashAttribute("message", "Incorrect password. Please repeat process again.");
            return "redirect:/mg-cus/{customer}";
        }

        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userService.hasNoAccessToUserData(cud, customerId, departmentId)) {
            redAtt.addFlashAttribute("message", "Access rejected.");
            return "redirect:/mg-cus";
        }

        HttpStatus status = customerService.mergeCustomer(customerId, verifiedCustomer);
        if (status.equals(HttpStatus.OK)) {
            redAtt.addFlashAttribute("message", "Accounts successfully merged");
        } else {
            redAtt.addFlashAttribute("message", "Unexpected error. Action cancelled.");
        }

        redAtt.addAttribute("customer", customerId);
        redAtt.addFlashAttribute("department", departmentId);
        return "redirect:/mg-cus/{customer}";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/merge/back")
    public String mergeBackButton(RedirectAttributes redAtt, @ModelAttribute("confirmation_form") ConfirmationForm form, @RequestParam("customer") Long customerId, @RequestParam("department") Long departmentId) {
        redAtt.addAttribute("customer", customerId);
        redAtt.addFlashAttribute("department", departmentId);
        return "redirect:/mg-cus/{customer}";
    }
}
