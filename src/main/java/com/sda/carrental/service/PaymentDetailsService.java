package com.sda.carrental.service;

import com.sda.carrental.exceptions.IllegalActionException;
import com.sda.carrental.exceptions.ResourceNotFoundException;
import com.sda.carrental.model.property.car.CarBase;
import com.sda.carrental.model.property.company.Settings;
import com.sda.carrental.global.Utility;
import com.sda.carrental.global.enums.Role;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.property.department.Country;
import com.sda.carrental.model.property.department.Department;
import com.sda.carrental.model.property.payments.Currency;
import com.sda.carrental.model.property.payments.PaymentDetails;
import com.sda.carrental.repository.PaymentDetailsRepository;
import com.sda.carrental.service.auth.CustomUserDetails;
import com.sda.carrental.web.mvc.form.property.payments.PaymentForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final SettingsService settingsService;
    private final Utility u;

    @Transactional
    public void createReservationPayment(Reservation reservation, double payment, double deposit) {
        long days = reservation.getDateFrom().until(reservation.getDateTo(), ChronoUnit.DAYS) + 1;

        Country country = reservation.getDepartmentTake().getCountry();
        Currency currency = country.getCurrency();

        Double exchange = country.getCurrency().getExchange();
        Double multiplier = exchange * reservation.getDepartmentTake().getMultiplier();

        double carPrice = u.roundCurrency(days * reservation.getCarBase().getPriceDay() * multiplier);
        double depositValue = u.roundCurrency(reservation.getCarBase().getDepositValue() * exchange);

        payment = u.roundCurrency(payment);
        deposit = u.roundCurrency(deposit);

        if (!reservation.getDepartmentBack().equals(reservation.getDepartmentTake())) {
            double returnPrice = u.roundCurrency(country.getRelocateCarPrice() * multiplier);
            repository.save(new PaymentDetails(carPrice, returnPrice, depositValue, payment, deposit, reservation.getId(), currency));
        } else {
            repository.save(new PaymentDetails(carPrice, 0.0, depositValue, payment, deposit, reservation.getId(), currency));
        }
    }

    @Transactional
    public void retractReservationPayment(Reservation reservation, Reservation.ReservationStatus requestType) {
        Optional<PaymentDetails> paymentDetailsOptional = getOptionalPaymentDetails(reservation.getId());
        Settings cs = settingsService.getInstance();

        if (paymentDetailsOptional.isEmpty()) {
            return;
        }
        PaymentDetails paymentDetails = paymentDetailsOptional.get();
        if (LocalDate.now().isAfter(reservation.getDateFrom().minusDays(cs.getRefundSubtractDaysDuration())) && requestType.equals(Reservation.ReservationStatus.STATUS_REFUNDED)) {
            paymentDetails.setSecured(paymentDetails.getPayment() * cs.getCancellationFeePercentage());
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
    public HttpStatus processPayment(PaymentDetails pd) {
        try {
            // Would require method to actually send money back to customer account
            pd.setSecured(pd.getPayment());
            pd.setPayment(0);

            double excessDeposit = pd.getDeposit() - pd.getInitialDeposit();

            if (excessDeposit > 0) {
                pd.setDeposit(pd.getInitialDeposit());
                pd.setReleasedDeposit(u.roundCurrency(pd.getReleasedDeposit() + excessDeposit));
            }

            repository.save(pd);
            return HttpStatus.ACCEPTED;
        } catch (RuntimeException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Transactional
    public void adjustPaymentDetails(Reservation reservation, CarBase carBase) {
        Optional<PaymentDetails> opd = repository.findByOperationId(reservation.getId());
        if (opd.isEmpty()) return;

        long days = reservation.getDateFrom().until(reservation.getDateTo(), ChronoUnit.DAYS) + 1;
        Department department = reservation.getDepartmentTake();

        Double multiplier = department.getMultiplier() * department.getCountry().getCurrency().getExchange();

        Double carPrice = carBase.getPriceDay() * days * multiplier;
        Double depositValue = carBase.getDepositValue() * multiplier;

        PaymentDetails pd = opd.get();

        pd.setInitialDeposit(u.roundCurrency(depositValue));
        pd.setInitialCarFee(u.roundCurrency(carPrice));
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
        } catch (ResourceNotFoundException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.NOT_FOUND;
        } catch (IllegalActionException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.NOT_ACCEPTABLE;
        } catch (IllegalArgumentException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.BAD_REQUEST;
        } catch (RuntimeException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Transactional
    public HttpStatus handlePayment(Reservation reservation, PaymentForm form) {
        if (form.isCard()) {
            return cardPayment(reservation, form);
        }
        return cashPayment(reservation, form);
    }

    @Transactional
    public HttpStatus cashPayment(Reservation reservation, PaymentForm form) {
        try {
            double payment = u.roundCurrency(Double.parseDouble(form.getPayment()));
            double deposit = u.roundCurrency(Double.parseDouble(form.getDeposit()));

            Optional<PaymentDetails> opd = getOptionalPaymentDetails(reservation.getId());
            if (opd.isEmpty()) {
                createReservationPayment(reservation, payment, deposit);
                return HttpStatus.CREATED;
            }

            PaymentDetails pd = opd.get();
            pd.setPayment(pd.getPayment() + payment);
            pd.setDeposit(pd.getDeposit() + deposit);
            if (pd.getDeposit() < 0 || pd.getPayment() < 0) throw new IllegalActionException();

            return HttpStatus.ACCEPTED;
        } catch (NumberFormatException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.BAD_REQUEST;
        } catch (IllegalActionException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.NOT_ACCEPTABLE;
        } catch (RuntimeException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    @Transactional
    public HttpStatus cardPayment(Reservation reservation, PaymentForm form) {
        try {
            //an actual copy, but let's say this method invokes another process related to requesting response from card
            double payment = u.roundCurrency(Double.parseDouble(form.getPayment()));
            double deposit = u.roundCurrency(Double.parseDouble(form.getDeposit()));

            Optional<PaymentDetails> opd = getOptionalPaymentDetails(reservation.getId());
            if (opd.isEmpty()) {
                createReservationPayment(reservation, payment, deposit);
                return HttpStatus.CREATED;
            }

            PaymentDetails pd = opd.get();
            pd.setPayment(pd.getPayment() + payment);
            pd.setDeposit(pd.getDeposit() + deposit);
            if (pd.getDeposit() < 0 || pd.getPayment() < 0) throw new IllegalActionException();

            return HttpStatus.ACCEPTED;
        } catch (NumberFormatException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.BAD_REQUEST;
        } catch (IllegalActionException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.NOT_ACCEPTABLE;
        } catch (RuntimeException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    public String[] getPaymentStatus(Long operationId) {
        Optional<PaymentDetails> opd = getOptionalPaymentDetails(operationId);
        if (opd.isEmpty()) {
            return new String[]{"Payment not found", "N/A", "N/A"};
        }

        PaymentDetails pd = opd.get();
        boolean isPaid = pd.getPayment() >= (pd.getInitialCarFee() + pd.getInitialDivergenceFee()) && pd.getDeposit() >= pd.getInitialDeposit();
        String payment = pd.getPayment() + " / " + (pd.getInitialCarFee() + pd.getInitialDivergenceFee());
        String deposit = pd.getDeposit() + " / " + pd.getInitialDeposit();
        if (isPaid) {
            return new String[]{"Paid", payment, deposit};
        }
        return new String[]{"Insufficient", payment, deposit};
    }

    public boolean isDenied(PaymentDetails p, boolean isBypassed) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean hasAuthority = Role.valueOf(cud.getAuthorities().toArray()[0].toString()).ordinal() >= Role.ROLE_MANAGER.ordinal();
        boolean isPaid = p.getPayment() >= (p.getInitialCarFee() + p.getInitialDivergenceFee()) && p.getDeposit() >= p.getInitialDeposit();
        if (!isPaid) {
            return !isBypassed || !hasAuthority;
        }
        return false;
    }

    public HttpStatus securePayment(Reservation r, boolean isBypassed) {
        Optional<PaymentDetails> opd = getOptionalPaymentDetails(r.getId());
        if (opd.isEmpty()) {
            return HttpStatus.PAYMENT_REQUIRED;
        }

        PaymentDetails p = opd.get();
        if (isDenied(p, isBypassed)) return HttpStatus.FORBIDDEN;

        return processPayment(p);
    }
}
