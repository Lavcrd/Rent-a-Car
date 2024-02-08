package com.sda.carrental.web.mvc.management;

import com.sda.carrental.global.enums.Country;
import com.sda.carrental.service.DepartmentService;
import com.sda.carrental.web.mvc.form.property.departments.SearchDepartmentsForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
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

    private final String MSG_KEY = "message";
    private final String MSG_ACCESS_REJECTED = "Failure: Access rejected";
    private final String MSG_GENERIC_EXCEPTION = "Failure: An unexpected error occurred";
    private final String MSG_NO_RESOURCE = "Failure: Resource not found";

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String searchDepartmentsPage(ModelMap map, RedirectAttributes redAtt) {
        try {
            map.addAttribute("countries", Country.values());
            map.addAttribute("searchDepartmentsForm", map.getOrDefault("searchDepartmentsForm", new SearchDepartmentsForm()));
            map.addAttribute("d_results", map.getOrDefault("d_results", Collections.emptyList()));

            /*map.addAttribute("register_form", map.getOrDefault("register_form", new RegisterDepartmentForm()));*/

            return "management/searchDepartments";
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/";
        }
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

            return "redirect:/mg-dpt";
        } catch (RuntimeException e) {
            e.printStackTrace();
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/mg-dpt";
        }
    }
}
