package com.sda.rentacar.web.mvc.common;

import com.sda.rentacar.exceptions.IllegalActionException;
import com.sda.rentacar.exceptions.ResourceNotFoundException;
import com.sda.rentacar.model.property.department.Department;
import com.sda.rentacar.model.property.car.CarBase;
import com.sda.rentacar.service.CarBaseService;
import com.sda.rentacar.service.DepartmentService;
import com.sda.rentacar.web.mvc.form.property.cars.SelectCarBaseFilterForm;
import com.sda.rentacar.web.mvc.form.operational.IndexForm;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cars")
public class SelectCarController {

    private final CarBaseService carBaseService;
    private final DepartmentService departmentService;

    private final String MSG_KEY = "message";
    private final String MSG_SESSION_EXPIRED = "The session request might be invalid or could have expired";
    private final String MSG_GENERIC_EXCEPTION = "An unexpected error occurred. Please try again later or contact customer service.";

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String selectCarPage(final ModelMap map, HttpSession httpSession, RedirectAttributes redAtt) {
        try {
            IndexForm indexForm = (IndexForm) httpSession.getAttribute("process_indexForm");
            LocalDateTime dateTime = (LocalDateTime) httpSession.getAttribute("process_step1_time");
            if (indexForm == null || dateTime == null) throw new IllegalActionException();

            Department department = departmentService.findById(indexForm.getDepartmentIdFrom());
            map.addAttribute("currency", department.getCountry().getCurrency().getCode());
            map.addAttribute("exchange", department.getCountry().getCurrency().getExchange());
            map.addAttribute("multiplier", department.getMultiplier() * department.getCountry().getCurrency().getExchange());

            List<CarBase> carBaseList = carBaseService.findAvailableCarBasesInCountry(
                    indexForm.getDateFrom(),
                    indexForm.getDateTo(),
                    department.getCountry());
            if (carBaseList.isEmpty()) throw new ResourceNotFoundException();

            LocalDateTime htmlTime1 = (LocalDateTime) map.getOrDefault("s1_time", dateTime);
            if (!htmlTime1.isEqual(dateTime)) throw new IllegalActionException();

            map.addAttribute("carBases", map.getOrDefault("filteredCarBases", carBaseList));
            map.addAttribute("s1_time", htmlTime1);

            Map<String, Object> carProperties = carBaseService.getFilterProperties(carBaseList, false);

            map.addAttribute("brands", carProperties.get("brands"));
            map.addAttribute("types", carProperties.get("types"));
            map.addAttribute("seats", carProperties.get("seats"));

            map.addAttribute("days", (indexForm.getDateFrom().until(indexForm.getDateTo(), ChronoUnit.DAYS) + 1));

            map.addAttribute("carFilterForm", map.getOrDefault("carFilterForm", new SelectCarBaseFilterForm()));

            return "common/selectCar";
        }  catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, "No cars available for selected department and dates.");
            return "redirect:/";
        } catch (IllegalActionException | NullPointerException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_SESSION_EXPIRED);
            return "redirect:/";
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/";
        }
    }

    //Select car buttons
    @RequestMapping(value = "/proceed", method = RequestMethod.POST)
    public String selectButton(@RequestParam(value = "select") Long carBaseId, @RequestParam(value = "s1_time") String htmlTime1Raw, HttpSession httpSession, RedirectAttributes redAtt) {
        try {
            IndexForm indexForm = (IndexForm) httpSession.getAttribute("process_indexForm");
            LocalDateTime dateTime = (LocalDateTime) httpSession.getAttribute("process_step1_time");
            LocalDateTime htmlTime1 = LocalDateTime.parse(htmlTime1Raw);
            if (indexForm == null || dateTime == null || !dateTime.isEqual(htmlTime1)) throw new IllegalActionException();
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_SESSION_EXPIRED);
            return "redirect:/";
        }

        redAtt.addFlashAttribute("s1_time", htmlTime1Raw);
        httpSession.setAttribute("process_carBaseId", carBaseId);
        httpSession.setAttribute("process_step2_time", LocalDateTime.now());
        return "redirect:/reservation";
    }

    @RequestMapping(value = "/filter", method = RequestMethod.POST)
    public String filterCarsButton(@ModelAttribute("carFilterForm") SelectCarBaseFilterForm filterData, @RequestParam(value = "s1_time") String htmlTime1Raw, HttpSession httpSession, RedirectAttributes redAtt) {
        try {
            IndexForm indexForm = (IndexForm) httpSession.getAttribute("process_indexForm");
            LocalDateTime dateTime = (LocalDateTime) httpSession.getAttribute("process_step1_time");
            LocalDateTime htmlTime1 = LocalDateTime.parse(htmlTime1Raw);
            if (indexForm == null || dateTime == null || !dateTime.isEqual(htmlTime1)) throw new IllegalActionException();

            SelectCarBaseFilterForm queryForm = new SelectCarBaseFilterForm(filterData, indexForm);

            redAtt.addFlashAttribute("s1_time", htmlTime1);
            redAtt.addFlashAttribute("filteredCarBases", carBaseService.findCarBasesByForm(queryForm));
            redAtt.addFlashAttribute("carFilterForm", filterData);
            return "redirect:/cars";
        } catch (IllegalActionException | NullPointerException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_SESSION_EXPIRED);
            return "redirect:/";
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/";
        }
    }
}