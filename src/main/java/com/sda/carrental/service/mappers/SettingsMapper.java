package com.sda.carrental.service.mappers;

import com.sda.carrental.model.property.company.Settings;
import com.sda.carrental.web.mvc.form.property.company.UpdateSettingsForm;

public class SettingsMapper {
    public static Settings toEntity(UpdateSettingsForm form) {
        return new Settings(Long.parseLong(form.getRefundSubtractDaysDuration()), Long.parseLong(form.getRefundDepositDeadlineDays()),
                Double.parseDouble(form.getCancellationFeePercentage()), Integer.parseInt(form.getReservationGap()));
    }
}
