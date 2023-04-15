package com.sda.carrental.web.mvc;

import com.sda.carrental.constants.GlobalValues;
import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.property.Car;
import com.sda.carrental.model.property.Department;
import com.sda.carrental.service.CarService;
import com.sda.carrental.service.DepartmentService;
import com.sda.carrental.service.ReservationService;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.ShowCarsForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.DecimalFormat;
import java.time.temporal.ChronoUnit;

@Controller
@RequiredArgsConstructor
@RequestMapping("/summary")
public class SummaryController {

    private final CarService carService;
    private final DepartmentService depService;
    private final ReservationService resService;
    private final GlobalValues gv;

    @RequestMapping(method = RequestMethod.GET)
    public String sumReservationPage(final ModelMap map, @ModelAttribute("showData") ShowCarsForm reservationData, RedirectAttributes redAtt) {
        if (reservationData == null) return "redirect:/";
        if (reservationData.getIndexData() == null) return "redirect:/";

        try {
            Car car = carService.findCarById(reservationData.getCarId());
            Department depFrom = depService.findBranchWhereId(reservationData.getIndexData().getDepartmentIdFrom());
            Department depTo = depService.findBranchWhereId(reservationData.getIndexData().getDepartmentIdTo());
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

            return "core/reservationSummary";
        } catch (ResourceNotFoundException err) {
            err.printStackTrace();
            redAtt.addAttribute("message", "Błąd serwera! \nProsimy spróbować później lub skontaktować się telefonicznie.");
            return "redirect:/";
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    public String reservationConfirmation(@ModelAttribute("reservationData") ShowCarsForm form, RedirectAttributes redAtt) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        HttpStatus status = resService.createReservation(cud, form);

        if (status == HttpStatus.CREATED) {
            redAtt.addAttribute("message", "Rezerwacja została pomyślnie zarejestrowana!");
            return "redirect:/reservations";
        } else if (status == HttpStatus.NOT_FOUND) {
            redAtt.addAttribute("message", "Rezerwacja napotkała błąd przy tworzeniu. \nRezerwowany samochód mógł zostać zajęty lub podane dane są nieprawidłowe. \nW razie dalszych kłopotów prosimy skontaktować się telefonicznie.");
            return "redirect:/";
        } else {
            redAtt.addAttribute("message", "Błąd serwera! \nProsimy spróbować później lub skontaktować się z obsługą klienta.");
            return "redirect:/";
        }
    }
    //TODO create "response" handling in index HTML + reservations HTML
}
