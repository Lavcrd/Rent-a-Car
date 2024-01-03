package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.users.Coordinator;
import com.sda.carrental.repository.CoordinatorRepository;
import com.sda.carrental.web.mvc.form.users.SearchEmployeesForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CoordinatorService {
    private final CoordinatorRepository repository;

    public Coordinator findCoordinatorById(Long id) {
        return repository.findCoordinatorById(id).orElseThrow(ResourceNotFoundException::new);
    }

    public List<Coordinator> findByForm(SearchEmployeesForm form) {
        return repository.findAllByForm(form.getName(), form.getSurname(), form.getDepartment(), form.isExpired());
    }
}
