package com.sda.carrental.web.mvc.form.property.cars;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SearchCarBasesFilterForm extends GenericCarForm {

    private Double depositMin;

    private Double depositMax;

    private List<Integer> years;
}
