package com.sda.carrental.web.mvc.management;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.enums.Country;
import com.sda.carrental.model.operational.Retrieve;
import com.sda.carrental.model.property.Department;
import com.sda.carrental.model.property.PaymentDetails;
import com.sda.carrental.service.DepartmentService;
import com.sda.carrental.service.PaymentDetailsService;
import com.sda.carrental.service.RetrieveService;
import com.sda.carrental.service.UserService;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.SearchDepositsForm;
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

@Controller
@RequiredArgsConstructor
@RequestMapping("/mg-depo")
public class ManageDepositController {
    private final RetrieveService retrieveService;
    private final DepartmentService departmentService;
    private final UserService userService;
    private final PaymentDetailsService paymentDetailsService;

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String searchDepositsPage(ModelMap map, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            map.addAttribute("results", map.getOrDefault("results", retrieveService.replaceDatesWithDeadlines(retrieveService.findAllUnresolvedByUserContext(cud))));

            List<Department> employeeDepartments = departmentService.getDepartmentsByUserContext(cud);
            map.addAttribute("countries", Country.values());
            map.addAttribute("departments", departmentService.findAllWhereCountry(employeeDepartments.get(0).getCountry()));

            map.addAttribute("searchDepositsForm", map.getOrDefault("searchDepositsForm", new SearchDepositsForm()));

            return "management/searchDeposits";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again.");
            return "redirect:/";
        }
    }

    //Search page buttons
    @RequestMapping(method = RequestMethod.POST, value = "/search")
    public String searchDepositsButton(@ModelAttribute("searchCustomersForm") @Valid SearchDepositsForm form, Errors err, RedirectAttributes redAtt) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        redAtt.addFlashAttribute("searchDepositsForm", form);

        if (err.hasErrors()) {
            redAtt.addFlashAttribute("message", err.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/mg-depo";
        }

        redAtt.addFlashAttribute("results", retrieveService.replaceDatesWithDeadlines(retrieveService.findUnresolvedByUserContextAndForm(cud, form)));
        return "redirect:/mg-depo";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/check")
    public String checkDepositButton(RedirectAttributes redAtt, @RequestParam("check_button") Long retrieveId) {
        redAtt.addAttribute("retrieve", retrieveId);
        return "redirect:/mg-depo/{retrieve}";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{retrieve}")
    public String viewDepositPage(@PathVariable(value = "retrieve") Long retrieveId, ModelMap map, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Retrieve retrieve = retrieveService.findById(retrieveId);
            PaymentDetails paymentDetails = paymentDetailsService.getOptionalPaymentDetails(retrieve.getReservation()).orElseThrow(ResourceNotFoundException::new);
            if (departmentService.departmentAccess(cud, retrieve.getReservation().getDepartmentBack().getId()).equals(HttpStatus.FORBIDDEN)) {
                redAtt.addFlashAttribute("message", "Inaccessible department.");
                return "redirect:/";
            }

            map.addAttribute("retrieve", retrieve);
            map.addAttribute("deadline", retrieveService.replaceDatesWithDeadlines(List.of(retrieve)).get(0).getDateTo());
            map.addAttribute("payment_details", paymentDetails);
            map.addAttribute("secured_deposit", paymentDetailsService.calculateSecuredDeposit(paymentDetails));
            map.addAttribute("released_deposit", paymentDetailsService.calculateReleasedDeposit(paymentDetails));
            map.addAttribute("rent_employee", userService.findById(retrieve.getRent().getEmployeeId()));
            map.addAttribute("retrieve_employee", userService.findById(retrieve.getEmployeeId()));
            return "management/viewDeposit";
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again.");
            return "redirect:/";
        }
    }
}
