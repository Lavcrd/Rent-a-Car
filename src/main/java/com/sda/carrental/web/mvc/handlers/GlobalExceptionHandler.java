package com.sda.carrental.web.mvc.handlers;

import com.sda.carrental.global.ConstantValues;
import com.sda.carrental.model.property.car.CarBase;
import com.sda.carrental.service.CarBaseService;
import com.sda.carrental.service.DepartmentService;
import com.sda.carrental.web.mvc.form.operational.IndexForm;
import com.sda.carrental.web.mvc.form.property.cars.RegisterCarBaseForm;
import com.sda.carrental.web.mvc.form.property.cars.SearchCarBasesFilterForm;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final ConstantValues cv;
    private final CarBaseService carBaseService;
    private final DepartmentService departmentService;

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus
    public ModelAndView handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex, HttpServletRequest req) {
        String uri = req.getRequestURI();
        ModelAndView mav = new ModelAndView();

        if (uri.equals("/mg-car/car-bases/register")) {
            return refillSearchCarBasePage(mav);
        }
        return refillIndexPage(mav);
    }

    private ModelAndView refillSearchCarBasePage(ModelAndView mav) {
        try {
            List<CarBase> carBases = carBaseService.findAll();


            mav.addObject("results", carBases);

            Map<String, Object> carProperties = carBaseService.getFilterProperties(carBases, true);
            mav.addObject("brands", carProperties.get("brands"));
            mav.addObject("types", carProperties.get("types"));
            mav.addObject("seats", carProperties.get("seats"));
            mav.addObject("years", carProperties.get("years"));

            mav.addObject("all_car_types", CarBase.CarType.values());

            mav.addObject("searchCarBasesForm", new SearchCarBasesFilterForm());
            mav.addObject("register_form", new RegisterCarBaseForm());

            mav.addObject("message", "Failure: File size exceeds permitted maximum of " + cv.getFileSize()/(1024*1024) + "Mb");
            mav.setViewName("management/searchCarBases");

            return mav;
        } catch (RuntimeException err) {
            mav.clear();
            return refillIndexPage(mav);
        }
    }

    private ModelAndView refillIndexPage(ModelAndView mav) {
        mav.addObject("departments", departmentService.findAll());
        mav.addObject("indexForm", new IndexForm(LocalDate.now(), LocalDate.now().plusDays(2)));

        mav.addObject("message", "Failure: Unexpected error");
        mav.setViewName("common/index");
        return mav;
    }
}
