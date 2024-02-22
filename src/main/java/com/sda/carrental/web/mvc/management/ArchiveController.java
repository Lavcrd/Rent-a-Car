package com.sda.carrental.web.mvc.management;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.operational.Rent;
import com.sda.carrental.model.operational.Retrieve;
import com.sda.carrental.model.property.Department;
import com.sda.carrental.model.property.PaymentDetails;
import com.sda.carrental.service.*;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.operational.SearchArchiveForm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/archive")
public class ArchiveController {
    private final UserService userService;
    private final EmployeeService employeeService;
    private final RetrieveService retrieveService;
    private final RentService rentService;
    private final PaymentDetailsService paymentDetailsService;
    private final CountryService countryService;

    private final String MSG_KEY = "message";
    private final String MSG_GENERIC_EXCEPTION = "Failure: An unexpected error occurred";
    private final String MSG_NO_RESOURCE = "Failure: Resource not found";

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String searchArchivePage(ModelMap map, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            SearchArchiveForm defaultForm = new SearchArchiveForm(LocalDate.now().minusMonths(1), LocalDate.now().plusDays(1));

            map.addAttribute("results", map.getOrDefault("results", retrieveService.findByUserContextAndForm(cud, defaultForm)));

            List<Department> employeeDepartments = employeeService.getDepartmentsByUserContext(cud);
            map.addAttribute("countries", countryService.findAll());
            map.addAttribute("departments", employeeDepartments);

            map.addAttribute("searchArchiveForm", map.getOrDefault("searchArchiveForm", defaultForm));

            return "management/searchArchive";
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/";
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{operation}")
    public String viewOperationPage(@PathVariable(value = "operation") Long operationId, ModelMap map, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Optional<Retrieve> retrieveOptional = retrieveService.findById(operationId);

            Rent rent;
            if (retrieveOptional.isEmpty()) {
                rent = rentService.findById(operationId);
            } else {
                Retrieve retrieve = retrieveOptional.get();
                rent = retrieve.getRent();
                map.addAttribute("retrieve", retrieve);
                map.addAttribute("retrieve_employee", userService.findById(retrieve.getEmployeeId()));
            }

            map.addAttribute("isObscured", employeeService.hasNoAccessToCustomerOperation(cud, rent.getReservation().getCustomer().getId(), operationId));
            map.addAttribute("previousPage", map.getOrDefault("previousPage", "/archive"));

            PaymentDetails paymentDetails = paymentDetailsService.getOptionalPaymentDetails(operationId).orElseThrow(ResourceNotFoundException::new);
            map.addAttribute("charged_deposit", paymentDetailsService.calculateOvercharge(paymentDetails));

            map.addAttribute("rent", rent);
            map.addAttribute("isComplete", retrieveOptional.isPresent());
            map.addAttribute("payment_details", paymentDetails);
            map.addAttribute("rent_employee", userService.findById(rent.getEmployeeId()));

            return "management/viewOperation";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
            return "redirect:/";
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/";
        }
    }

    //Search page buttons
    @RequestMapping(method = RequestMethod.POST, value = "/search")
    public String searchArchiveButton(@ModelAttribute("searchArchiveForm") @Valid SearchArchiveForm form, Errors err, RedirectAttributes redAtt) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        redAtt.addFlashAttribute("searchArchiveForm", form);

        if (err.hasErrors()) {
            return "redirect:/archive";
        }

        redAtt.addFlashAttribute("results", retrieveService.findByUserContextAndForm(cud, form));
        return "redirect:/archive";
    }
}
