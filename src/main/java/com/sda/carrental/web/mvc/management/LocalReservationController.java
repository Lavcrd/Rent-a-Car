package com.sda.carrental.web.mvc.management;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sda.carrental.exceptions.IllegalActionException;
import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.enums.Country;
import com.sda.carrental.model.users.Customer;
import com.sda.carrental.service.CustomerService;
import com.sda.carrental.service.DepartmentService;
import com.sda.carrental.service.ReservationService;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.LocalReservationForm;
import com.sda.carrental.web.mvc.form.SelectCarForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
@RequestMapping("/loc-res")
public class LocalReservationController {
    private final ReservationService reservationService;
    private final CustomerService customerService;
    private final DepartmentService departmentService;

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String localReservationPage(final ModelMap map, @ModelAttribute("reservationDetails") SelectCarForm form, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (form.getIndexData() == null) throw new IllegalActionException();
            if (departmentService.departmentAccess(cud, form.getIndexData().getDepartmentIdFrom()).equals(HttpStatus.FORBIDDEN)) {
                redAtt.addFlashAttribute("message", "Inaccessible department.");
                return "redirect:/";
            }

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            map.addAttribute("reservationData", objectMapper.writeValueAsString(form));
            map.addAttribute("localReservation", new LocalReservationForm());
            map.addAttribute("countries", Country.values());

            return "management/localReservation";
        } catch (JsonProcessingException | ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again later.");
            return "redirect:/";
        } catch (IllegalActionException err) {
            redAtt.addFlashAttribute("message", "Action not allowed.");
            return "redirect:/";
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/create")
    public String proceedButton(@ModelAttribute("localReservation") @Valid LocalReservationForm form, Errors errors, final ModelMap map, @RequestParam("reservationData") String reservationData, RedirectAttributes redAtt) {
        try {
            if (errors.hasErrors()) {
                map.addAttribute("reservationData", reservationData);
                map.addAttribute("localReservation", form);
                map.addAttribute("countries", Country.values());
                return "management/localReservation";
            }

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            SelectCarForm reservation = objectMapper.readValue(reservationData, SelectCarForm.class);
            form.setReservationForm(reservation);

            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            HttpStatus status = customerService.appendLocalReservationToCustomer(cud, form);

            if (status.equals(HttpStatus.FORBIDDEN)) {
                redAtt.addFlashAttribute("message", "Inaccessible department.");
                return "redirect:/";
            } else if (status.equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
                redAtt.addFlashAttribute("message", "Unexpected error. Operation cancelled.");
                return "redirect:/";
            }

            Customer customer = customerService.findCustomerByVerification(form.getCountry(), form.getPersonalId());
            redAtt.addFlashAttribute("department", reservation.getIndexData().getDepartmentIdFrom());
            redAtt.addAttribute("customer", customer.getId());
            return "redirect:/mg-res/{customer}";
        } catch (JsonProcessingException | ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again later.");
            return "redirect:/";
        }
    }

    @RequestMapping(value="/back", method = RequestMethod.POST)
    public String backButton(@RequestParam("reservationData") String formData, RedirectAttributes redAtt) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        try {
            redAtt.addFlashAttribute("showData", objectMapper.readValue(formData, SelectCarForm.class));
        } catch (JsonProcessingException err) {
            err.printStackTrace();
            redAtt.addFlashAttribute("showData", null);
        }
        return "redirect:/reservation";
    }
}
