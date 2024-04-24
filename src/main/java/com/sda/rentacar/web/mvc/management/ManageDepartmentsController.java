package com.sda.rentacar.web.mvc.management;

import com.sda.rentacar.exceptions.ResourceNotFoundException;
import com.sda.rentacar.global.enums.Role;
import com.sda.rentacar.model.property.department.Department;
import com.sda.rentacar.service.CountryService;
import com.sda.rentacar.service.DepartmentService;
import com.sda.rentacar.service.EmployeeService;
import com.sda.rentacar.service.auth.CustomUserDetails;
import com.sda.rentacar.web.mvc.form.common.ConfirmationForm;
import com.sda.rentacar.web.mvc.form.property.departments.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Collections;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mg-dpt")
public class ManageDepartmentsController {
    private final DepartmentService departmentService;
    private final EmployeeService employeeService;
    private final CountryService countryService;

    private final String MSG_KEY = "message";
    private final String MSG_ACCESS_REJECTED = "Failure: Access rejected";
    private final String MSG_GENERIC_EXCEPTION = "Failure: An unexpected error occurred";
    private final String MSG_NO_RESOURCE = "Failure: Resource not found";
    private final String MSG_DPT_UPDATED = "Success: Department has been updated.";

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String searchDepartmentsPage(ModelMap map, RedirectAttributes redAtt) {
        try {
            map.addAttribute("countries", countryService.findAll());
            map.addAttribute("searchDepartmentsForm", map.getOrDefault("searchDepartmentsForm", new SearchDepartmentsForm()));
            map.addAttribute("d_results", map.getOrDefault("d_results", Collections.emptyList()));

            map.addAttribute("register_form", map.getOrDefault("register_form", new RegisterDepartmentForm()));

            return "management/searchDepartments";
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/";
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{department}")
    public String viewDepartmentPage(ModelMap map, RedirectAttributes redAtt, @PathVariable(value = "department") Long departmentId) {
        try {
            Department department = departmentService.findById(departmentId);

            map.addAttribute("department", department);
            map.addAttribute("hasPresence", departmentService.hasPresence(department.getId()));

            map.addAttribute("details_form", map.getOrDefault("details_form", new UpdateDepartmentForm(department)));
            map.addAttribute("contacts_form", map.getOrDefault("contacts_form", new UpdateContactsForm(department)));
            map.addAttribute("multiplier_form", map.getOrDefault("multiplier_form", new UpdateMultiplierForm(department)));

            map.addAttribute("confirm_form", map.getOrDefault("confirm_form", new ConfirmationForm()));

            return "management/viewDepartment";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-dpt";
    }

    //Search page buttons
    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public String searchDepartmentsButton(@ModelAttribute("searchDepartmentsForm") @Valid SearchDepartmentsForm form, Errors err, RedirectAttributes redAtt) {
        try {
            redAtt.addFlashAttribute("searchDepartmentsForm", form);

            if (err.hasErrors()) {
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-dpt";
            }

            redAtt.addFlashAttribute("d_results", departmentService.findByForm(form));
        } catch (RuntimeException e) {
            e.printStackTrace();
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-dpt";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String departmentRegisterButton(@ModelAttribute("register_form") @Valid RegisterDepartmentForm form, Errors err, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (!employeeService.hasMinimumAuthority(cud, Role.ROLE_DIRECTOR)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-dpt";
            }

            if (err.hasErrors()) {
                redAtt.addFlashAttribute("register_form", form);
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-dpt";
            }

            Department department = departmentService.register(form);
            HttpStatus status = employeeService.assignDepartment(cud.getId(), department);

            if (status.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Success: Department created.");
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-dpt";
    }

    //View page buttons
    @RequestMapping(value = "/{department}/update-details", method = RequestMethod.POST)
    public String departmentDetailsButton(@ModelAttribute("details_form") @Valid UpdateDepartmentForm form, Errors err, @PathVariable(value = "department") Long departmentId, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (!employeeService.hasMinimumAuthority(cud, Role.ROLE_COORDINATOR) ||
                    employeeService.departmentAccess(cud, departmentId).equals(HttpStatus.FORBIDDEN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-dpt/{department}";
            }

            if (err.hasErrors()) {
                redAtt.addFlashAttribute("details_form", form);
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-dpt/{department}";
            }

            HttpStatus status = departmentService.updateDetails(departmentId, form);

            if (status.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_DPT_UPDATED);
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }
            return "redirect:/mg-dpt/{department}";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-dpt";
    }

    @RequestMapping(value = "/{department}/update-contacts", method = RequestMethod.POST)
    public String departmentContactsButton(@ModelAttribute("contacts_form") @Valid UpdateContactsForm form, Errors err, @PathVariable(value = "department") Long departmentId, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (!employeeService.hasMinimumAuthority(cud, Role.ROLE_COORDINATOR) ||
                    employeeService.departmentAccess(cud, departmentId).equals(HttpStatus.FORBIDDEN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-dpt/{department}";
            }

            if (err.hasErrors()) {
                redAtt.addFlashAttribute("contacts_form", form);
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-dpt/{department}";
            }

            HttpStatus status = departmentService.updateContacts(departmentId, form);

            if (status.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_DPT_UPDATED);
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }
            return "redirect:/mg-dpt/{department}";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-dpt";
    }

    @RequestMapping(value = "/{department}/update-multiplier", method = RequestMethod.POST)
    public String departmentMultiplierButton(@ModelAttribute("multiplier_form") @Valid UpdateMultiplierForm form, Errors err, @PathVariable(value = "department") Long departmentId, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (!employeeService.hasMinimumAuthority(cud, Role.ROLE_COORDINATOR) ||
                    employeeService.departmentAccess(cud, departmentId).equals(HttpStatus.FORBIDDEN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-dpt/{department}";
            }

            if (err.hasErrors()) {
                redAtt.addFlashAttribute("multiplier_form", form);
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-dpt/{department}";
            }

            HttpStatus status = departmentService.updateMultiplier(departmentId, form);

            if (status.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_DPT_UPDATED);
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }
            return "redirect:/mg-dpt/{department}";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-dpt";
    }

    @RequestMapping(value = "/{department}/update-activity", method = RequestMethod.POST)
    public String departmentActivityButton(@ModelAttribute("confirm_form") @Valid ConfirmationForm form, Errors err, @PathVariable(value = "department") Long departmentId, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (!employeeService.hasMinimumAuthority(cud, Role.ROLE_COORDINATOR) ||
                    employeeService.departmentAccess(cud, departmentId).equals(HttpStatus.FORBIDDEN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-dpt/{department}";
            }

            if (err.hasErrors()) {
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-dpt/{department}";
            }

            HttpStatus status = departmentService.updateActivity(departmentId);

            if (status.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_DPT_UPDATED);
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }
            return "redirect:/mg-dpt/{department}";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-dpt";
    }

    @RequestMapping(value = "/{department}/update-hq", method = RequestMethod.POST)
    public String departmentHQButton(@ModelAttribute("confirm_form") @Valid ConfirmationForm form, Errors err, @PathVariable(value = "department") Long departmentId, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (!employeeService.hasMinimumAuthority(cud, Role.ROLE_DIRECTOR) ||
                    employeeService.departmentAccess(cud, departmentId).equals(HttpStatus.FORBIDDEN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-dpt/{department}";
            }

            if (err.hasErrors()) {
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-dpt/{department}";
            }

            HttpStatus status = departmentService.updateHQ(departmentId);

            if (status.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_DPT_UPDATED);
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }
            return "redirect:/mg-dpt/{department}";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-dpt";
    }

    @RequestMapping(value = "/{department}/delete", method = RequestMethod.POST)
    public String departmentDeleteButton(@ModelAttribute("confirm_form") @Valid ConfirmationForm form, Errors err, @PathVariable(value = "department") Long departmentId, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (!employeeService.hasMinimumAuthority(cud, Role.ROLE_DIRECTOR) ||
                    employeeService.departmentAccess(cud, departmentId).equals(HttpStatus.FORBIDDEN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-dpt/{department}";
            }

            if (err.hasErrors()) {
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-dpt/{department}";
            }

            HttpStatus status = departmentService.delete(departmentId);

            if (status.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Success: Department has been successfully removed.");
                return "redirect:/mg-dpt";
            } else if (status.equals(HttpStatus.PRECONDITION_FAILED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Failure: Department cannot be removed.");
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }
            return "redirect:/mg-dpt/{department}";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-dpt";
    }
}
