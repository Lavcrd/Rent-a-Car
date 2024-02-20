package com.sda.carrental.service;

import com.sda.carrental.exceptions.IllegalActionException;
import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.ConstantValues;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.property.PaymentDetails;
import com.sda.carrental.repository.PaymentDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

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
        Double exchange = reservation.getDepartmentTake().getCountry().getExchange();
        Double multiplier = exchange * reservation.getDepartmentTake().getMultiplier();
        double returnPrice = (double) Math.round((cv.getDeptReturnPriceDiff() * multiplier) * 100)/100;

        double rawValue = (double) Math.round((days * reservation.getCarBase().getPriceDay() * multiplier) * 100)/100;
        double depositValue = (double) Math.round((reservation.getCarBase().getDepositValue() * exchange) * 100)/100;
        if (!reservation.getDepartmentBack().equals(reservation.getDepartmentTake())) {
            double payment = rawValue + returnPrice;
            repository.save(new PaymentDetails(rawValue, returnPrice, depositValue, payment, depositValue, reservation));
        } else {
            repository.save(new PaymentDetails(rawValue, 0.0, depositValue, rawValue, depositValue, reservation));
        }
    }

    @Transactional
    public void retractReservationPayment(Reservation reservation, Reservation.ReservationStatus requestType) {
        Optional<PaymentDetails> paymentDetailsOptional = getOptionalPaymentDetails(reservation.getId());
        if (paymentDetailsOptional.isEmpty()) {
            return;
        }
        PaymentDetails paymentDetails = paymentDetailsOptional.get();
        if (LocalDate.now().isAfter(reservation.getDateFrom().minusDays(cv.getRefundSubtractDaysDuration())) && requestType.equals(Reservation.ReservationStatus.STATUS_REFUNDED)) {
            paymentDetails.setSecured(paymentDetails.getPayment() * cv.getCancellationFeePercentage());
        }

        //some method here that would return money to the customer

        paymentDetails.setPayment(0);
        paymentDetails.setDeposit(0);
        repository.save(paymentDetails);
    }

    public Optional<PaymentDetails> getOptionalPaymentDetails(Long operationId) {
        return repository.findByOperationId(operationId);
    }

    public double calculateChargedDeposit(PaymentDetails paymentDetails) {
        return paymentDetails.getInitialDeposit() - paymentDetails.getReleasedDeposit() - paymentDetails.getDeposit();
    }

    @Transactional
    public void securePayment(PaymentDetails paymentDetails) {
        paymentDetails.setSecured(paymentDetails.getPayment());
        paymentDetails.setPayment(0);
        repository.save(paymentDetails);
    }

    @Transactional
    public void adjustRequiredDeposit(Reservation r, Double depositValue) {
        Optional<PaymentDetails> pd = repository.findByOperationId(r.getId());
        if (pd.isEmpty()) return;
        pd.get().setInitialDeposit(depositValue);
        repository.save(pd.get());
    }

    @Transactional
    public HttpStatus transferDeposit(Long operationId, Double value, boolean isCharge) {
        try {
            Optional<PaymentDetails> opd = repository.findByOperationId(operationId);
            if (opd.isEmpty()) throw new ResourceNotFoundException();

            PaymentDetails pd = opd.get();
            if (pd.getDeposit() < value || value < 0) throw new IllegalActionException();

            if (isCharge) {
                pd.setDeposit(Math.round((pd.getDeposit() - value) * 100.0) / 100.0);
                pd.setSecured(Math.round((pd.getSecured() + value) * 100.0) / 100.0);
            } else {
                pd.setDeposit(Math.round((pd.getDeposit() - value) * 100.0) / 100.0);
                pd.setReleasedDeposit(Math.round((pd.getReleasedDeposit() + value) * 100.0) / 100.0);
            }

            repository.save(pd);

            return HttpStatus.OK;
        } catch (ResourceNotFoundException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.NOT_FOUND;
        } catch (IllegalActionException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.NOT_ACCEPTABLE;
        } catch (RuntimeException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
