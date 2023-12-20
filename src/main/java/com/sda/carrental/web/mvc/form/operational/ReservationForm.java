package com.sda.carrental.web.mvc.form.operational;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReservationForm {

    private Long carBaseId;

    private IndexForm indexData;

    public ReservationForm(Long carBaseId, IndexForm indexData) {
        this.carBaseId = carBaseId;
        this.indexData = indexData;
    }
}
