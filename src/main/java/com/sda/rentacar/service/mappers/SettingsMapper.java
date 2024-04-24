package com.sda.rentacar.service.mappers;

import com.sda.rentacar.model.property.company.Settings;
import com.sda.rentacar.web.mvc.form.property.company.UpdateSettingsForm;

public class SettingsMapper {
    public static Settings toEntity(UpdateSettingsForm form) {
        return new Settings(Long.parseLong(form.getRefundSubtractDaysDuration()), Long.parseLong(form.getRefundDepositDeadlineDays()),
                Double.parseDouble(form.getCancellationFeePercentage()), Integer.parseInt(form.getReservationGap()));
    }
}
