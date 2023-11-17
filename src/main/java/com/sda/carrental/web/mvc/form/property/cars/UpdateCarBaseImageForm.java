package com.sda.carrental.web.mvc.form.property.cars;

import com.sda.carrental.web.mvc.form.common.ConfirmationForm;
import com.sda.carrental.web.mvc.form.validation.constraint.ImageFile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class UpdateCarBaseImageForm extends ConfirmationForm {

    @ImageFile
    private MultipartFile image;
}
