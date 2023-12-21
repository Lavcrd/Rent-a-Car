package com.sda.carrental.web.mvc.management;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sda.carrental.exceptions.IllegalActionException;
import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.enums.Country;
import com.sda.carrental.model.users.Customer;
import com.sda.carrental.service.CustomerService;
import com.sda.carrental.service.DepartmentService;
import com.sda.carrental.service.ReservationService;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.service.mappers.CustomObjectMapper;
import com.sda.carrental.web.mvc.form.operational.IndexForm;
import com.sda.carrental.web.mvc.form.operational.LocalReservationForm;
import com.sda.carrental.web.mvc.form.operational.ReservationForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
@RequestMapping("/loc-res")
public class LocalReservationController {
    private final CustomerService customerService;
    private final DepartmentService departmentService;
    private final ReservationService reservationService;

    private final String MSG_KEY = "message";
    private final String MSG_ACCESS_REJECTED = "Failure: Access rejected";
    private final String MSG_GENERIC_EXCEPTION = "Failure: An unexpected error occurred";
    private final String MSG_SESSION_EXPIRED = "The session request might be invalid or could have expired";
    private final String MSG_NO_RESOURCE = "Failure: Resource not found";

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String localReservationPage(final ModelMap map, @ModelAttribute("reservationDetails") ReservationForm htmlForm, HttpSession httpSession, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (departmentService.departmentAccess(cud, htmlForm.getIndexData().getDepartmentIdFrom()).equals(HttpStatus.FORBIDDEN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                clearSessionValues(httpSession);
                return "redirect:/";
            }

            LocalDateTime htmlTime1 = LocalDateTime.parse(map.get("s1_time").toString());
            LocalDateTime htmlTime2 = LocalDateTime.parse(map.get("s2_time").toString());

            //Throws exception if invalid values
            checkValidity(httpSession, htmlTime1.toString(), htmlTime2.toString(), htmlForm);

            map.addAttribute("s1_time", htmlTime1);
            map.addAttribute("s2_time", htmlTime2);

            ObjectMapper objectMapper = new CustomObjectMapper();
            map.addAttribute("reservationData", objectMapper.writeValueAsString(htmlForm));
            map.addAttribute("localReservation", new LocalReservationForm());
            map.addAttribute("countries", Country.values());

            return "management/localReservation";
        } catch (IllegalActionException err) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_SESSION_EXPIRED);
            clearSessionValues(httpSession);
            return "redirect:/";
        } catch (JsonProcessingException | RuntimeException err) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            clearSessionValues(httpSession);
            return "redirect:/";
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/proceed")
    public String proceedButton(@ModelAttribute("localReservation") @Valid LocalReservationForm form, Errors errors, @RequestParam("s1_time") String htmlTime1Raw, @RequestParam("s2_time") String htmlTime2Raw, HttpSession httpSession, final ModelMap map, @RequestParam("reservationData") String reservationData, RedirectAttributes redAtt) {
        try {
            if (errors.hasErrors()) {
                map.addAttribute("reservationData", reservationData);
                map.addAttribute("localReservation", form);
                map.addAttribute("countries", Country.values());
                map.addAttribute("s1_time", LocalDateTime.parse(htmlTime1Raw));
                map.addAttribute("s2_time", LocalDateTime.parse(htmlTime2Raw));
                return "management/localReservation";
            }

            ObjectMapper objectMapper = new CustomObjectMapper();
            ReservationForm reservation = objectMapper.readValue(reservationData, ReservationForm.class);

            //Throws exception if invalid values
            checkValidity(httpSession, htmlTime1Raw, htmlTime2Raw, reservation);

            redAtt.addFlashAttribute("s1_time", LocalDateTime.parse(htmlTime1Raw));
            redAtt.addFlashAttribute("s2_time", LocalDateTime.parse(htmlTime2Raw));

            form.setReservationForm(reservation);

            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            HttpStatus status = customerService.appendLocalReservationToCustomer(cud, form);
            clearSessionValues(httpSession);

            if (status.equals(HttpStatus.FORBIDDEN)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_ACCESS_REJECTED);
                return "redirect:/";
            } else if (status.equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
                return "redirect:/";
            } else if (status.equals(HttpStatus.NOT_FOUND)) {
                redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
                return "redirect:/";
            } else if (status.equals(HttpStatus.CREATED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Success: Reservation applied to created guest");
            } else if (status.equals(HttpStatus.OK)) {
                redAtt.addFlashAttribute(MSG_KEY, "Success: Reservation applied to existing user");
            }

            Customer customer = customerService.findCustomerByVerification(Country.valueOf(form.getCountry()), form.getPersonalId());
            redAtt.addAttribute("department", reservation.getIndexData().getDepartmentIdFrom());
            redAtt.addAttribute("customer", customer.getId());
            return "redirect:/mg-res/{department}-{customer}";
        } catch (IllegalActionException err) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_SESSION_EXPIRED);
            clearSessionValues(httpSession);
            return "redirect:/";
        } catch (JsonProcessingException | RuntimeException err) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            clearSessionValues(httpSession);
            return "redirect:/";
        }
    }

    @RequestMapping(value = "/back", method = RequestMethod.POST)
    public String backButton(@RequestParam("reservationData") String formData, @RequestParam("s1_time") String htmlTime1Raw, @RequestParam("s2_time") String htmlTime2Raw, HttpSession httpSession, RedirectAttributes redAtt) {
        ObjectMapper objectMapper = new CustomObjectMapper();

        try {
            ReservationForm htmlForm = objectMapper.readValue(formData, ReservationForm.class);

            //Throws exception if invalid values
            checkValidity(httpSession, htmlTime1Raw, htmlTime2Raw, htmlForm);
            redAtt.addFlashAttribute("s1_time", LocalDateTime.parse(htmlTime1Raw));
            redAtt.addFlashAttribute("s2_time", LocalDateTime.parse(htmlTime2Raw));

        } catch (JsonProcessingException err) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            clearSessionValues(httpSession);
            return "redirect:/";
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_SESSION_EXPIRED);
            clearSessionValues(httpSession);
            return "redirect:/";
        }
        return "redirect:/reservation";
    }

    private void clearSessionValues(HttpSession httpSession) {
        httpSession.removeAttribute("process_indexForm");
        httpSession.removeAttribute("process_carBaseId");
        httpSession.removeAttribute("process_step1_time");
        httpSession.removeAttribute("process_step2_time");
    }

    private boolean isIdentical(ReservationForm htmlForm, IndexForm sessionIndex, Long sessionCarBaseId) {
        // Checks if required values are null
        if (htmlForm.getIndexData() == null || sessionIndex == null || htmlForm.getCarBaseId() == null || sessionCarBaseId == null) {
            return false;
        }

        // Compare index values
        return Objects.equals(htmlForm.getIndexData().getDateFrom(), sessionIndex.getDateFrom())
                && Objects.equals(htmlForm.getIndexData().getDateTo(), sessionIndex.getDateTo())
                && Objects.equals(htmlForm.getIndexData().getDepartmentIdFrom(), sessionIndex.getDepartmentIdFrom())
                && Objects.equals(htmlForm.getIndexData().getDepartmentIdTo(), sessionIndex.getDepartmentIdTo())
                && htmlForm.getIndexData().isDifferentDepartment() == sessionIndex.isDifferentDepartment();
    }

    private void checkValidity(HttpSession httpSession, String htmlTime1Raw, String htmlTime2Raw, ReservationForm htmlForm) throws IllegalActionException, ResourceNotFoundException, RuntimeException{
        //HttpSession should exist
        IndexForm indexForm = (IndexForm) httpSession.getAttribute("process_indexForm");
        Long carBaseId = (Long) httpSession.getAttribute("process_carBaseId");
        LocalDateTime dateTime1 = (LocalDateTime) httpSession.getAttribute("process_step1_time");
        LocalDateTime dateTime2 = (LocalDateTime) httpSession.getAttribute("process_step2_time");
        LocalDateTime htmlTime1 = LocalDateTime.parse(htmlTime1Raw);
        LocalDateTime htmlTime2 = LocalDateTime.parse(htmlTime2Raw);
        if (indexForm == null ||  !dateTime1.isEqual(htmlTime1) || !dateTime2.isEqual(htmlTime2) || !dateTime2.isAfter(dateTime1)) throw new IllegalActionException();

        if (!reservationService.isValidRequest(indexForm, carBaseId, dateTime1, dateTime2)) throw new ResourceNotFoundException();

        //Html variables should exist and HttpSession should be equal during this step
        if (!isIdentical(htmlForm, indexForm, carBaseId)) throw new IllegalActionException();
    }

}
