package com.sda.carrental.global;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@NoArgsConstructor
public class ConstantValues {
    private double deptReturnPriceDiff = 120;
    private long refundSubtractDaysDuration = 4;
    private double depositPercentage = 0.2;
}
