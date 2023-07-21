package com.sda.carrental.web.mvc.user;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.enums.Country;
import com.sda.carrental.model.users.User;
import com.sda.carrental.service.CredentialsService;
import com.sda.carrental.service.CustomerService;
import com.sda.carrental.service.UserService;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
@RequestMapping("/profile")
public class ProfileController {
    private final CustomerService customerService;
    private final CredentialsService credentialsService;
    private final UserService userService;

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String profilePage(ModelMap map, RedirectAttributes redAtt) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            map.addAttribute("user", userService.findById(cud.getId()));
            map.addAttribute("username", credentialsService.findById(cud.getId()).getUsername());
        } catch (ResourceNotFoundException err) {
            SecurityContextHolder.getContext().setAuthentication(null);
            redAtt.addFlashAttribute("message", "User not recognized. Please login again.");
            return "redirect:/";
        }

        if(cud.getAuthorities().contains(new SimpleGrantedAuthority(User.Roles.ROLE_CUSTOMER.name()))) {
            return "user/profileCustomer";
        }
        return "user/profileEmployee";
    }

    //Pages from profile buttons
    @RequestMapping(method = RequestMethod.GET, value = "/password")
    public String changePasswordPage(final ModelMap map) {
        map.addAttribute("password_form", new ChangePasswordForm());
        return "user/passwordUser";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/email")
    public String changeEmailPage(final ModelMap map) {
        map.addAttribute("email_form", new ChangeEmailForm());
        return "user/emailCustomer";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/contact")
    public String changeContactPage(final ModelMap map) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        map.addAttribute("customer", customerService.findById(cud.getId()));
        map.addAttribute("contact_form", new ChangeContactForm());
        return "user/contactCustomer";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/delete")
    public String deleteAccountPage(final ModelMap map) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        map.addAttribute("user", cud.getUsername());
        map.addAttribute("delete_form", new ConfirmationForm());
        return "user/deleteCustomer";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/address")
    public String changeAddressPage(final ModelMap map) {
        map.addAttribute("countries", Country.values());
        map.addAttribute("address_form", new ChangeAddressForm());
        return "user/addressCustomer";
    }

    //Confirmation buttons
    @RequestMapping(method = RequestMethod.POST, value = "/contact")
    public String changeContactConfirmButton(RedirectAttributes redAtt, @ModelAttribute("contact_form") @Valid ChangeContactForm form, Errors errors) {
        if (errors.hasErrors()) {
            return "user/contactCustomer";
        }

        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        HttpStatus response = customerService.changeContact(form.getContactNumber(), cud.getId());
        if (response.equals(HttpStatus.ACCEPTED)) {
            redAtt.addFlashAttribute("message", "Contact number has been changed successfully.");
            return "redirect:/profile";
        } else if (response.equals(HttpStatus.NOT_FOUND)) {
            redAtt.addFlashAttribute("message", "User not recognized. Please login again.");
        } else {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again later or contact customer service.");
        }
        SecurityContextHolder.getContext().setAuthentication(null);
        return "redirect:/";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/address")
    public String changeAddressConfirmButton(RedirectAttributes redAtt, @ModelAttribute("address_form") @Valid ChangeAddressForm form, Errors errors) {
        if (errors.hasErrors()) {
            return "user/addressCustomer";
        }

        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        HttpStatus response = customerService.changeAddress(form, cud.getId());
        if (response.equals(HttpStatus.ACCEPTED)) {
            redAtt.addFlashAttribute("message", "The mailing address has been successfully changed.");
            return "redirect:/profile";
        } else if (response.equals(HttpStatus.NOT_FOUND)) {
            redAtt.addFlashAttribute("message", "User not recognized. Please login again.");
        } else {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again later or contact customer service.");
        }
        SecurityContextHolder.getContext().setAuthentication(null);
        return "redirect:/";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/email")
    public String changeEmailConfirmButton(RedirectAttributes redAtt, @ModelAttribute("email_form") @Valid ChangeEmailForm form, Errors errors) {
        if (errors.hasErrors()) {
            return "user/emailCustomer";
        }

        HttpStatus response = credentialsService.changeUsername(form.getNewEmail());
        if (response.equals(HttpStatus.ACCEPTED)) {
            redAtt.addFlashAttribute("message", "The email has been successfully changed. Please login again.");
        } else if (response.equals(HttpStatus.NOT_FOUND)) {
            redAtt.addFlashAttribute("message", "User not recognized. Please login again.");
        } else {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again later or contact customer service.");
        }
        SecurityContextHolder.getContext().setAuthentication(null);
        return "redirect:/";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/password")
    public String changePasswordConfirmButton(RedirectAttributes redAtt, @ModelAttribute("password_form") @Valid ChangePasswordForm form, Errors errors) {
        if (errors.hasErrors()) {
            return "user/passwordUser";
        }

        HttpStatus response = credentialsService.changePassword(form.getNewPassword());
        if (response.equals(HttpStatus.ACCEPTED)) {
            redAtt.addFlashAttribute("message", "Password has been changed successfully.");
            return "redirect:/profile";
        } else if (response.equals(HttpStatus.NOT_FOUND)) {
            redAtt.addFlashAttribute("message", "User not recognized. Please login again.");
        } else {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again later or contact customer service.");
        }
        SecurityContextHolder.getContext().setAuthentication(null);
        return "redirect:/";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/delete")
    public String deleteAccountConfirmButton(RedirectAttributes redAtt, @ModelAttribute("delete_form") @Valid ConfirmationForm form, Errors errors) {
        if (errors.hasErrors()) {
            return "user/deleteCustomer";
        }

        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        HttpStatus response = userService.deleteUser(cud.getId());

        if (response.equals(HttpStatus.OK)) {
            redAtt.addFlashAttribute("message", "Account has been successfully deleted.");
        } else if (response.equals(HttpStatus.NOT_FOUND)) {
            redAtt.addFlashAttribute("message", "User not recognized. Please login again.");
        } else if (response.equals(HttpStatus.CONFLICT)) {
            redAtt.addFlashAttribute("message", "Action not allowed due to active reservations.");
            return "redirect:/profile";
        } else {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again later or contact customer service.");
        }
        SecurityContextHolder.getContext().setAuthentication(null);
        return "redirect:/";
    }
}