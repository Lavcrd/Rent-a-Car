package com.sda.carrental.service;

import com.sda.carrental.exceptions.IllegalActionException;
import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.global.ConstantValues;
import com.sda.carrental.global.Utility;
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
    private final Utility u;

    @Transactional
    public void createReservationPayment(Reservation reservation) {
        long days = reservation.getDateFrom().until(reservation.getDateTo(), ChronoUnit.DAYS) + 1;

        Double exchange = reservation.getDepartmentTake().getCountry().getExchange();
        Double multiplier = exchange * reservation.getDepartmentTake().getMultiplier();

        double rawValue = u.roundCurrency(days * reservation.getCarBase().getPriceDay() * multiplier);
        double depositValue = u.roundCurrency(reservation.getCarBase().getDepositValue() * exchange);

        if (!reservation.getDepartmentBack().equals(reservation.getDepartmentTake())) {
            double returnPrice = u.roundCurrency(cv.getDeptReturnPriceDiff() * multiplier);
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

    public double calculateOvercharge(PaymentDetails paymentDetails) {
        double payment = paymentDetails.getInitialCarFee() + paymentDetails.getInitialDivergenceFee();
        return paymentDetails.getSecured() - payment;
    }

    @Transactional
    public void processPayment(PaymentDetails pd) {
        // Would require method to actually send money back to customer account
        pd.setSecured(pd.getPayment());
        pd.setPayment(0);

        double excessDeposit = pd.getDeposit() - pd.getInitialDeposit();

        if (excessDeposit > 0) {
            pd.setDeposit(pd.getInitialDeposit());
            pd.setReleasedDeposit(u.roundCurrency(pd.getReleasedDeposit() + excessDeposit));
        }

        repository.save(pd);
    }

    @Transactional
    public void adjustRequiredDeposit(Reservation r, Double depositValue) {
        Optional<PaymentDetails> opd = repository.findByOperationId(r.getId());
        if (opd.isEmpty()) return;

        PaymentDetails pd = opd.get();
        pd.setInitialDeposit(u.roundCurrency(depositValue));
        repository.save(pd);
    }

    @Transactional
    public HttpStatus transferDeposit(Long operationId, String deposit, boolean isCharge) {
        try {
            double value = u.roundCurrency(Double.parseDouble(deposit));
            Optional<PaymentDetails> opd = repository.findByOperationId(operationId);
            if (opd.isEmpty()) throw new ResourceNotFoundException();

            PaymentDetails pd = opd.get();
            if (pd.getDeposit() < value || value < 0) throw new IllegalActionException();

            if (isCharge) {
                pd.setDeposit(u.roundCurrency(pd.getDeposit() - value));
                pd.setSecured(u.roundCurrency(pd.getSecured() + value));
            } else {
                pd.setDeposit(u.roundCurrency(pd.getDeposit() - value));
                pd.setReleasedDeposit(u.roundCurrency(pd.getReleasedDeposit() + value));
            }

            repository.save(pd);

            return HttpStatus.OK;
        } catch (ResourceNotFoundException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.NOT_FOUND;
        } catch (IllegalActionException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.NOT_ACCEPTABLE;
        } catch (IllegalArgumentException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.BAD_REQUEST;
        } catch (RuntimeException err) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
