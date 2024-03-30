package com.sda.carrental.web.mvc.management;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.enums.Role;
import com.sda.carrental.model.property.department.Department;
import com.sda.carrental.service.DepartmentService;
import com.sda.carrental.service.EmployeeService;
import com.sda.carrental.service.RentService;
import com.sda.carrental.service.ReservationService;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.property.departments.RefreshOverviewForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/overview")
public class OverviewController {
    private final DepartmentService departmentService;
    private final EmployeeService employeeService;
    private final RentService rentService;
    private final ReservationService reservationService;

    private final String MSG_KEY = "message";
    private final String MSG_GENERIC_EXCEPTION = "Failure: An unexpected error occurred";
    private final String MSG_NO_RESOURCE = "Failure: Resource not found";
    private final String MSG_ACCESS_REJECTED = "Failure: Access rejected";

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String requestOverviewPage(ModelMap map, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<Department> departments = employeeService.getDepartmentsByUserContext(cud);
            return "redirect:/overview/" + departments.get(0).getId();
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/";
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{departmentId}")
    public String viewOverviewPage(@PathVariable("departmentId") Long departmentId, ModelMap map, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (employeeService.departmentAccess(cud, departmentId).equals(HttpStatus.FORBIDDEN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/";
            }

            List<Department> departments = employeeService.getDepartmentsByUserContext(cud);

            map.addAttribute("incoming_rents", rentService.findIncomingByDepartment(departmentId));
            map.addAttribute("incoming_reservations", reservationService.findExpectedDeparturesByDepartment(departmentId));

            map.addAttribute("hasAuthority", employeeService.hasMinimumAuthority(cud, Role.ROLE_COORDINATOR));
            map.addAttribute("departments", departments);
            map.addAttribute("department", departmentService.findById(departmentId));
            map.addAttribute("refresh_form", new RefreshOverviewForm(departmentId));
            return "management/viewOverview";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/";
    }

    // Overview page buttons
    @RequestMapping(method = RequestMethod.POST, value = "/refresh")
    public String refreshDepartmentButton(@ModelAttribute(value = "refresh_form") RefreshOverviewForm form, Errors err, RedirectAttributes redAtt) {
        if (err.hasErrors()) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/";
        }
        return "redirect:/overview/" + form.getDepartmentId();
    }
}
