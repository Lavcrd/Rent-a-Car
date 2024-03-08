package com.sda.carrental.web.mvc.common;

import com.sda.carrental.exceptions.IllegalActionException;
import com.sda.carrental.model.company.CompanySettings;
import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.enums.Role;
import com.sda.carrental.model.property.department.Department;
import com.sda.carrental.model.property.car.CarBase;
import com.sda.carrental.service.*;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.operational.IndexForm;
import com.sda.carrental.web.mvc.form.operational.ReservationForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reservation")
public class ReservationController {

    private final CarBaseService carBaseService;
    private final DepartmentService depService;
    private final CustomerService customerService;
    private final ReservationService reservationService;
    private final SettingsService settingsService;

    private final String MSG_KEY = "message";
    private final String MSG_SESSION_EXPIRED = "The session request might be invalid or could have expired";
    private final String MSG_GENERIC_EXCEPTION = "An unexpected error occurred. Please try again later or contact customer service.";

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String reservationRecapPage(final ModelMap map, HttpSession httpSession, RedirectAttributes redAtt) {
        try {
            IndexForm indexForm = (IndexForm) httpSession.getAttribute("process_indexForm");
            Long carBaseId = (Long) httpSession.getAttribute("process_carBaseId");
            LocalDateTime dateTime1 = (LocalDateTime) httpSession.getAttribute("process_step1_time");
            LocalDateTime dateTime2 = (LocalDateTime) httpSession.getAttribute("process_step2_time");
            LocalDateTime htmlTime1 = LocalDateTime.parse(map.get("s1_time").toString());
            LocalDateTime htmlTime2 = (LocalDateTime) map.getOrDefault("s2_time", dateTime2);
            if (indexForm == null || !dateTime1.isEqual(htmlTime1) || !dateTime2.isEqual(htmlTime2) || !dateTime2.isAfter(dateTime1)) throw new IllegalActionException();

            if (!reservationService.isValidRequest(indexForm, carBaseId, dateTime1, dateTime2)) throw new ResourceNotFoundException();

            map.addAttribute("s1_time", htmlTime1);
            map.addAttribute("s2_time", dateTime2);

            CarBase carBase = carBaseService.findById(carBaseId);
            Department depFrom = depService.findById(indexForm.getDepartmentIdFrom());
            Department depTo = depService.findById(indexForm.getDepartmentIdTo());

            long days = indexForm.getDateFrom().until(indexForm.getDateTo(), ChronoUnit.DAYS) + 1;
            double exchange = depFrom.getCountry().getCurrency().getExchange();
            double multiplier = depFrom.getMultiplier() * exchange;
            CompanySettings cs = settingsService.getInstance();

            if (indexForm.isDifferentDepartment()) {
                map.addAttribute("diff_return_price", depFrom.getCountry().getRelocateCarPrice() * multiplier);
                map.addAttribute("total_price", (depFrom.getCountry().getRelocateCarPrice() + (days * carBase.getPriceDay())) * multiplier);
            } else {
                map.addAttribute("diff_return_price", 0.0);
                map.addAttribute("total_price", days * carBase.getPriceDay() * multiplier);
            }

            map.addAttribute("days", (indexForm.getDateFrom().until(indexForm.getDateTo(), ChronoUnit.DAYS) + 1));
            map.addAttribute("dptF", depFrom);
            map.addAttribute("dptT", depTo);
            map.addAttribute("reservationData", new ReservationForm(carBaseId, indexForm));
            map.addAttribute("carBase", carBase);
            map.addAttribute("raw_price", days * carBase.getPriceDay() * multiplier);
            map.addAttribute("deposit", carBase.getDepositValue() * exchange);
            map.addAttribute("fee_percentage", cs.getCancellationFeePercentage() * 100);
            map.addAttribute("refund_fee_days", cs.getRefundSubtractDaysDuration());
            map.addAttribute("deposit_deadline", cs.getRefundDepositDeadlineDays());

            return "common/reservationRecap";
        } catch (IllegalActionException err) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_SESSION_EXPIRED);
            clearSessionValues(httpSession);
            return "redirect:/";
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            clearSessionValues(httpSession);
            return "redirect:/";
        }
    }

    //Reservation summary buttons
    @RequestMapping(value = "/confirm", method = RequestMethod.POST)
    public String reservationConfirmationButton(@ModelAttribute("reservationData") ReservationForm htmlForm, @RequestParam("s1_time") String htmlTime1Raw, @RequestParam("s2_time") String htmlTime2Raw, HttpSession httpSession, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            //HttpSession should exist
            IndexForm indexForm = (IndexForm) httpSession.getAttribute("process_indexForm");
            Long carBaseId = (Long) httpSession.getAttribute("process_carBaseId");
            LocalDateTime dateTime1 = (LocalDateTime) httpSession.getAttribute("process_step1_time");
            LocalDateTime dateTime2 = (LocalDateTime) httpSession.getAttribute("process_step2_time");
            LocalDateTime htmlTime1 = LocalDateTime.parse(htmlTime1Raw);
            LocalDateTime htmlTime2 = LocalDateTime.parse(htmlTime2Raw);
            if (indexForm == null ||  !dateTime1.isEqual(htmlTime1) || !dateTime2.isEqual(htmlTime2) || !dateTime2.isAfter(dateTime1)) throw new IllegalActionException();

            if (!reservationService.isValidRequest(indexForm, carBaseId, dateTime1, dateTime2)) throw new ResourceNotFoundException();

            //Html variables should exist and HttpSession should be equal during this step - role of html variables are to be valid at the final step of reservation from customer pov
            if (!isIdentical(htmlForm, indexForm, carBaseId)) throw new IllegalActionException();


            HttpStatus status;
            ReservationForm form = new ReservationForm(carBaseId, indexForm);

            if (cud.getAuthorities().contains(new SimpleGrantedAuthority(Role.ROLE_CUSTOMER.name()))) {
                status = customerService.appendReservationToCustomer(cud.getId(), form);
                clearSessionValues(httpSession);
            } else {
                redAtt.addFlashAttribute("reservationDetails", form);
                redAtt.addFlashAttribute("s1_time", htmlTime1);
                redAtt.addFlashAttribute("s2_time", htmlTime2);

                return "redirect:/loc-res";
            }

            if (status.equals(HttpStatus.CREATED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Reservation has been successfully registered!");
                return "redirect:/reservations";
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
                return "redirect:/";
            }
        } catch (IllegalActionException err) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_SESSION_EXPIRED);
            clearSessionValues(httpSession);
            return "redirect:/";
        } catch (RuntimeException err) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            clearSessionValues(httpSession);
            return "redirect:/";
        }
    }

    @RequestMapping(value = "/back", method = RequestMethod.POST)
    public String reservationBackButton(HttpSession httpSession, @RequestParam("s1_time") String htmlTime1, RedirectAttributes redAtt) {
        httpSession.removeAttribute("process_step2_time");
        httpSession.removeAttribute("process_carBaseId");
        redAtt.addFlashAttribute("s1_time", LocalDateTime.parse(htmlTime1));
        return "redirect:/cars";
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
}
