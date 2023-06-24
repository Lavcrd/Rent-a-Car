package com.sda.carrental.service;

import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.users.Coordinator;
import com.sda.carrental.repository.CoordinatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CoordinatorService {
    private final CoordinatorRepository repository;

    public Coordinator findCoordinatorById(Long id) {
        return repository.findCoordinatorById(id).orElseThrow(ResourceNotFoundException::new);
    }
}
