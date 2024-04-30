package com.sda.rentacar.web.mvc.management;

import com.sda.rentacar.exceptions.IllegalActionException;
import com.sda.rentacar.exceptions.ResourceNotFoundException;
import com.sda.rentacar.global.Utility;
import com.sda.rentacar.model.property.department.Department;
import com.sda.rentacar.model.property.car.CarBase;
import com.sda.rentacar.service.*;
import com.sda.rentacar.service.auth.CustomUserDetails;
import com.sda.rentacar.web.mvc.form.common.ConfirmationForm;
import com.sda.rentacar.web.mvc.form.common.UpdateImageForm;
import com.sda.rentacar.web.mvc.form.property.cars.*;
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

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mg-car/car-bases")
public class ManageCarBasesController {
    private final Utility utility;
    private final CarService carService;
    private final CarBaseService carBaseService;
    private final EmployeeService employeeService;
    private final CurrencyService currencyService;

    private final String MSG_KEY = "message";
    private final String MSG_GENERIC_EXCEPTION = "Failure: An unexpected error occurred";
    private final String MSG_NO_RESOURCE = "Failure: Not found";

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String searchCarBasesPage(ModelMap map, HttpServletRequest req, RedirectAttributes redAtt) {
        try {
            List<CarBase> carBases = carBaseService.findAllSorted();
            utility.retrieveSessionMessage(map, req);

            map.addAttribute("results", map.getOrDefault("results", carBases));
            map.addAttribute("currency", currencyService.placeholder().getCode());

            Map<String, Object> carProperties = carBaseService.getFilterProperties(carBases, true);
            map.addAttribute("brands", carProperties.get("brands"));
            map.addAttribute("types", carProperties.get("types"));
            map.addAttribute("seats", carProperties.get("seats"));
            map.addAttribute("years", carProperties.get("years"));

            map.addAttribute("all_car_types", CarBase.CarType.values());

            map.addAttribute("searchCarBasesForm", map.getOrDefault("searchCarBasesForm", new SearchCarBasesFilterForm()));
            map.addAttribute("register_form", map.getOrDefault("register_form", new RegisterCarBaseForm()));

            return "management/searchCarBases";
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/";
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{carBaseId}")
    public String viewCarBasePage(ModelMap map, @PathVariable(value = "carBaseId") Long carBaseId, HttpServletRequest req, RedirectAttributes redAtt) {
        try {
            CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            List<Department> departments = employeeService.getDepartmentsByUserContext(cud);
            map.addAttribute("departments", departments);
            map.addAttribute("currency", currencyService.placeholder().getCode());

            utility.retrieveSessionMessage(map, req);

            CarBase carBase = carBaseService.findById(carBaseId);

            map.addAttribute("confirmation_form", new ConfirmationForm());
            map.addAttribute("update_image_form", new UpdateImageForm());
            map.addAttribute("split_form", map.getOrDefault("split_form", new SplitCarBaseForm()));
            map.addAttribute("update_price_form", map.getOrDefault("update_price_form", new UpdateCarBasePricesForm(carBase.getPriceDay().toString(), carBase.getDepositValue().toString())));
            map.addAttribute("register_form", map.getOrDefault("register_form", new RegisterCarForm(carBase.getId())));

            map.addAttribute("result", carBase);
            map.addAttribute("carList", carService.findCarsByDepartmentsAndCarBase(departments, carBase));
            map.addAttribute("patterns", carBaseService.findAllSorted());
            map.addAttribute("resultSize", carService.getCarSizeByCarBase(carBase.getId()));

            return "management/viewCarBase";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
            return "redirect:/mg-car/car-bases";
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            return "redirect:/mg-car/car-bases";
        }
    }

    //Search car bases page buttons
    @RequestMapping(method = RequestMethod.POST, value = "/search")
    public String filterCarBasesButton(@ModelAttribute("searchCarBasesForm") @Valid SearchCarBasesFilterForm form, Errors err, RedirectAttributes redAtt) {
        redAtt.addFlashAttribute("searchCarBasesForm", form);
        if (err.hasErrors()) {
            return "redirect:/mg-car/car-bases";
        }

        redAtt.addFlashAttribute("results", carBaseService.findCarBasesByForm(form));
        return "redirect:/mg-car/car-bases";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/register")
    public String registerCarBaseButton(@ModelAttribute("register_form") @Valid RegisterCarBaseForm form, Errors err, RedirectAttributes redAtt) {
        if (err.hasErrors()) {
            redAtt.addFlashAttribute(MSG_KEY, err.getAllErrors().get(0).getDefaultMessage());
            redAtt.addFlashAttribute("register_form", form);
            return "redirect:/mg-car/car-bases";
        }

        HttpStatus status = carBaseService.register(form);
        if (status.equals(HttpStatus.CREATED)) {
            redAtt.addFlashAttribute(MSG_KEY, "Success: Pattern successfully registered");
        } else {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-car/car-bases";

    }

    //View car base page buttons
    @RequestMapping(method = RequestMethod.POST, value = "/{carBaseId}/split")
    public String splitCarBaseButton(@ModelAttribute("split_form") @Valid SplitCarBaseForm form, Errors errors, RedirectAttributes redAtt, @PathVariable("carBaseId") Long carBaseId) {
        try {
            if (errors.hasErrors()) {
                redAtt.addFlashAttribute("split_form", form);
                redAtt.addFlashAttribute(MSG_KEY, errors.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-car/car-bases/{carBaseId}";
            }

            CarBase cb = carBaseService.findById(carBaseId);
            CarBase tcb = carBaseService.findById(form.getPattern());
            if (cb.getId().equals(tcb.getId())) throw new IllegalActionException();

            HttpStatus status = carService.splitCarsToCarBase(cb, tcb, form.getCars());
            if (status.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Success: Split " + form.getCars().size() + " cars to '" + tcb.getBrand() + " " + tcb.getModel() + " [" + tcb.getYear() + "/" + tcb.getSeats() + "/pd: " +tcb.getPriceDay() + "/d: " +tcb.getDepositValue() + "]' pattern");
                return "redirect:/mg-car/car-bases/{carBaseId}";
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }
            return "redirect:/mg-car/car-bases/{carBaseId}";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (IllegalActionException e) {
            redAtt.addFlashAttribute(MSG_KEY, "Failure: Target pattern is equal to current pattern");
            return "redirect:/mg-car/car-bases/{carBaseId}";
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-car/car-bases";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{carBaseId}/delete")
    public String deleteCarBaseButton(@ModelAttribute("confirmation_form") @Valid ConfirmationForm form, Errors errors, RedirectAttributes redAtt, @PathVariable("carBaseId") Long carBaseId) {
        try {
            if (errors.hasErrors()) {
                redAtt.addFlashAttribute(MSG_KEY, errors.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-car/car-bases/{carBaseId}";
            }

            CarBase cb = carBaseService.findById(carBaseId);
            HttpStatus status = carBaseService.handleCarBaseDelete(cb);
            if (status.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Success: Pattern '" + cb.getBrand() + ' ' + cb.getModel() + "' successfully deleted");
                return "redirect:/mg-car/car-bases";
            } else if (status.equals(HttpStatus.PRECONDITION_FAILED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Failure: Car base cannot be deleted while it has cars");
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }
            return "redirect:/mg-car/car-bases/{carBaseId}";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-car/car-bases";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{carBaseId}/update-image")
    public String updateCarBaseImageButton(@ModelAttribute("update_image_form") @Valid UpdateImageForm form, Errors errors, RedirectAttributes redAtt, @PathVariable("carBaseId") Long carBaseId) {
        try {
            if (errors.hasErrors()) {
                redAtt.addFlashAttribute(MSG_KEY, errors.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-car/car-bases/{carBaseId}";
            }

            CarBase cb = carBaseService.findById(carBaseId);
            HttpStatus status = carBaseService.handleCarBaseImageUpdate(cb, form.getImage());
            if (status.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Success: Image file of '" + cb.getBrand() + ' ' + cb.getModel() + "' successfully updated");
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }
            return "redirect:/mg-car/car-bases/{carBaseId}";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-car/car-bases";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{carBaseId}/update-price")
    public String updateCarBasePricesButton(@ModelAttribute("update_price_form") @Valid UpdateCarBasePricesForm form, Errors errors, RedirectAttributes redAtt, @PathVariable("carBaseId") Long carBaseId) {
        try {
            if (errors.hasErrors()) {
                redAtt.addFlashAttribute("update_price_form", form);
                redAtt.addFlashAttribute(MSG_KEY, errors.getAllErrors().get(0).getDefaultMessage());
                return "redirect:/mg-car/car-bases/{carBaseId}";
            }

            CarBase cb = carBaseService.findById(carBaseId);
            HttpStatus status = carBaseService.handleCarBasePricesUpdate(cb, form);
            if (status.equals(HttpStatus.ACCEPTED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Success: Prices of '" + cb.getBrand() + ' ' + cb.getModel() + "' successfully updated");
                return "redirect:/mg-car/car-bases/{carBaseId}";
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }
            return "redirect:/mg-car/car-bases/{carBaseId}";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-car/car-bases";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{carBaseId}/register-car")
    public String registerCarButton(@ModelAttribute("register_form") @Valid RegisterCarForm form, Errors errors, RedirectAttributes redAtt, @PathVariable("carBaseId") Long carBaseId) {
        try {
            if (errors.hasErrors()) {
                redAtt.addFlashAttribute(MSG_KEY, errors.getAllErrors().get(0).getDefaultMessage());
                redAtt.addFlashAttribute("register_form", form);
                return "redirect:/mg-car/car-bases/{carBaseId}";
            }

            CarBase cb = carBaseService.findById(carBaseId);
            if (!cb.getId().equals(form.getPattern())) throw new IllegalActionException();

            HttpStatus status = carService.register(form);
            if (status.equals(HttpStatus.CREATED)) {
                redAtt.addFlashAttribute(MSG_KEY, "Success: Car " + form.getPlate() + " successfully registered");
            } else {
                redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
            }
            return "redirect:/mg-car/car-bases/{carBaseId}";
        } catch (ResourceNotFoundException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_NO_RESOURCE);
        } catch (RuntimeException e) {
            redAtt.addFlashAttribute(MSG_KEY, MSG_GENERIC_EXCEPTION);
        }
        return "redirect:/mg-car/car-bases";
    }
}
