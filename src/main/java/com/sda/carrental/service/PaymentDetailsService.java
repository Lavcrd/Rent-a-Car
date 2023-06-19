package com.sda.carrental.service;

import com.sda.carrental.global.ConstantValues;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.property.PaymentDetails;
import com.sda.carrental.repository.PaymentDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class PaymentDetailsService {
    private final PaymentDetailsRepository repository;
    private final ConstantValues cv;

    @Transactional
    public void createReservationPayment(Reservation reservation) {
        long days = reservation.getDateFrom().until(reservation.getDateTo(), ChronoUnit.DAYS) + 1;

        double rawValue = days * reservation.getCar().getPrice_day();
        double payment = rawValue;
        double depositValue = reservation.getCar().getDepositValue();
        if (!reservation.getDepartmentBack().equals(reservation.getDepartmentTake())) {
            payment += cv.getDeptReturnPriceDiff();
            repository.save(new PaymentDetails(rawValue, cv.getDeptReturnPriceDiff(), depositValue, payment, depositValue, reservation));
        } else {
            repository.save(new PaymentDetails(rawValue, 0.0, depositValue, payment, depositValue, reservation));
        }
    }

    @Transactional
    public void retractReservationPayment(Reservation reservation, Reservation.ReservationStatus requestType) {
        Optional<PaymentDetails> paymentDetailsOptional = getOptionalPaymentDetails(reservation);
        if(paymentDetailsOptional.isEmpty()) {
            return;
        }
        PaymentDetails paymentDetails = paymentDetailsOptional.get();
        if (LocalDate.now().isAfter(reservation.getDateFrom().minusDays(cv.getRefundSubtractDaysDuration())) && requestType.equals(Reservation.ReservationStatus.STATUS_REFUNDED)) {
            paymentDetails.setSecuredValue(paymentDetails.getMainValue() * cv.getDepositPercentage());
        }

        //some method here that would return money to the customer

        paymentDetails.setMainValue(0);
        paymentDetails.setDeposit(0);
        repository.save(paymentDetails);
    }

    public Optional<PaymentDetails> getOptionalPaymentDetails(Reservation reservation) {
        return repository.findByReservation(reservation);
    }

    @Transactional
    public void securePayment(PaymentDetails paymentDetails) {
        paymentDetails.setSecuredValue(paymentDetails.getMainValue());
        paymentDetails.setMainValue(0);
        repository.save(paymentDetails);
    }

    @Transactional
    public void adjustRequiredDeposit(Reservation r, Double dv) {
        Optional<PaymentDetails> pd = repository.findByReservation(r);
        if(pd.isEmpty()) return;
        pd.get().setRequiredDeposit(dv);
        repository.save(pd.get());
    }
}
