package com.sda.carrental.web.mvc.form;

import com.sda.carrental.web.mvc.form.validation.constraint.CurrentEmail;
import com.sda.carrental.web.mvc.form.validation.constraint.MatchingEmail;
import com.sda.carrental.web.mvc.form.validation.constraint.UniqueUsername;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@MatchingEmail(message = "E-mail confirmation does not match.")
public class ChangeEmailForm {

    @NotEmpty(message = "Field cannot be empty")
    @CurrentEmail
    private String currentEmail;

    @NotEmpty(message = "Field cannot be empty")
    @Email(message = "Login should be a valid email address format")
    @UniqueUsername(message = "E-mail is already taken")
    private String newEmail;

    @NotEmpty(message = "Field cannot be empty")
    @Email(message = "Login should be a valid email address format")
    private String newEmailRepeat;
}