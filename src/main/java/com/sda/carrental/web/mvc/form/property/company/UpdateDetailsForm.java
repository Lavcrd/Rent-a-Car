package com.sda.carrental.web.mvc.form.property.company;

import com.sda.carrental.model.property.company.Company;
import com.sda.carrental.web.mvc.form.common.ConfirmationForm;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
public class UpdateDetailsForm extends ConfirmationForm {
    @NotBlank(message = "Failure: Name field cannot be blank.")
    private String name;

    @NotBlank(message = "Failure: Owner field cannot be blank.")
    private String owner;

    @NotBlank(message = "Failure: Website field cannot be blank.")
    private String website;

    public UpdateDetailsForm(Company company) {
        this.name = company.getName();
        this.owner = company.getOwner();
        this.website = company.getWebsite();
    }
}
