package com.sda.carrental.web.mvc.management;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.enums.Role;
import com.sda.carrental.model.property.department.Department;
import com.sda.carrental.service.*;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.property.departments.RefreshOverviewForm;
import com.sda.carrental.web.mvc.form.property.departments.RefreshStatisticsForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/overview")
public class OverviewController {
    private final DepartmentService departmentService;
    private final EmployeeService employeeService;
    private final PaymentDetailsService paymentDetailsService;
    private final RentService rentService;
    private final CarBaseService carBaseService;
    private final ReservationService reservationService;
    private final RetrieveService retrieveService;

    private final String MSG_KEY = "message";
    private final String MSG_GENERIC_EXCEPTION = "Failure: An unexpected error occurred";
    private final String MSG_NO_RESOURCE = "Failure: Resource not found";
    private final String MSG_ACCESS_REJECTED = "Failure: Access rejected";

    // Redirections
    @RequestMapping(method = RequestMethod.GET)
    public String requestOverviewPage(RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<Department> departments = employeeService.getDepartmentsByUserContext(cud);
            return "redirect:/overview/" + departments.get(0).getId();
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/";
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/statistics")
    public String requestStatisticsPage(RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (employeeService.hasMinimumAuthority(cud, Role.ROLE_COORDINATOR)) {
                List<Department> departments = employeeService.getDepartmentsByUserContext(cud);
                return "redirect:/overview/statistics/" + departments.get(0).getId();
            }
            redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/";
    }

    // Pages
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

            map.addAttribute("cars_info", carBaseService.getStatistics(departmentId));

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

    @RequestMapping(method = RequestMethod.GET, value = "/statistics/{departmentId}")
    public String viewStatisticsPage(@PathVariable("departmentId") Long departmentId, ModelMap map, RedirectAttributes redAtt, HttpSession session) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (employeeService.departmentAccess(cud, departmentId).equals(HttpStatus.FORBIDDEN) || !employeeService.hasMinimumAuthority(cud, Role.ROLE_COORDINATOR)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/";
            }

            List<Department> departments = employeeService.getDepartmentsByUserContext(cud);

            LocalDate dateFrom = session.getAttribute("stat_from") != null ? (LocalDate) session.getAttribute("stat_from") : LocalDate.now().minusMonths(1);
            LocalDate dateTo = session.getAttribute("stat_to") != null ? (LocalDate) session.getAttribute("stat_to") : LocalDate.now();

            Map<String, Double> departmentStatistics = new HashMap<>();
            paymentDetailsService.mapStatistics(departmentStatistics, departmentId, dateFrom, dateTo);
            reservationService.mapStatistics(departmentStatistics, departmentId, dateFrom, dateTo);
            retrieveService.mapStatistics(departmentStatistics, departmentId, dateFrom, dateTo);

            map.addAttribute("statistics", departmentStatistics);

            map.addAttribute("departments", departments);
            map.addAttribute("department", departmentService.findById(departmentId));
            map.addAttribute("refresh_form", map.getOrDefault("refresh_form", new RefreshStatisticsForm(departmentId, dateFrom, dateTo)) );
            return "management/viewStatistics";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (RuntimeException e) {
            e.printStackTrace();
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/";
    }

    // Overview page buttons
    @RequestMapping(method = RequestMethod.POST, value = "/refresh")
    public String refreshOverviewButton(@ModelAttribute(value = "refresh_form") @Valid RefreshOverviewForm form, Errors err, RedirectAttributes redAtt) {
        if (err.hasErrors()) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/";
        }
        return "redirect:/overview/" + form.getDepartmentId();
    }

    // Statistics page buttons
    @RequestMapping(method = RequestMethod.POST, value = "/statistics/refresh")
    public String refreshStatisticsButton(@ModelAttribute(value = "refresh_form") @Valid RefreshStatisticsForm form, Errors err, RedirectAttributes redAtt, HttpSession session) {
        if (err.hasErrors()) {
            redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/overview/statistics/" + form.getPreviousId();
        }
        session.setAttribute("stat_from", form.getDateFrom());
        session.setAttribute("stat_to", form.getDateTo());
        return "redirect:/overview/statistics/" + form.getDepartmentId();
    }
}
