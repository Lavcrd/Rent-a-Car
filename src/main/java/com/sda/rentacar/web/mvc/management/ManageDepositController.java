package com.sda.rentacar.web.mvc.management;

import com.sda.rentacar.exceptions.ResourceNotFoundException;
import com.sda.rentacar.model.operational.Retrieve;
import com.sda.rentacar.model.property.department.Department;
import com.sda.rentacar.model.property.payments.PaymentDetails;
import com.sda.rentacar.service.*;
import com.sda.rentacar.service.auth.CustomUserDetails;
import com.sda.rentacar.web.mvc.form.property.payments.DepositForm;
import com.sda.rentacar.web.mvc.form.property.payments.SearchDepositsForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mg-depo")
public class ManageDepositController {
    private final RetrieveService retrieveService;
    private final UserService userService;
    private final PaymentDetailsService paymentDetailsService;
    private final EmployeeService employeeService;
    private final CountryService countryService;

    private final String MSG_KEY = "message";
    private final String MSG_ACCESS_REJECTED = "Failure: Access rejected";
    private final String MSG_GENERIC_EXCEPTION = "Failure: An unexpected error occurred";
    private final String MSG_NO_RESOURCE = "Failure: Resource not found";

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String searchDepositsPage(ModelMap map, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            map.addAttribute("results", map.getOrDefault("results", retrieveService.replaceDatesWithDeadlines(retrieveService.findAllUnresolvedByUserContext(cud))));

            List<Department> employeeDepartments = employeeService.getDepartmentsByUserContext(cud);
            map.addAttribute("countries", countryService.findAll());
            map.addAttribute("departments", employeeDepartments);

            map.addAttribute("searchDepositsForm", map.getOrDefault("searchDepositsForm", new SearchDepositsForm()));

            return "management/searchDeposits";
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/";
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{retrieve}")
    public String viewDepositPage(@PathVariable(value = "retrieve") Long retrieveId, ModelMap map, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Retrieve retrieve = retrieveService.findById(retrieveId).orElseThrow(ResourceNotFoundException::new);
            PaymentDetails paymentDetails = paymentDetailsService.getOptionalPaymentDetails(retrieve.getId()).orElseThrow(ResourceNotFoundException::new);
            if (employeeService.departmentAccess(cud, retrieve.getDepartment().getId()).equals(HttpStatus.FORBIDDEN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/";
            }

            map.addAttribute("retrieve", retrieve);
            map.addAttribute("deadline", retrieveService.replaceDatesWithDeadlines(List.of(retrieve)).get(0).getDateTo());
            map.addAttribute("payment_details", paymentDetails);
            map.addAttribute("charged_deposit", paymentDetailsService.calculateOvercharge(paymentDetails));
            map.addAttribute("rent_employee", userService.findById(retrieve.getRent().getEmployeeId()));
            map.addAttribute("retrieve_employee", userService.findById(retrieve.getEmployeeId()));

            map.addAttribute("form", new DepositForm());
            return "management/viewDeposit";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
            return "redirect:/mg-depo";
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/";
        }
    }

    //Search page buttons
    @RequestMapping(method = RequestMethod.POST, value = "/search")
    public String searchDepositsButton(@ModelAttribute("searchDepositsForm") @Valid SearchDepositsForm form, Errors err, RedirectAttributes redAtt) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        redAtt.addFlashAttribute("searchDepositsForm", form);

        if (err.hasErrors()) {
            return "redirect:/mg-depo";
        }

        redAtt.addFlashAttribute("results", retrieveService.replaceDatesWithDeadlines(retrieveService.findUnresolvedByUserContextAndForm(cud, form)));
        return "redirect:/mg-depo";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/check")
    public String viewDepositButton(RedirectAttributes redAtt, @RequestParam("check_button") Long retrieveId) {
        redAtt.addAttribute("retrieve", retrieveId);
        return "redirect:/mg-depo/{retrieve}";
    }

    //Check page buttons
    @RequestMapping(method = RequestMethod.POST, value = "/release")
    public String depositReleaseButton(@ModelAttribute("form") @Valid DepositForm form, Errors err, RedirectAttributes redAtt, @RequestParam("retrieve") Long retrieveId, @RequestParam("customer") Long customerId) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (employeeService.hasNoAccessToCustomerOperation(cud, customerId, retrieveId)) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
            return "redirect:/mg-depo";
        }

        redAtt.addAttribute("retrieve", retrieveId);

        if (err.hasErrors()) {
            redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/mg-depo/{retrieve}";
        }

        HttpStatus status = paymentDetailsService.transferDeposit(retrieveId, form.getValue(), false);
        if (status.equals(HttpStatus.OK)) {
            redAtt.addFlashAttribute(MSG_KEY, "Success: Value successfully released from deposit");
        } else if (status.equals(HttpStatus.NOT_ACCEPTABLE)) {
            redAtt.addFlashAttribute(MSG_KEY, "Failure: Value exceeds deposit");
        } else if (status.equals(HttpStatus.NOT_FOUND)) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } else {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-depo/{retrieve}";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/charge")
    public String depositChargeButton(@ModelAttribute("form") @Valid DepositForm form, Errors err, RedirectAttributes redAtt, @RequestParam("retrieve") Long retrieveId, @RequestParam("customer") Long customerId) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (employeeService.hasNoAccessToCustomerOperation(cud, customerId, retrieveId)) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
            return "redirect:/mg-depo";
        }

        redAtt.addAttribute("retrieve", retrieveId);

        if (err.hasErrors()) {
            redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/mg-depo/{retrieve}";
        }
        HttpStatus status = paymentDetailsService.transferDeposit(retrieveId, form.getValue(), true);

        if (status.equals(HttpStatus.OK)) {
            redAtt.addFlashAttribute(MSG_KEY, "Success: Value successfully charged from deposit");
        } else if (status.equals(HttpStatus.NOT_ACCEPTABLE)) {
            redAtt.addFlashAttribute(MSG_KEY, "Failure: Value exceeds deposit");
        } else if (status.equals(HttpStatus.NOT_FOUND)) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } else {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-depo/{retrieve}";
    }
}
