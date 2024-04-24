package com.sda.rentacar.web.mvc.common;


import com.sda.rentacar.global.Utility;
import com.sda.rentacar.service.DepartmentService;
import com.sda.rentacar.web.mvc.form.operational.IndexForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@RequestMapping(path = "/")
public class IndexController {
    private final Utility utility;
    private final DepartmentService departmentService;

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String indexPage(final ModelMap map, HttpServletRequest res) {
        utility.retrieveSessionMessage(map, res);
        map.addAttribute("departments", departmentService.findAll());
        map.addAttribute("indexForm", new IndexForm(LocalDate.now(), LocalDate.now().plusDays(2)));
        return "common/index";
    }

    //Index buttons
    @RequestMapping(method = RequestMethod.POST)
    public String indexReservationProcessButton(@ModelAttribute("indexForm") @Valid IndexForm form, Errors errors, HttpSession httpSession, ModelMap map) {
        if (errors.hasErrors()) {
            map.addAttribute("departments", departmentService.findAll());
            return "common/index";
        }

        if (!form.isDifferentDepartment()) form.setDepartmentIdTo(form.getDepartmentIdFrom());
        if (form.isDifferentDepartment()) {
            if (form.getDepartmentIdFrom().equals(form.getDepartmentIdTo())) form.setDifferentDepartment(false);
        }

        httpSession.setAttribute("process_indexForm", form);
        httpSession.setAttribute("process_step1_time", LocalDateTime.now());
        return "redirect:/cars";
    }
}