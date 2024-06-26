package com.sda.rentacar.web.mvc.management;

import com.sda.rentacar.exceptions.ResourceNotFoundException;
import com.sda.rentacar.global.Utility;
import com.sda.rentacar.global.enums.Role;
import com.sda.rentacar.model.property.department.Country;
import com.sda.rentacar.service.CountryService;
import com.sda.rentacar.service.CurrencyService;
import com.sda.rentacar.service.EmployeeService;
import com.sda.rentacar.service.auth.CustomUserDetails;
import com.sda.rentacar.web.mvc.form.common.ConfirmationForm;
import com.sda.rentacar.web.mvc.form.property.departments.country.*;
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
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mg-ctr")
public class ManageCountriesController {
    private final EmployeeService employeeService;
    private final CountryService countryService;
    private final CurrencyService currencyService;
    private final Utility u;

    private final String MSG_KEY = "message";
    private final String MSG_ACCESS_REJECTED = "Failure: Access rejected";
    private final String MSG_GENERIC_EXCEPTION = "Failure: An unexpected error occurred";
    private final String MSG_NO_RESOURCE = "Failure: Resource not found";
    private final String MSG_CTR_UPDATED = "Success: Country has been updated.";

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String searchCountriesPage(ModelMap map, RedirectAttributes redAtt) {
        try {
            List<Country> countries = countryService.findAll();

            map.addAttribute("countries", countries);
            map.addAttribute("searchCountriesForm", map.getOrDefault("searchCountriesForm", new SearchCountriesForm()));
            map.addAttribute("c_results", map.getOrDefault("c_results", countries));

            map.addAttribute("register_form", map.getOrDefault("register_form", new RegisterCountryForm()));
            map.addAttribute("currencies", currencyService.findAll());

            return "management/searchCountries";
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/";
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{country}")
    public String viewCountryPage(ModelMap map, RedirectAttributes redAtt, @PathVariable(value = "country") Long countryId) {
        try {
            Country country = countryService.findById(countryId);

            map.addAttribute("country", country);
            map.addAttribute("default_currency", currencyService.placeholder().getCode());
            map.addAttribute("hasPresence", countryService.hasPresence(country.getId()));

            map.addAttribute("currencies", currencyService.findAll());
            map.addAttribute("relocation_price", u.roundCurrency(country.getRelocateCarPrice() * country.getCurrency().getExchange()));

            map.addAttribute("details_form", map.getOrDefault("details_form", new UpdateCountryDetailsForm(country)));
            map.addAttribute("currency_form", map.getOrDefault("currency_form", new UpdateCountryCurrencyForm(country)));
            map.addAttribute("relocation_fee_form", map.getOrDefault("relocation_fee_form", new UpdateRelocationFeeForm(country)));
            map.addAttribute("confirm_form", map.getOrDefault("confirm_form", new ConfirmationForm()));

            return "management/viewCountry";
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/";
        }
    }

    // Search page buttons
    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public String searchCountriesButton(@ModelAttribute("searchCountriesForm") @Valid SearchCountriesForm form, Errors err, RedirectAttributes redAtt) {
        try {
            redAtt.addFlashAttribute("searchCountriesForm", form);

            if (err.hasErrors()) {
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-ctr";
            }

            redAtt.addFlashAttribute("c_results", countryService.findByForm(form));
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-ctr";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String countryRegisterButton(@ModelAttribute("register_form") @Valid RegisterCountryForm form, Errors err, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (!employeeService.hasMinimumAuthority(cud, Role.ROLE_ADMIN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-ctr";
            }

            if (err.hasErrors()) {
                redAtt.addFlashAttribute("register_form", form);
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-ctr";
            }

            HttpStatus status = countryService.register(form);

            if (status.equals(HttpStatus.CREATED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Success: Country added.");
            } else if (status.equals(HttpStatus.CONFLICT)) {
                redAtt.addFlashAttribute(MSG_KEY, "Failure: Active country with these specifics already exists.");
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-ctr";
    }

    // View page buttons
    @RequestMapping(value = "/{country}/update-details", method = RequestMethod.POST)
    public String countryDetailsButton(@ModelAttribute("details_form") @Valid UpdateCountryDetailsForm form, Errors err, @PathVariable(value = "country") Long countryId, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (!employeeService.hasMinimumAuthority(cud, Role.ROLE_ADMIN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-ctr/{country}";
            }

            if (err.hasErrors()) {
                redAtt.addFlashAttribute("details_form", form);
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-ctr/{country}";
            }

            HttpStatus status = countryService.updateDetails(countryId, form);

            if (status.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_CTR_UPDATED);
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }
            return "redirect:/mg-ctr/{country}";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-ctr";
    }

    @RequestMapping(value = "/{country}/update-currency", method = RequestMethod.POST)
    public String countryCurrencyButton(@ModelAttribute("currency_form") @Valid UpdateCountryCurrencyForm form, Errors err, @PathVariable(value = "country") Long countryId, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (!employeeService.hasMinimumAuthority(cud, Role.ROLE_ADMIN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-ctr/{country}";
            }

            if (err.hasErrors()) {
                redAtt.addFlashAttribute("currency_form", form);
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-ctr/{country}";
            }

            HttpStatus status = countryService.updateCurrency(countryId, form);

            if (status.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_CTR_UPDATED);
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }
            return "redirect:/mg-ctr/{country}";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-ctr";
    }

    @RequestMapping(value = "/{country}/update-fee", method = RequestMethod.POST)
    public String countryFeeButton(@ModelAttribute("relocation_fee_form") @Valid UpdateRelocationFeeForm form, Errors err, @PathVariable(value = "country") Long countryId, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (!employeeService.hasMinimumAuthority(cud, Role.ROLE_ADMIN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-ctr/{country}";
            }

            if (err.hasErrors()) {
                redAtt.addFlashAttribute("relocation_fee_form", form);
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-ctr/{country}";
            }

            HttpStatus status = countryService.updateRelocationFee(countryId, form);

            if (status.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_CTR_UPDATED);
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }
            return "redirect:/mg-ctr/{country}";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-ctr";
    }

    @RequestMapping(value = "/{country}/update-activity", method = RequestMethod.POST)
    public String countryActivityButton(@ModelAttribute("confirm_form") @Valid ConfirmationForm form, Errors err, @PathVariable(value = "country") Long countryId, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (!employeeService.hasMinimumAuthority(cud, Role.ROLE_ADMIN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-ctr/{country}";
            }

            if (err.hasErrors()) {
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-ctr/{country}";
            }

            HttpStatus status = countryService.updateActivity(countryId);

            if (status.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_CTR_UPDATED);
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }
            return "redirect:/mg-ctr/{country}";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-ctr";
    }

    @RequestMapping(value = "/{country}/delete", method = RequestMethod.POST)
    public String countryDeleteButton(@ModelAttribute("confirm_form") @Valid ConfirmationForm form, Errors err, @PathVariable(value = "country") Long countryId, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (!employeeService.hasMinimumAuthority(cud, Role.ROLE_ADMIN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/mg-ctr/{country}";
            }

            if (err.hasErrors()) {
                redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-ctr/{country}";
            }

            HttpStatus status = countryService.delete(countryId);

            if (status.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Success: Country has been successfully removed.");
                return "redirect:/mg-ctr";
            } else if (status.equals(HttpStatus.PRECONDITION_FAILED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Failure: Country cannot be removed.");
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }
            return "redirect:/mg-ctr/{country}";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-ctr";
    }

}
