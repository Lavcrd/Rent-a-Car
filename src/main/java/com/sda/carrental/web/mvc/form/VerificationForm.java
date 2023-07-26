package com.sda.carrental.web.mvc.form;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@RequiredArgsConstructor
public class VerificationForm {

    @NonNull
    @NotNull
    private Long customerId;

    @Pattern(regexp = ".{4,}", message = "Must contain minimum 4 characters.")
    private String personalId;

    @Pattern(regexp = ".{4,}", message = "Must contain minimum 4 characters.")
    private String driverId;
}
