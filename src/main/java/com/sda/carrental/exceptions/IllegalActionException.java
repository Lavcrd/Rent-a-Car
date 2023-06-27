package com.sda.carrental.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class IllegalActionException extends RuntimeException {
    public IllegalActionException(String reason) {
        super(reason);
    }
}