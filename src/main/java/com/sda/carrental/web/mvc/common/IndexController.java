package com.sda.carrental.web.mvc.common;


import com.sda.carrental.service.DepartmentService;
import com.sda.carrental.web.mvc.form.operational.IndexForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@RequestMapping(path = "/")
public class IndexController {
    private final DepartmentService departmentService;

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String indexPage(final ModelMap map) {
        map.addAttribute("departments", departmentService.findAll());
        map.addAttribute("indexForm", new IndexForm(LocalDate.now(), LocalDate.now().plusDays(2)));
        return "common/index";
    }

    //Index buttons
    @RequestMapping(method = RequestMethod.POST)
    public String handleRequest(@ModelAttribute("indexForm") @Valid IndexForm form, Errors errors, RedirectAttributes redirectAttributes, ModelMap map) {
        if (errors.hasErrors()) {
            if (form.isFirstBranchChecked()) form.setFirstBranchChecked(false);
            map.addAttribute("departments", departmentService.findAll());
            return "common/index";
        }

        if (!form.isFirstBranchChecked()) form.setDepartmentIdTo(form.getDepartmentIdFrom());
        if (form.isFirstBranchChecked()) {
            if (form.getDepartmentIdFrom().equals(form.getDepartmentIdTo())) form.setFirstBranchChecked(false);
        }
        form.setDateCreated(LocalDate.now());

        redirectAttributes.addFlashAttribute("indexData", form);
        return "redirect:/cars";
    }
}