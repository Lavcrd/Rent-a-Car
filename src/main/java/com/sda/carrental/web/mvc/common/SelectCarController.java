package com.sda.carrental.web.mvc.common;

import com.sda.carrental.exceptions.IllegalActionException;
import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.property.Car;
import com.sda.carrental.service.CarService;
import com.sda.carrental.web.mvc.form.CarFilterForm;
import com.sda.carrental.web.mvc.form.IndexForm;
import com.sda.carrental.web.mvc.form.SelectCarForm;
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

    private final CarService carService;

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String selectCarPage(final ModelMap map, RedirectAttributes redAtt, @ModelAttribute("indexData") IndexForm indexData) {
        try {
            if (indexData.getDateCreated() == null) throw new IllegalActionException();
            List<Car> carList = carService.findAvailableDistinctCarsInDepartment(
                    indexData.getDateFrom(),
                    indexData.getDateTo(),
                    indexData.getDepartmentIdFrom());
            if (carList.isEmpty()) throw new ResourceNotFoundException();

            map.addAttribute("cars", map.getOrDefault("filteredCars", carList));

            Map<String, Object> carProperties = carService.getFilterProperties(carList);

            map.addAttribute("brands", carProperties.get("brands"));
            map.addAttribute("types", carProperties.get("types"));
            map.addAttribute("seats", carProperties.get("seats"));

            map.addAttribute("days", (indexData.getDateFrom().until(indexData.getDateTo(), ChronoUnit.DAYS) + 1));

            map.addAttribute("selectCarForm", new SelectCarForm(indexData));
            map.addAttribute(map.getOrDefault("carFilterForm", new CarFilterForm(indexData)));

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
    public String selectButton(@ModelAttribute("selectCarForm") SelectCarForm selectCarData, @RequestParam(value = "select") Long carId, RedirectAttributes redAtt) {
        selectCarData.setCarId(carId);
        redAtt.addFlashAttribute("showData", selectCarData);
        return "redirect:/reservation";
    }

    @RequestMapping(value = "/filter", method = RequestMethod.POST)
    public String filterCarsButton(@ModelAttribute("carFilterForm") CarFilterForm filterData, RedirectAttributes redirect) {
        redirect.addFlashAttribute("filteredCars", carService.findCarsByForm(filterData));
        redirect.addFlashAttribute("indexData", filterData.getIndexData());
        redirect.addFlashAttribute("carFilterForm", filterData);
        return "redirect:/cars";
    }
}