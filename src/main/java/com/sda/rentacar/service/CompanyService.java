package com.sda.rentacar.service;

import com.sda.rentacar.model.property.company.Company;
import com.sda.rentacar.repository.CompanyRepository;
import com.sda.rentacar.web.mvc.form.property.company.UpdateDetailsForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepository repository;


    public Company get() {
        return repository.get().orElse(new Company("—", "—", "—", "—"));
    }

    @Transactional
    public HttpStatus updateLogotype(MultipartFile image) {
        try {
            // Method would add new file and replace company image address with new one (external storage system)
            repository.updateLogotypeReference(image.getName() + " reference");
            return HttpStatus.ACCEPTED;
        } catch (RuntimeException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Transactional
    public HttpStatus updateDetails(UpdateDetailsForm form) {
        try {
            repository.updateDetails(form.getName(), form.getOwner(), form.getWebsite());
            return HttpStatus.ACCEPTED;
        } catch (RuntimeException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}