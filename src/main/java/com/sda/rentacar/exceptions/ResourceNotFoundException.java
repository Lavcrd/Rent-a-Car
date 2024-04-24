package com.sda.rentacar.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String modelName, String columnName, Object value) {
        super(String.format("%s not found with %s : '%s'", modelName, columnName, value));
    }
}