package com.sda.carrental.web.mvc.management;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.operational.Retrieve;
import com.sda.carrental.model.property.Department;
import com.sda.carrental.service.DepartmentService;
import com.sda.carrental.service.RetrieveService;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.SearchDepositsForm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
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
            List<Retrieve> results = retrieveService.findAllUnresolvedByUserContext(cud);
            map.addAttribute("results", retrieveService.replaceDatesWithDeadlines(results));

            List<Department> employeeDepartments = departmentService.getDepartmentsByUserContext(cud);
            map.addAttribute("departments", employeeDepartments);
            map.addAttribute("departmentsCountry", departmentService.findAllWhereCountry(employeeDepartments.get(0).getCountry()));

            if (!map.containsKey("searchDepositsForm")) {
                map.addAttribute("searchDepositsForm", new SearchDepositsForm(LocalDate.now().minusWeeks(1), LocalDate.now().plusWeeks(1)));
            }

            return "management/searchDeposits";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again.");
            return "redirect:/";
        }
    }
}
