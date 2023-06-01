package com.sda.carrental.web.mvc;

import com.sda.carrental.constants.enums.Country;
import com.sda.carrental.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@Controller
@RequiredArgsConstructor
@RequestMapping("/contact")
public class ContactController {

    private final DepartmentService departmentService;

    @RequestMapping(method = RequestMethod.GET)
    public String welcomePage(final ModelMap map) {
        //User IP for geolocation?
        map.addAttribute("dealer", departmentService.findAllWhereCountry(Country.COUNTRY_PL));
        map.addAttribute("hq", departmentService.findAllWhereCountryAndHq(Country.COUNTRY_PL));
        return "core/contact";
    }
}
