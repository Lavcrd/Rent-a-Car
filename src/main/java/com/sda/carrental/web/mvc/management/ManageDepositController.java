package com.sda.carrental.web.mvc.management;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.enums.Country;
import com.sda.carrental.model.property.Department;
import com.sda.carrental.service.DepartmentService;
import com.sda.carrental.service.RetrieveService;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.SearchDepositsForm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mg-depo")
public class ManageDepositController {
    private final RetrieveService retrieveService;
    private final DepartmentService departmentService;

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
}
