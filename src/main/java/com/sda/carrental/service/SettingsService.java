package com.sda.carrental.service;

import com.sda.carrental.model.company.CompanySettings;
import com.sda.carrental.repository.SettingsRepository;
import com.sda.carrental.web.mvc.form.property.company.UpdateSettingsForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class SettingsService {
    private CompanySettings settings;
    private final SettingsRepository repository;

    @Scheduled(fixedRate = 3 * 60 * 1000)
    public void refreshSettings() {
        settings = get();
    }

    @PostConstruct
    public void init() {
        settings = get();
    }

    public CompanySettings getInstance() {
        if (settings == null) {
            settings = get();
        }
        return settings;
    }

    public CompanySettings get() {
        return repository.get().orElse(new CompanySettings(4, 5, 0.2, 4));
    }

    @Transactional
    public HttpStatus updateDetails(UpdateSettingsForm form) {
        try {
            repository.updateSettings(form.getRefundDepositDeadlineDays(), form.getRefundDepositDeadlineDays(),
                    form.getCancellationFeePercentage(), form.getReservationGap());
            return HttpStatus.ACCEPTED;
        } catch (RuntimeException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}