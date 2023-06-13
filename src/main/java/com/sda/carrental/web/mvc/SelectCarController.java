package com.sda.carrental.web.mvc;

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

    @RequestMapping(method = RequestMethod.GET)
    public String showPage(final ModelMap map, @ModelAttribute("indexData") IndexForm indexData) {
        if (indexData.getDateCreated() == null) {
            return "redirect:/";
        }
        List<Car> carList = carService.findAvailableDistinctCarsInDepartment(
                indexData.getDateFrom(),
                indexData.getDateTo(),
                indexData.getDepartmentIdFrom());

        if (!map.containsKey("filteredCars")) {
            map.addAttribute("cars", carList);
        } else {
            map.addAttribute("cars", map.getAttribute("filteredCars"));
        }
        Map<String,Object> carProperties = carService.getFilterProperties(carList);

        map.addAttribute("brand", carProperties.get("brand"));
        map.addAttribute("type", carProperties.get("type"));
        map.addAttribute("seats", carProperties.get("seats"));

        map.addAttribute("days", (indexData.getDateFrom().until(indexData.getDateTo(), ChronoUnit.DAYS) + 1));

        SelectCarForm selectCarForm = new SelectCarForm();
        CarFilterForm carFilterForm = new CarFilterForm();

        selectCarForm.setIndexData(indexData);
        carFilterForm.setIndexData(indexData);

        map.addAttribute("selectCarForm", selectCarForm);
        map.addAttribute("carFilterForm", carFilterForm);
        return "core/selectCar";
    }

    @RequestMapping(value="/proceed", method = RequestMethod.POST)
    public String showHandler(@ModelAttribute("selectCarForm") SelectCarForm selectCarData, @RequestParam(value = "car_button") Long carId, RedirectAttributes redirectAttributes) {
        if (selectCarData == null) return "redirect:/";
        selectCarData.setCarId(carId);
        if (selectCarData.getIndexData() == null) return "redirect:/";

        redirectAttributes.addFlashAttribute("showData", selectCarData);
        return "redirect:/reservation";
    }

    @RequestMapping(value="/filter", method = RequestMethod.POST)
    public String filterCars(@ModelAttribute("carFilterForm") CarFilterForm filterData, RedirectAttributes redirect) {
        redirect.addFlashAttribute("filteredCars", carService.filterCars(filterData));
        redirect.addFlashAttribute("indexData", filterData.getIndexData());
        return "redirect:/cars";
    }
}