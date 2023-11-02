package com.sda.carrental.web.mvc.form;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SearchCarBasesForm extends GenericCarForm {

    private Integer depositMin;

    private Integer depositMax;

    private List<Integer> years;
}
