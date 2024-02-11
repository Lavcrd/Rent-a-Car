package com.sda.carrental.web.mvc.management;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.enums.Role;
import com.sda.carrental.model.property.Department;
import com.sda.carrental.model.users.Employee;
import com.sda.carrental.service.*;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.common.ConfirmationForm;
import com.sda.carrental.web.mvc.form.users.ChangePasswordForm;
import com.sda.carrental.web.mvc.form.users.employee.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mg-emp")
public class ManageEmployeesController {
    private final EmployeeService employeeService;
    private final CredentialsService credentialsService;

    private final String MSG_KEY = "message";
    private final String MSG_ACCESS_REJECTED = "Failure: Access rejected";
    private final String MSG_GENERIC_EXCEPTION = "Failure: An unexpected error occurred";
    private final String MSG_NO_RESOURCE = "Failure: Resource not found";

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String searchEmployeesPage(ModelMap map, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<Department> employeeDepartments = employeeService.getDepartmentsByUserContext(cud);
            map.addAttribute("departments", employeeDepartments);
            map.addAttribute("roles", employeeService.getEmployeeEnums());
            map.addAttribute("searchEmployeesForm", map.getOrDefault("searchEmployeesForm", new SearchEmployeesForm()));
            map.addAttribute("e_results", map.getOrDefault("e_results", Collections.emptyList()));

            map.addAttribute("register_form", map.getOrDefault("register_form", new RegisterEmployeeForm()));

            return "management/searchEmployees";
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/";
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{employee}")
    public String viewEmployeePage(ModelMap map, RedirectAttributes redAtt, @PathVariable(value = "employee") Long employeeId) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Employee employee = employeeService.findById(employeeId);
            if (employeeService.departmentAccess(cud, employee.getDepartments().get(0).getId()).equals(HttpStatus.FORBIDDEN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-emp";
            }

            map.addAttribute("employee", employee);
            map.addAttribute("isExpired", !employee.getTerminationDate().isAfter(LocalDate.now()));

            map.addAttribute("roles", employeeService.getEmployeeEnums());
            map.addAttribute("details_form", map.getOrDefault("details_form", new UpdateEmployeeForm(employee)));

            map.addAttribute("password_form", new ChangePasswordForm());

            map.addAttribute("lock_form", new UpdateLockForm(LocalDate.now()));

            map.addAttribute("departments_form", new UpdateDepartmentsForm(employee.getDepartments()));
            map.addAttribute("departmentList", employeeService.getDepartmentsByUserContext(cud));

            map.addAttribute("hasPresence", employeeService.hasPresence(employee.getId()));
            map.addAttribute("delete_form", new ConfirmationForm());

            return "management/viewEmployee";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
            return "redirect:/mg-emp";
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/mg-emp";
        }
    }

