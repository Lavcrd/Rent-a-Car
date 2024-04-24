package com.sda.rentacar.web.mvc.form.users;

import com.sda.rentacar.web.mvc.form.common.ConfirmationForm;
import com.sda.rentacar.web.mvc.form.validation.constraint.MatchingPassword;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Size;

@Getter
@Setter
@MatchingPassword(message = "Provided new password fields do not match", targetFormClasses = {ChangePasswordForm.class})
public class ChangePasswordForm extends ConfirmationForm {

    @Size(min = 8, message = "Minimum length: 8")
    private String newPassword;

    @Size(min = 8, message = "Minimum length: 8")
    private String newPasswordRepeat;
}
