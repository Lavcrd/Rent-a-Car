package com.sda.carrental.web.mvc;

import com.sda.carrental.constants.GlobalValues;
import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.property.Car;
import com.sda.carrental.model.property.Department;
import com.sda.carrental.service.CarService;
import com.sda.carrental.service.DepartmentService;
import com.sda.carrental.service.ReservationService;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.SelectCarForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.temporal.ChronoUnit;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reservation")
public class ReservationController {

    private final CarService carService;
    private final DepartmentService depService;
    private final ReservationService resService;
    private final GlobalValues gv;

    @RequestMapping(method = RequestMethod.GET)
    public String sumReservationPage(final ModelMap map, @ModelAttribute("showData") SelectCarForm reservationData, RedirectAttributes redAtt) {
        if (reservationData == null) return "redirect:/";
        if (reservationData.getIndexData() == null) return "redirect:/";

        try {
            Car car = carService.findCarById(reservationData.getCarId());
            Department depFrom = depService.findDepartmentWhereId(reservationData.getIndexData().getDepartmentIdFrom());
            Department depTo = depService.findDepartmentWhereId(reservationData.getIndexData().getDepartmentIdTo());
            long days = reservationData.getIndexData().getDateFrom().until(reservationData.getIndexData().getDateTo(), ChronoUnit.DAYS) + 1;

            if (reservationData.getIndexData().isFirstBranchChecked()) {
                map.addAttribute("diff_return_price", gv.getDeptReturnPriceDiff());
                map.addAttribute("total_price", gv.getDeptReturnPriceDiff() + (days * car.getPrice_day()));
            } else {
                map.addAttribute("diff_return_price", 0.0);
                map.addAttribute("total_price", days * car.getPrice_day());
            }

            map.addAttribute("days", (reservationData.getIndexData().getDateFrom().until(reservationData.getIndexData().getDateTo(), ChronoUnit.DAYS) + 1));
            map.addAttribute("branchFrom", depFrom);
            map.addAttribute("branchTo", depTo);
            map.addAttribute("reservationData", reservationData);
            map.addAttribute("car", car);
            map.addAttribute("raw_price", days * car.getPrice_day());
            map.addAttribute("deposit_percentage", gv.getDepositPercentage() * 100);
            map.addAttribute("refund_fee_days", gv.getRefundSubtractDaysDuration());

            return "core/reservation";
        } catch (ResourceNotFoundException err) {
            err.printStackTrace();
            redAtt.addFlashAttribute("message", "Server error! \nPlease contact customer service or try again later.");
            return "redirect:/";
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    public String reservationConfirmation(@ModelAttribute("reservationData") SelectCarForm form, RedirectAttributes redAtt) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        HttpStatus status = resService.createReservation(cud, form);

        if (status == HttpStatus.CREATED) {
            redAtt.addFlashAttribute("message", "Reservation has been successfully registered!");
            return "redirect:/reservations";
        } else if (status == HttpStatus.NOT_FOUND) {
            redAtt.addFlashAttribute("message", "Reservation encountered an error while creating. \nIn case of further problems, please contact us by phone.");
            return "redirect:/";
        } else {
            redAtt.addFlashAttribute("message", "Server error! \nPlease contact customer service or try again later.");
            return "redirect:/";
        }
    }
}
