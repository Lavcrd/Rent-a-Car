package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.users.Manager;
import com.sda.carrental.repository.ManagerRepository;
import com.sda.carrental.web.mvc.form.users.SearchEmployeesForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ManagerService {
    private final ManagerRepository repository;

    public Manager findManagerById(Long id) {
        return repository.findManagerById(id).orElseThrow(ResourceNotFoundException::new);
    }

    public List<Manager> findByForm(SearchEmployeesForm form) {
        return repository.findAllByForm(form.getName(), form.getSurname(), form.getDepartment(), form.isExpired());
    }
}
