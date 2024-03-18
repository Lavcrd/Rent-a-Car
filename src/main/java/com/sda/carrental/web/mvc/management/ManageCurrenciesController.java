package com.sda.carrental.web.mvc.management;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.enums.Role;
import com.sda.carrental.model.property.payments.Currency;
import com.sda.carrental.service.CurrencyService;
import com.sda.carrental.service.EmployeeService;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.common.ConfirmationForm;
import com.sda.carrental.web.mvc.form.property.payments.RegisterCurrencyForm;
import com.sda.carrental.web.mvc.form.property.payments.SearchCurrenciesForm;
import com.sda.carrental.web.mvc.form.property.payments.UpdateCurrencyDetailsForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mg-cur")
public class ManageCurrenciesController {
    private final EmployeeService employeeService;
    private final CurrencyService currencyService;

    private final String MSG_KEY = "message";
    private final String MSG_CUR_UPDATED = "Success: Currency has been updated.";
    private final String MSG_ACCESS_REJECTED = "Failure: Access rejected";
    private final String MSG_GENERIC_EXCEPTION = "Failure: An unexpected error occurred";
    private final String MSG_NO_RESOURCE = "Failure: Resource not found";
    private final String MSG_EXISTS_EXCEPTION = "Failure: Currency already exists.";

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String searchCurrenciesPage(ModelMap map, RedirectAttributes redAtt) {
        try {
            map.addAttribute("default_currency", currencyService.placeholder());
            map.addAttribute("searchCurrenciesForm", map.getOrDefault("searchCurrenciesForm", new SearchCurrenciesForm()));
            map.addAttribute("c_results", map.getOrDefault("c_results", currencyService.findAll()));

            map.addAttribute("register_form", map.getOrDefault("register_form", new RegisterCurrencyForm()));

            return "management/searchCurrencies";
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/";
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{currency}")
    public String viewCurrencyPage(ModelMap map, RedirectAttributes redAtt, @PathVariable(value = "currency") Long currencyId) {
        try {
            Currency currency = currencyService.findById(currencyId);
            map.addAttribute("currency", currency);
            map.addAttribute("default_currency", currencyService.placeholder());
            map.addAttribute("hasPresence", currencyService.hasPresence(currency.getId()));
            map.addAttribute("currency_usage", currencyService.findUsageById(currencyId));

            map.addAttribute("details_form", new UpdateCurrencyDetailsForm(currency));
            map.addAttribute("confirm_form", new ConfirmationForm());

            return "management/viewCurrency";
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/";
        }
    }

    // Search page buttons
    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public String searchCurrenciesButton(@ModelAttribute("searchCurrenciesForm") @Valid SearchCurrenciesForm form, Errors err, RedirectAttributes redAtt) {
        try {
            redAtt.addFlashAttribute("searchCurrenciesForm", form);

            if (err.hasErrors()) {
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-cur";
            }

            redAtt.addFlashAttribute("c_results", currencyService.findByForm(form));
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-cur";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String currencyRegisterButton(@ModelAttribute("register_form") @Valid RegisterCurrencyForm form, Errors err, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (!employeeService.hasMinimumAuthority(cud, Role.ROLE_ADMIN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-cur";
            }

            if (err.hasErrors()) {
                redAtt.addFlashAttribute("register_form", form);
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-cur";
            }

            HttpStatus status = currencyService.register(form);

            if (status.equals(HttpStatus.CREATED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Success: Currency added.");
            } else if (status.equals(HttpStatus.CONFLICT)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_EXISTS_EXCEPTION);
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-cur";
    }

    // View page buttons
    @RequestMapping(value = "/{currency}/update-details", method = RequestMethod.POST)
    public String currencyDetailsButton(@ModelAttribute("details_form") @Valid UpdateCurrencyDetailsForm form, Errors err, @PathVariable(value = "currency") Long currencyId, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (!employeeService.hasMinimumAuthority(cud, Role.ROLE_ADMIN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-cur/{currency}";
            }

            if (err.hasErrors()) {
                redAtt.addFlashAttribute("details_form", form);
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-cur/{currency}";
            }

            HttpStatus status = currencyService.updateDetails(currencyId, form);

            if (status.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_CUR_UPDATED);
            } else if (status.equals(HttpStatus.CONFLICT)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_EXISTS_EXCEPTION);
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }
            return "redirect:/mg-cur/{currency}";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-cur";
    }

    @RequestMapping(value = "/{currency}/update-exchange", method = RequestMethod.POST)
    public String currencyRefreshButton(@ModelAttribute("confirm_form") @Valid ConfirmationForm form, Errors err, @PathVariable(value = "currency") Long currencyId, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (!employeeService.hasMinimumAuthority(cud, Role.ROLE_ADMIN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-cur/{currency}";
            }

            if (err.hasErrors()) {
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-cur/{currency}";
            }

            HttpStatus status = currencyService.updateExchange(currencyId);

            if (status.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_CUR_UPDATED);
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }
            return "redirect:/mg-cur/{currency}";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-cur";
    }

    @RequestMapping(value = "/{currency}/delete", method = RequestMethod.POST)
    public String currencyDeleteButton(@ModelAttribute("confirm_form") @Valid ConfirmationForm form, Errors err, @PathVariable(value = "currency") Long currencyId, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (!employeeService.hasMinimumAuthority(cud, Role.ROLE_ADMIN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-cur/{currency}";
            }

            if (err.hasErrors()) {
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-cur/{currency}";
            }

            HttpStatus status = currencyService.delete(currencyId);

            if (status.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Success: Currency has been successfully removed.");
                return "redirect:/mg-cur";
            } else if (status.equals(HttpStatus.PRECONDITION_FAILED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Failure: Currency cannot be removed.");
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }
            return "redirect:/mg-cur/{currency}";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-cur";
    }
}
