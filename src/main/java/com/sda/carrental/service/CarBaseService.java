package com.sda.carrental.service;

import com.sda.carrental.repository.CarBaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CarBaseService {
    private final CarBaseRepository repository;
}
