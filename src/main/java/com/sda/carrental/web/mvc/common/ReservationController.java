package com.sda.carrental.web.mvc.common;

import com.sda.carrental.exceptions.IllegalActionException;
import com.sda.carrental.global.ConstantValues;
import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.property.Department;
import com.sda.carrental.model.property.car.CarBase;
import com.sda.carrental.model.users.User;
import com.sda.carrental.service.*;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.operational.IndexForm;
import com.sda.carrental.web.mvc.form.operational.SelectCarForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

    private final CarBaseService carBaseService;
    private final DepartmentService depService;
    private final CustomerService customerService;
    private final ReservationService reservationService;
    private final ConstantValues cv;

    //Pages
    @RequestMapping(method = RequestMethod.GET)
    public String reservationRecapPage(final ModelMap map, @ModelAttribute("showData") SelectCarForm reservationData, RedirectAttributes redAtt) {
        try {
            if (reservationData == null) throw new IllegalActionException();
            if (reservationData.getIndexData() == null) throw new IllegalActionException();
            IndexForm index = reservationData.getIndexData();
            if (!reservationService.isChronologyValid(index.getDateFrom(), index.getDateTo(), index.getDateCreated())) throw new ResourceNotFoundException();

            CarBase carBase = carBaseService.findById(reservationData.getCarBaseId());
            Department depFrom = depService.findDepartmentWhereId(reservationData.getIndexData().getDepartmentIdFrom());
            Department depTo = depService.findDepartmentWhereId(reservationData.getIndexData().getDepartmentIdTo());
            long days = reservationData.getIndexData().getDateFrom().until(reservationData.getIndexData().getDateTo(), ChronoUnit.DAYS) + 1;

            if (reservationData.getIndexData().isFirstBranchChecked()) {
                map.addAttribute("diff_return_price", cv.getDeptReturnPriceDiff());
                map.addAttribute("total_price", cv.getDeptReturnPriceDiff() + (days * carBase.getPriceDay()));
            } else {
                map.addAttribute("diff_return_price", 0.0);
                map.addAttribute("total_price", days * carBase.getPriceDay());
            }

            map.addAttribute("days", (reservationData.getIndexData().getDateFrom().until(reservationData.getIndexData().getDateTo(), ChronoUnit.DAYS) + 1));
            map.addAttribute("branchFrom", depFrom);
            map.addAttribute("branchTo", depTo);
            map.addAttribute("reservationData", reservationData);
            map.addAttribute("carBase", carBase);
            map.addAttribute("raw_price", days * carBase.getPriceDay());
            map.addAttribute("fee_percentage", cv.getCancellationFeePercentage() * 100);
            map.addAttribute("refund_fee_days", cv.getRefundSubtractDaysDuration());
            map.addAttribute("deposit_deadline", cv.getRefundDepositDeadlineDays());

            return "common/reservationRecap";
        } catch (IllegalActionException | ResourceNotFoundException err) {
            redAtt.addFlashAttribute("message", "An unexpected error occurred. Please try again later or contact customer service.");
            return "redirect:/";
        }
    }

    //Reservation summary buttons
    @RequestMapping(value = "/confirm", method = RequestMethod.POST)
    public String reservationConfirmationButton(@ModelAttribute("reservationData") SelectCarForm form, RedirectAttributes redAtt) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        HttpStatus status;
        if (cud.getAuthorities().contains(new SimpleGrantedAuthority(User.Roles.ROLE_CUSTOMER.name()))) {
            status = customerService.appendReservationToCustomer(cud.getId(), form);
        } else {
            redAtt.addFlashAttribute("reservationDetails", form);
            return "redirect:/loc-res";
        }

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

    @RequestMapping(value = "/back", method = RequestMethod.POST)
    public String reservationBackButton(@ModelAttribute("reservationData") SelectCarForm form, RedirectAttributes redAtt) {
        redAtt.addFlashAttribute("indexData", form.getIndexData());
        return "redirect:/cars";
    }
}
