package com.sda.carrental.web.mvc.management;

import com.sda.carrental.service.*;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.common.ConfirmationForm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mg-cfg")
public class ConfigController {
    private final CountryService countryService;
    private final CompanyService companyService;

    private final String MSG_KEY = "message";
    private final String MSG_GENERIC_EXCEPTION = "Failure: An unexpected error occurred";
    private final String MSG_NO_RESOURCE = "Failure: Resource not found";

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String viewCompanyPage(ModelMap map, RedirectAttributes redAtt) {
        try {
            map.addAttribute("company", companyService.get());

            return "management/viewConfig";
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/";
        }
    }

    //Company page buttons
    @RequestMapping(method = RequestMethod.POST, value = "/example")
    public String exampleButton(@ModelAttribute("example") @Valid ConfirmationForm form, Errors err, RedirectAttributes redAtt) {
        if (err.hasErrors()) {
            return "redirect:/mg-cfg";
        }

        redAtt.addFlashAttribute("results", null);
        return "redirect:/mg-cfg";
    }
}
