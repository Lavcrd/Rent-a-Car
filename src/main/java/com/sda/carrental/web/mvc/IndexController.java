package com.sda.carrental.web.mvc;


import com.sda.carrental.service.DepartmentService;
import com.sda.carrental.web.mvc.form.IndexForm;
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
        map.addAttribute("department", departmentService.findAll());

        IndexForm form = new IndexForm();
        form.setDateFrom(LocalDate.now());
        form.setDateTo(LocalDate.now().plusDays(2));
        map.addAttribute("indexForm", form);
        return "core/index";
    }

    //Index buttons
    @RequestMapping(method = RequestMethod.POST)
    public String handleRequest(@ModelAttribute("indexForm") @Valid IndexForm form, Errors errors, RedirectAttributes redirectAttributes, ModelMap map) {
        if (errors.hasErrors()) {
            if (form.isFirstBranchChecked()) form.setFirstBranchChecked(false);
            map.addAttribute("department", departmentService.findAll());
            return "core/index";
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