    //Search page buttons
    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public String searchEmployeesButton(@ModelAttribute("searchEmployeesForm") @Valid SearchEmployeesForm form, Errors err, RedirectAttributes redAtt) {
        try {
            redAtt.addFlashAttribute("searchEmployeesForm", form);

            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (employeeService.departmentAccess(cud, form.getDepartment()).equals(HttpStatus.FORBIDDEN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-emp";
            }

            if (err.hasErrors()) {
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-emp";
            }

            redAtt.addFlashAttribute("e_results", employeeService.findByForm(form));

            return "redirect:/mg-emp";
        } catch (RuntimeException e) {
            e.printStackTrace();
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/mg-emp";
        }
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String employeeRegisterButton(@ModelAttribute("register_form") @Valid RegisterEmployeeForm form, Errors err, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (!employeeService.hasMinimumAuthority(cud, Role.ROLE_MANAGER)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-emp";
            }

            if (err.hasErrors()) {
                redAtt.addFlashAttribute("register_form", form);
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-emp";
            }

            HttpStatus status = employeeService.register(form);

            if (status.equals(HttpStatus.CREATED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Success: Employee created.");
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }

            return "redirect:/mg-emp";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-emp";
    }


    //Employee page buttons
    @RequestMapping(value = "/{employee}/update-details", method = RequestMethod.POST)
    public String employeeDetailsButton(@ModelAttribute("details_form") @Valid UpdateEmployeeForm form, Errors err, RedirectAttributes redAtt, @PathVariable(value = "employee") Long employeeId) {
        try {
            redAtt.addFlashAttribute("details_form", form);

            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Employee employee = employeeService.findById(employeeId);
            if (!employeeService.isSupervisorOf(cud, employee)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-emp/{employee}";
            }

            if (err.hasErrors()) {
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-emp/{employee}";
            }

            HttpStatus status = employeeService.updateDetails(employee, form);

            if (status.equals(HttpStatus.OK)) {
                redAtt.addFlashAttribute(MSG_KEY, "Success: Employee's data has been updated.");
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }

            return "redirect:/mg-emp/{employee}";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-emp";
    }

    @RequestMapping(value = "/{employee}/update-password", method = RequestMethod.POST)
    public String employeePasswordButton(@ModelAttribute("password_form") @Valid ChangePasswordForm form, Errors err, RedirectAttributes redAtt, @PathVariable(value = "employee") Long employeeId) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Employee employee = employeeService.findById(employeeId);
            if (!employeeService.isSupervisorOf(cud, employee)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-emp/{employee}";
            }

            if (err.hasErrors()) {
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-emp/{employee}";
            }

            HttpStatus status = credentialsService.changePassword(employee.getId(), form.getNewPassword());

            if (status.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Success: Employee's password has been updated.");
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }

            return "redirect:/mg-emp/{employee}";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-emp";
    }

    @RequestMapping(value = "/{employee}/update-lock", method = RequestMethod.POST)
    public String employeeLockButton(@ModelAttribute("lock_form") @Valid UpdateLockForm form, Errors err, RedirectAttributes redAtt, @PathVariable(value = "employee") Long employeeId) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Employee employee = employeeService.findById(employeeId);
            if (!employeeService.isSupervisorOf(cud, employee)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-emp/{employee}";
            }

            if (err.hasErrors()) {
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-emp/{employee}";
            }

            HttpStatus status = employeeService.setLock(employeeId, form.getDate());

            if (status.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Success: Employee's status has been updated.");
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }

            return "redirect:/mg-emp/{employee}";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-emp";
    }

    @RequestMapping(value = "/{employee}/update-departments", method = RequestMethod.POST)
    public String employeeDepartmentsButton(@ModelAttribute("departments_form") @Valid UpdateDepartmentsForm form, Errors err, RedirectAttributes redAtt, @PathVariable(value = "employee") Long employeeId) {
        try {
            redAtt.addFlashAttribute("departments_form", form);

            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Employee employee = employeeService.findById(employeeId);
            if (!employeeService.isSupervisorOf(cud, employee)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-emp/{employee}";
            }

            if (err.hasErrors()) {
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-emp/{employee}";
            }

            HttpStatus status = employeeService.setDepartments(employeeId, form.getDepartments());

            if (status.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Success: Employee's departments has been updated.");
            } else if (status.equals(HttpStatus.PRECONDITION_FAILED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Failure: Employee's rank is insufficient.");
            } else if (status.equals(HttpStatus.BAD_REQUEST)) {
                redAtt.addFlashAttribute(MSG_KEY, "Failure: Employee must be in at least one department.");
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }

            return "redirect:/mg-emp/{employee}";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-emp";
    }

    @RequestMapping(value = "/{employee}/delete", method = RequestMethod.POST)
    public String employeeDeleteButton(@ModelAttribute("delete_form") @Valid ConfirmationForm form, Errors err, RedirectAttributes redAtt, @PathVariable(value = "employee") Long employeeId) {
        try {
            redAtt.addFlashAttribute("details_form", form);

            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Employee employee = employeeService.findById(employeeId);
            if (!employeeService.isSupervisorOf(cud, employee)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-emp/{employee}";
            }

            if (err.hasErrors()) {
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-emp/{employee}";
            }

            HttpStatus status = employeeService.delete(employee.getId());

            if (status.equals(HttpStatus.OK)) {
                redAtt.addFlashAttribute(MSG_KEY, "Success: Employee's account has been deleted.");
                return "redirect:/mg-emp";
            } else if (status.equals(HttpStatus.PRECONDITION_FAILED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Failure: Employee's account cannot be deleted.");
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }

            return "redirect:/mg-emp/{employee}";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-emp";
    }

}
