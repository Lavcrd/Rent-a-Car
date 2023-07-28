package com.sda.carrental.web.mvc.management;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.enums.Country;
import com.sda.carrental.model.users.Customer;
import com.sda.carrental.model.users.auth.Verification;
import com.sda.carrental.service.*;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.ConfirmationForm;
import com.sda.carrental.web.mvc.form.SearchCustomerForm;
import com.sda.carrental.web.mvc.form.VerificationForm;
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
            }
            if (!customer.getStatus().equals(Customer.CustomerStatus.STATUS_DELETED)) {
                map.addAttribute("is_deleted", false);
                map.addAttribute("deleteConfirmationForm", new ConfirmationForm());
            } else {
                map.addAttribute("is_deleted", true);
            }
            return "management/viewCustomer";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again.");
            return "redirect:/mg-cus";
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/verify")
    public String verifyPage(ModelMap map, @ModelAttribute("customer") Long customerId, @ModelAttribute("department") Long departmentId, RedirectAttributes redAtt) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userService.hasNoAccessToUserData(cud, customerId, departmentId)) {
            redAtt.addFlashAttribute("message", "Access rejected.");
            return "redirect:/mg-cus";
        }

        map.addAttribute("verification_form", new VerificationForm(customerId));
        map.addAttribute("countries", Country.values());
        return "management/verifyCustomer";
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
    public String verifyButton(RedirectAttributes redAtt, @RequestParam("customer") Long customerId, @RequestParam("department") Long departmentId) {
        redAtt.addFlashAttribute("customer", customerId);
        redAtt.addFlashAttribute("department", departmentId);
        return "redirect:/mg-cus/verify";
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

        HttpStatus status = verificationService.deleteVerification(customerId);
        if (status.equals(HttpStatus.OK)) {
            redAtt.addAttribute("customer", customerId);
            redAtt.addFlashAttribute("department", departmentId);
            redAtt.addFlashAttribute("message", "Verification successfully removed");
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

    @RequestMapping(method = RequestMethod.POST, value = "/mg-ren")
    public String manageRentalsButton(RedirectAttributes redAtt, @RequestParam("customer") Long customerId, @RequestParam("department") Long departmentId) {
        redAtt.addAttribute("customer", customerId);
        redAtt.addFlashAttribute("department", departmentId);
        return "redirect:/mg-ren/{customer}";
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

    //Verification page buttons
    @RequestMapping(method = RequestMethod.POST, value = "/verify/create")
    public String verifyConfirmButton(ModelMap map, RedirectAttributes redAtt, @ModelAttribute("verification_form") @Valid VerificationForm form, Errors errors, @RequestParam("department") Long departmentId) {
        if (errors.hasErrors()) {
            map.addAttribute("department", departmentId);
            map.addAttribute("customer", form.getCustomerId());
            map.addAttribute("verification_form", form);
            map.addAttribute("countries", Country.values());
            return "management/verifyCustomer";
        }

        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (userService.hasNoAccessToUserData(cud, form.getCustomerId(), departmentId)) {
            redAtt.addFlashAttribute("message", "Access rejected.");
            return "redirect:/mg-cus";
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
            redAtt.addFlashAttribute("customer", form.getCustomerId());

            redAtt.addFlashAttribute("message", "The identification is already linked to another account.");
            return "redirect:/mg-cus/verify";
        }

        redAtt.addAttribute("customer", form.getCustomerId());
        redAtt.addFlashAttribute("department", departmentId);
        return "redirect:/mg-cus/{customer}";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/verify/back")
    public String verifyBackButton(RedirectAttributes redAtt, @ModelAttribute("verification_form") VerificationForm form, @RequestParam("department") Long departmentId) {
        redAtt.addAttribute("customer", form.getCustomerId());
        redAtt.addFlashAttribute("department", departmentId);
        return "redirect:/mg-cus/{customer}";
    }
}
