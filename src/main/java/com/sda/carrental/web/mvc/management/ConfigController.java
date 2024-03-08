package com.sda.carrental.web.mvc.management;

import com.sda.carrental.model.company.Company;
import com.sda.carrental.service.*;
import com.sda.carrental.web.mvc.form.common.UpdateImageForm;
import com.sda.carrental.web.mvc.form.property.company.UpdateDetailsForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    private final CompanyService companyService;

    private final String MSG_KEY = "message";
    private final String MSG_GENERIC_EXCEPTION = "Failure: An unexpected error occurred";
    private final String MSG_NO_RESOURCE = "Failure: Resource not found";

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String viewCompanyPage(ModelMap map, RedirectAttributes redAtt) {
        try {
            Company company = companyService.get();
            map.addAttribute("company", company);
            map.addAttribute("logotype_form", new UpdateImageForm());
            map.addAttribute("details_form", new UpdateDetailsForm(company));

            return "management/viewConfig";
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/";
        }
    }

    //Company page buttons
    @RequestMapping(method = RequestMethod.POST, value = "/update-logotype")
    public String updateLogotypeButton(@ModelAttribute("logotype_form") @Valid UpdateImageForm form, Errors errors, RedirectAttributes redAtt) {
        try {
            if (errors.hasErrors()) {
                redAtt.addFlashAttribute(MSG_KEY, errors.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-cfg";
            }

            HttpStatus status = companyService.updateLogotype(form.getImage());
            if (status.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Success: Logotype successfully updated");
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }
            return "redirect:/mg-cfg";
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/update-details")
    public String updateDetailsButton(@ModelAttribute("details_form") @Valid UpdateDetailsForm form, Errors errors, RedirectAttributes redAtt) {
        try {
            if (errors.hasErrors()) {
                redAtt.addFlashAttribute(MSG_KEY, errors.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-cfg";
            }

            HttpStatus status = companyService.updateDetails(form);
            if (status.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Success: Details successfully updated");
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }
            return "redirect:/mg-cfg";
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/";
    }
}
