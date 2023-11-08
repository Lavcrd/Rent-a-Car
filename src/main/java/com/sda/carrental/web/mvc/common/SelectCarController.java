package com.sda.carrental.web.mvc.common;

import com.sda.carrental.exceptions.IllegalActionException;
import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.property.car.CarBase;
import com.sda.carrental.service.CarBaseService;
import com.sda.carrental.service.DepartmentService;
import com.sda.carrental.web.mvc.form.property.cars.SelectCarBaseFilterForm;
import com.sda.carrental.web.mvc.form.operational.IndexForm;
import com.sda.carrental.web.mvc.form.operational.SelectCarForm;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cars")
public class SelectCarController {

    private final CarBaseService carBaseService;
    private final DepartmentService departmentService;

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String selectCarPage(final ModelMap map, RedirectAttributes redAtt, @ModelAttribute("indexData") IndexForm indexData) {
        try {
            if (indexData.getDateCreated() == null) throw new IllegalActionException();
            List<CarBase> carBaseList = carBaseService.findAvailableCarBasesInCountry(
                    indexData.getDateFrom(),
                    indexData.getDateTo(),
                    departmentService.findDepartmentWhereId(indexData.getDepartmentIdFrom()).getCountry());
            if (carBaseList.isEmpty()) throw new ResourceNotFoundException();

            map.addAttribute("carBases", map.getOrDefault("filteredCarBases", carBaseList));

            Map<String, Object> carProperties = carBaseService.getFilterProperties(carBaseList, false);

            map.addAttribute("brands", carProperties.get("brands"));
            map.addAttribute("types", carProperties.get("types"));
            map.addAttribute("seats", carProperties.get("seats"));

            map.addAttribute("days", (indexData.getDateFrom().until(indexData.getDateTo(), ChronoUnit.DAYS) + 1));

            map.addAttribute("selectCarForm", new SelectCarForm(indexData));
            map.addAttribute("carFilterForm", map.getOrDefault("carFilterForm", new SelectCarBaseFilterForm(indexData)));

            return "common/selectCar";
        } catch (IllegalActionException err) {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again later or contact customer service.");
            return "redirect:/";
        } catch (ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "No cars available for selected department and dates.");
            return "redirect:/";
        }
    }

    //Select car buttons
    @RequestMapping(value = "/proceed", method = RequestMethod.POST)
    public String selectButton(@ModelAttribute("selectCarForm") SelectCarForm selectCarData, @RequestParam(value = "select") Long carBaseId, RedirectAttributes redAtt) {
        selectCarData.setCarBaseId(carBaseId);
        redAtt.addFlashAttribute("showData", selectCarData);
        return "redirect:/reservation";
    }

    @RequestMapping(value = "/filter", method = RequestMethod.POST)
    public String filterCarsButton(@ModelAttribute("carFilterForm") SelectCarBaseFilterForm filterData, RedirectAttributes redirect) {
        redirect.addFlashAttribute("filteredCarBases", carBaseService.findCarBasesByForm(filterData));
        redirect.addFlashAttribute("indexData", filterData.getIndexData());
        redirect.addFlashAttribute("carFilterForm", filterData);
        return "redirect:/cars";
    }
}