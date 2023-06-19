package com.sda.carrental.web.mvc;

import com.sda.carrental.global.enums.Country;
import com.sda.carrental.model.users.User;
import com.sda.carrental.service.UserService;
import com.sda.carrental.web.mvc.form.RegisterCustomerForm;
import com.sda.carrental.service.mappers.CustomerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
@RequestMapping("/register")
public class RegisterController {
    private final UserService userService;

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String createCustomerPage(final ModelMap map) {
        map.addAttribute("customer", new RegisterCustomerForm());
        map.addAttribute("roles", User.Roles.values());
        map.addAttribute("countries", Country.values());

        return "user/registerCustomer";
    }

    //Create customer page buttons
    @RequestMapping(method = RequestMethod.POST)
    public String createCustomer(@ModelAttribute("customer") @Valid RegisterCustomerForm form, Errors errors, final ModelMap map) {
        if (errors.hasErrors()) {
            map.addAttribute("roles", User.Roles.values());
            map.addAttribute("countries", Country.values());
            return "user/registerCustomer";
        }
        userService.save(CustomerMapper.toEntity(form));
        map.addAttribute("message", "User " + form.getName() + " " + form.getSurname() + " with login " + form.getEmail() + " has been added.");

        return "core/login";
    }


}
