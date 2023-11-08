package com.sda.carrental.web.mvc.form.operational;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SelectCarForm {

    private Long carBaseId;

    private IndexForm indexData;

    public SelectCarForm(IndexForm indexData) {
        this.indexData = indexData;
    }
}
