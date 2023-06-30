package com.sda.carrental.web.mvc;

import com.sda.carrental.global.enums.Country;
import com.sda.carrental.model.users.User;
import com.sda.carrental.service.CustomerService;
import com.sda.carrental.web.mvc.form.RegisterCustomerForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
@RequestMapping("/register")
public class RegisterController {
    private final CustomerService customerService;

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String createCustomerPage(final ModelMap map) {
        map.addAttribute("customer", new RegisterCustomerForm());
        map.addAttribute("countries", Country.values());

        return "user/registerCustomer";
    }

    //Create customer page buttons
    @RequestMapping(method = RequestMethod.POST)
    public String createCustomer(@ModelAttribute("customer") @Valid RegisterCustomerForm form, Errors errors, final ModelMap map, RedirectAttributes redAtt) {
        if (errors.hasErrors()) {
            map.addAttribute("customer", form);
            map.addAttribute("countries", Country.values());
            return "user/registerCustomer";
        }

        HttpStatus status = customerService.createCustomer(form);
        if (status.equals(HttpStatus.CREATED)) {
        map.addAttribute("message", "User " + form.getName() + " " + form.getSurname() + " with login " + form.getUsername() + " has been added.");
            return "core/login";
        }
        redAtt.addFlashAttribute("message", "Something went wrong. Please try again or contact customer service.");
        return "redirect:/register";
    }
}
