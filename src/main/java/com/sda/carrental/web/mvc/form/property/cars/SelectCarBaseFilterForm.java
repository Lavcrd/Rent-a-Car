package com.sda.carrental.web.mvc.form.property.cars;

import com.sda.carrental.web.mvc.form.operational.IndexForm;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
@NoArgsConstructor
public class SelectCarBaseFilterForm extends GenericCarForm {
    private IndexForm indexData;

    public SelectCarBaseFilterForm(GenericCarForm form, IndexForm indexData) {
        this.indexData = indexData;
        this.setPriceMin(form.getPriceMin());
        this.setPriceMax(form.getPriceMax());
        this.setBrands(form.getBrands());
        this.setTypes(form.getTypes());
        this.setSeats(form.getSeats());
    }
}
