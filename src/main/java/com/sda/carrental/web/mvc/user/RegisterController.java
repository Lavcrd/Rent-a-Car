package com.sda.carrental.web.mvc.user;

import com.sda.carrental.service.CustomerService;
import com.sda.carrental.web.mvc.form.users.RegisterCustomerForm;
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

    private final String MSG_KEY = "message";

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String createCustomerPage(final ModelMap map) {
        map.addAttribute("customer", new RegisterCustomerForm());

        return "user/registerCustomer";
    }

    //Create customer page buttons
    @RequestMapping(method = RequestMethod.POST)
    public String createCustomer(@ModelAttribute("customer") @Valid RegisterCustomerForm form, Errors errors, final ModelMap map, RedirectAttributes redAtt) {
        if (errors.hasErrors()) {
            map.addAttribute("customer", form);
            return "user/registerCustomer";
        }

        HttpStatus status = customerService.createCustomer(form);
        if (status.equals(HttpStatus.CREATED)) {
        map.addAttribute(MSG_KEY, "User " + form.getName() + " " + form.getSurname() + " with login " + form.getUsername() + " has been added.");
            return "user/login";
        }
        redAtt.addFlashAttribute(MSG_KEY, "Something went wrong. Please try again later.");
        return "redirect:/register";
    }
}
