package com.sda.carrental.web.mvc.management;

import com.sda.carrental.model.property.Department;
import com.sda.carrental.model.users.Coordinator;
import com.sda.carrental.model.users.Employee;
import com.sda.carrental.model.users.Manager;
import com.sda.carrental.model.users.User;
import com.sda.carrental.service.*;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.users.SearchEmployeesForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

import static com.sda.carrental.model.users.User.Roles.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mg-emp")
public class ManageEmployeesController {
    private final DepartmentService departmentService;
    private final EmployeeService employeeService;
    private final ManagerService managerService;
    private final CoordinatorService coordinatorService;

    private final String MSG_KEY = "message";
    private final String MSG_ACCESS_REJECTED = "Failure: Access rejected";
    private final String MSG_GENERIC_EXCEPTION = "Failure: An unexpected error occurred";
    private final String MSG_NO_RESOURCE = "Failure: Resource not found";

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String searchEmployeesPage(ModelMap map, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<Department> employeeDepartments = departmentService.getDepartmentsByUserContext(cud);
            map.addAttribute("departments", employeeDepartments);
            map.addAttribute("roles", List.of(User.Roles.valueOf(ROLE_EMPLOYEE.name()), User.Roles.valueOf(ROLE_MANAGER.name()), User.Roles.valueOf(ROLE_COORDINATOR.name())));
            map.addAttribute("searchEmployeesForm", map.getOrDefault("searchEmployeesForm", new SearchEmployeesForm()));
            map.addAttribute("isEmpty", map.getOrDefault("isEmpty", true));

            return "management/searchEmployees";
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/";
        }
    }

    //Search page buttons
    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public String searchEmployeesButton(@ModelAttribute("searchEmployeesForm") @Valid SearchEmployeesForm form, Errors err, RedirectAttributes redAtt) {
        try {
            redAtt.addFlashAttribute("searchEmployeesForm", form);

            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (departmentService.departmentAccess(cud, form.getDepartment()).equals(HttpStatus.FORBIDDEN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-emp";
            }

            if (err.hasErrors()) {
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-emp";
            }

            List<Employee> eList = Collections.emptyList();
            List<Manager> mList = Collections.emptyList();
            List<Coordinator> cList = Collections.emptyList();

            if (form.getRole().isEmpty() || form.getRole().equals(ROLE_EMPLOYEE.name())) eList = employeeService.findByForm(form);
            if (form.getRole().isEmpty() || form.getRole().equals(ROLE_MANAGER.name())) mList = managerService.findByForm(form);
            if (form.getRole().isEmpty() || form.getRole().equals(ROLE_COORDINATOR.name())) cList = coordinatorService.findByForm(form);


            redAtt.addFlashAttribute("e_results", eList);
            redAtt.addFlashAttribute("m_results", mList);
            redAtt.addFlashAttribute("c_results", cList);

            redAtt.addFlashAttribute("isEmpty", eList.isEmpty() && mList.isEmpty() && cList.isEmpty());
            return "redirect:/mg-emp";
        } catch (RuntimeException e) {
            e.printStackTrace();
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/mg-emp";
        }
    }
}
