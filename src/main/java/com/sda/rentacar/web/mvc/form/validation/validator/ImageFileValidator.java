package com.sda.rentacar.web.mvc.form.validation.validator;

import com.sda.rentacar.web.mvc.form.validation.constraint.ImageFile;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ImageFileValidator implements ConstraintValidator<ImageFile, MultipartFile> {

    @Override
    public void initialize(ImageFile constraint) {
    }

    @Override
    public boolean isValid(MultipartFile input, ConstraintValidatorContext cvc) {
        try {
            String filename = input.getOriginalFilename();
            return filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".png");
        } catch (RuntimeException e) {
            return false;
        }
    }
}
