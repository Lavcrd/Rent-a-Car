package com.sda.carrental.web.mvc.form;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
@NoArgsConstructor
public class CarFilterForm extends GenericCarForm {
    private IndexForm indexData;

    public CarFilterForm(IndexForm indexData) {
        this.indexData = indexData;
    }
}
