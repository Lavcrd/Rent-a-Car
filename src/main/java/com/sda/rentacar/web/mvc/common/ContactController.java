package com.sda.rentacar.web.mvc.common;

import com.sda.rentacar.service.CountryService;
import com.sda.rentacar.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequiredArgsConstructor
@RequestMapping("/contact")
public class ContactController {
    private final DepartmentService departmentService;
    private final CountryService countryService;

    private final String MSG_KEY = "message";
    private final String MSG_GENERIC_EXCEPTION = "Failure: An unexpected error occurred";

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String contactPage(final ModelMap map, RedirectAttributes redAtt) {
        //geolocation with user IP or in case of inaccessible country - language of page ex. /pl/contact
        try {
            map.addAttribute("dealer", departmentService.findAllWhereCountry(countryService.findById(1L)));
            map.addAttribute("hq", departmentService.findAllWhereCountryAndHq(countryService.findById(1L)));
            return "common/contact";
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/";
        }
    }
}
