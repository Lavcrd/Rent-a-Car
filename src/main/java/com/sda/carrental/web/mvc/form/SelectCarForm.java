package com.sda.carrental.web.mvc.form;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SelectCarForm {

    private Long carId;

    private IndexForm indexData;

    public SelectCarForm(IndexForm indexData) {
        this.indexData = indexData;
    }
}
