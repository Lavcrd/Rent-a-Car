package com.sda.carrental.web.mvc;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.users.Customer;
import com.sda.carrental.model.users.auth.Verification;
import com.sda.carrental.service.*;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.ConfirmationForm;
import com.sda.carrental.web.mvc.form.SearchCustomerForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
                map.addAttribute("verification", new Verification(customerId, "N/D", "N/D"));
            }
            return "management/viewCustomer";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again.");
            return "redirect:/mg-cus";
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
}
