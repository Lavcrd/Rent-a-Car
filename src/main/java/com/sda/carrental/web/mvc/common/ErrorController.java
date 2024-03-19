package com.sda.carrental.web.mvc.common;

import com.sda.carrental.service.DepartmentService;
import com.sda.carrental.web.mvc.form.operational.IndexForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@RequestMapping(path = "/error")
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {
    private final DepartmentService departmentService;

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String errorPage(final ModelMap map) {
        map.addAttribute("message", "Page not found or access denied.");
        map.addAttribute("departments", departmentService.findAll());
        map.addAttribute("indexForm", new IndexForm(LocalDate.now(), LocalDate.now().plusDays(2)));
        return "common/index";
    }
}