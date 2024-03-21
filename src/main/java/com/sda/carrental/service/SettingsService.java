package com.sda.carrental.service;

import com.sda.carrental.model.property.company.Settings;
import com.sda.carrental.repository.SettingsRepository;
import com.sda.carrental.service.mappers.SettingsMapper;
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
    private Settings settings;
    private final SettingsRepository repository;
    private static final long refreshFrequency = 15 * 60 * 1000;

    @PostConstruct
    public void init() {
        settings = get();
    }

    @Scheduled(fixedRate = refreshFrequency)
    public void refreshSettings() {
        settings = get();
    }

    public Settings getInstance() {
        if (settings == null) {
            settings = get();
        }
        return settings;
    }

    public Settings get() {
        return repository.get().orElse(new Settings(4, 5, 0.2, 4));
    }

    public long getRefreshFrequency() {
        return refreshFrequency;
    }

    @Transactional
    public HttpStatus update(UpdateSettingsForm form) {
        try {
            Settings entity = SettingsMapper.toEntity(form);
            if (repository.existsById(1L)) {
                repository.update(
                        Long.parseLong(form.getRefundDepositDeadlineDays()),
                        Long.parseLong(form.getRefundDepositDeadlineDays()),
                        Double.parseDouble(form.getCancellationFeePercentage()),
                        Integer.parseInt(form.getReservationGap()));
            } else {
                repository.save(entity);
            }

            settings = entity;
            return HttpStatus.ACCEPTED;
        } catch (RuntimeException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}