package com.sda.carrental.web.mvc.user;

import com.sda.carrental.model.users.User;
import com.sda.carrental.service.CredentialsService;
import com.sda.carrental.service.CustomerService;
import com.sda.carrental.service.UserService;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.common.ConfirmationForm;
import com.sda.carrental.web.mvc.form.users.ChangeContactForm;
import com.sda.carrental.web.mvc.form.users.ChangeEmailForm;
import com.sda.carrental.web.mvc.form.users.ChangePasswordForm;
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
        } catch (RuntimeException err) {
            SecurityContextHolder.getContext().setAuthentication(null);
            redAtt.addFlashAttribute("message", "Something went wrong. Please login again.");
            return "redirect:/";
        }

        map.addAttribute("username", cud.getUsername());
        map.addAttribute("password_form", map.getOrDefault("password_form", new ChangePasswordForm()));

        if (cud.getAuthorities().contains(new SimpleGrantedAuthority(User.Roles.ROLE_CUSTOMER.name()))) {
            map.addAttribute("contact_form", map.getOrDefault("contact_form", new ChangeContactForm()));
            map.addAttribute("email_form", map.getOrDefault("email_form", new ChangeEmailForm()));
            map.addAttribute("delete_form", map.getOrDefault("delete_form", new ConfirmationForm()));

            return "user/profileCustomer";
        }
        return "user/profileEmployee";
    }

    //Confirmation buttons
    @RequestMapping(method = RequestMethod.POST, value = "/contact")
    public String changeContactConfirmButton(RedirectAttributes redAtt, @ModelAttribute("contact_form") @Valid ChangeContactForm form, Errors errors) {
        if (errors.hasErrors()) {
            redAtt.addFlashAttribute("message", errors.getAllErrors().get(0).getDefaultMessage());
            redAtt.addFlashAttribute("contact_form", form);
            return "redirect:/profile";
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

    @RequestMapping(method = RequestMethod.POST, value = "/email")
    public String changeEmailConfirmButton(RedirectAttributes redAtt, @ModelAttribute("email_form") @Valid ChangeEmailForm form, Errors errors) {
        if (errors.hasErrors()) {
            redAtt.addFlashAttribute("message", errors.getAllErrors().get(0).getDefaultMessage());
            redAtt.addFlashAttribute("email_form", form);
            return "redirect:/profile";
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
            redAtt.addFlashAttribute("message", errors.getAllErrors().get(0).getDefaultMessage());
            redAtt.addFlashAttribute("password_form", form);
            return "redirect:/profile";
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
            redAtt.addFlashAttribute("message", errors.getAllErrors().get(0).getDefaultMessage());
            redAtt.addFlashAttribute("delete_form", form);
            return "redirect:/profile";
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
