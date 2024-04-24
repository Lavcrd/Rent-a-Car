package com.sda.rentacar.service;

import com.sda.rentacar.exceptions.IllegalActionException;
import com.sda.rentacar.exceptions.ResourceNotFoundException;
import com.sda.rentacar.model.property.car.CarBase;
import com.sda.rentacar.model.property.company.Settings;
import com.sda.rentacar.global.Utility;
import com.sda.rentacar.global.enums.Role;
import com.sda.rentacar.model.operational.Reservation;
import com.sda.rentacar.model.property.department.Country;
import com.sda.rentacar.model.property.department.Department;
import com.sda.rentacar.model.property.payments.Currency;
import com.sda.rentacar.model.property.payments.PaymentDetails;
import com.sda.rentacar.repository.PaymentDetailsRepository;
import com.sda.rentacar.service.auth.CustomUserDetails;
import com.sda.rentacar.web.mvc.form.property.payments.PaymentForm;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
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
            double cancellationFee = paymentDetails.getPaymentBalance() * cs.getCancellationFeePercentage();
            paymentDetails.setSecured(cancellationFee);
            paymentDetails.setPaymentAccepted(cancellationFee);
        }

        //some method here that would return money to the customer

        paymentDetails.setPaymentBalance(0);
        paymentDetails.setDeposit(0);
        repository.save(paymentDetails);
    }

    public Optional<PaymentDetails> getOptionalPaymentDetails(Long operationId) {
        return repository.findByOperationId(operationId);
    }

    public double calculateOvercharge(PaymentDetails paymentDetails) {
        return paymentDetails.getSecured() - paymentDetails.getPaymentAccepted();
    }

    @Transactional
    public HttpStatus processPayment(PaymentDetails pd) {
        try {
            // Would require method to actually send money back to customer account
            pd.setSecured(pd.getPaymentBalance());
            pd.setPaymentAccepted(pd.getPaymentBalance());
            pd.setPaymentBalance(0);

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
            pd.setPaymentBalance(pd.getPaymentBalance() + payment);
            pd.setDeposit(pd.getDeposit() + deposit);
            if (pd.getDeposit() < 0 || pd.getPaymentBalance() < 0) throw new IllegalActionException();

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
            pd.setPaymentBalance(pd.getPaymentBalance() + payment);
            pd.setDeposit(pd.getDeposit() + deposit);
            if (pd.getDeposit() < 0 || pd.getPaymentBalance() < 0) throw new IllegalActionException();

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
        boolean isPaid = pd.getPaymentBalance() >= (pd.getInitialCarFee() + pd.getInitialDivergenceFee()) && pd.getDeposit() >= pd.getInitialDeposit();
        String payment = pd.getPaymentBalance() + " / " + (pd.getInitialCarFee() + pd.getInitialDivergenceFee());
        String deposit = pd.getDeposit() + " / " + pd.getInitialDeposit();
        if (isPaid) {
            return new String[]{"Paid", payment, deposit};
        }
        return new String[]{"Insufficient", payment, deposit};
    }

    public boolean isDenied(PaymentDetails p, boolean isBypassed) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean hasAuthority = Role.valueOf(cud.getAuthorities().toArray()[0].toString()).ordinal() >= Role.ROLE_MANAGER.ordinal();
        boolean isPaid = p.getPaymentBalance() >= (p.getInitialCarFee() + p.getInitialDivergenceFee()) && p.getDeposit() >= p.getInitialDeposit();
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

    public void mapStatistics(Map<String, Double> map, Long departmentId, LocalDate dateFrom, LocalDate dateTo) {
        CustomUserDetails cud = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Object[] sr = (Object[]) repository.getDepartmentServiceStatistics(departmentId, dateFrom, dateTo);
        Object[] dr = (Object[]) repository.getDepartmentDepositStatistics(departmentId, dateFrom, dateTo);

        Object[] asr = (Object[]) repository.getAccountServiceStatistics(cud.getId(), dateFrom, dateTo);
        Object[] adr = (Object[]) repository.getAccountDepositStatistics(cud.getId(), dateFrom, dateTo);

        Object[] gsr = (Object[]) repository.getGlobalServiceStatistics(dateFrom, dateTo);
        Object[] gdr = (Object[]) repository.getGlobalDepositStatistics(dateFrom, dateTo);

        mapDepartmentStatistics(map, sr, dr);
        mapAccountStatistics(map, asr, adr);
        mapGlobalStatistics(map, gsr, gdr);
    }

    private void mapDepartmentStatistics(Map<String, Double> map, Object[] sr, Object[] dr) {
        double rentalPayment = sr[0] != null ? (double) sr[0] : 0.0;
        double cancellationPayment = sr[1] != null ? (double) sr[1] : 0.0;
        double additionalNegative = sr[2] != null ? (double) sr[2] : 0.0;
        double additionalPositive = sr[3] != null ? (double) sr[3] : 0.0;
        double carFees = sr[4] != null ? (double) sr[4] : 0.0;
        double divergenceFees = sr[5] != null ? (double) sr[5] : 0.0;

        double pendingDeposit = dr[0] != null ? (double) dr[0] : 0.0;
        double releasedDeposit = dr[0] != null ? (double) dr[1] : 0.0;
        double chargedDeposit = dr[0] != null ? (double) dr[2] : 0.0;

        double revenue = rentalPayment + cancellationPayment;
        double totalRevenue = revenue + chargedDeposit;
        double cad = carFees + divergenceFees;

        map.put("total_revenue", totalRevenue);
        map.put("pending_deposit", pendingDeposit);
        map.put("released_deposit", releasedDeposit);
        map.put("charged_deposit", chargedDeposit);
        map.put("summary_payment", revenue);
        map.put("cancellation_payment", cancellationPayment);
        map.put("car_fees", cad == 0 ? 0 : carFees / cad * revenue);
        map.put("divergence_fees", cad == 0 ? 0 : divergenceFees / cad * revenue);
        map.put("add_negatives", additionalNegative);
        map.put("add_positives", additionalPositive);
    }

    private void mapAccountStatistics(Map<String, Double> map, Object[] asr, Object[] adr) {
        double accountRentalPayment = asr[0] != null ? (double) asr[0] : 0.0;
        double accountCancellationPayment = asr[1] != null ? (double) asr[1] : 0.0;
        double accountAdditionalNegative = asr[2] != null ? (double) asr[2] : 0.0;
        double accountAdditionalPositive = asr[3] != null ? (double) asr[3] : 0.0;
        double accountCarFees = asr[4] != null ? (double) asr[4] : 0.0;
        double accountDivergenceFees = asr[5] != null ? (double) asr[5] : 0.0;

        double accountPendingDeposit = adr[0] != null ? (double) adr[0] : 0.0;
        double accountReleasedDeposit = adr[0] != null ? (double) adr[1] : 0.0;
        double accountChargedDeposit = adr[0] != null ? (double) adr[2] : 0.0;

        double accountRevenue = accountRentalPayment + accountCancellationPayment;
        double accountTotalRevenue = accountRevenue + accountChargedDeposit;
        double acad = accountCarFees + accountDivergenceFees;

        map.put("account_total_revenue", accountTotalRevenue);
        map.put("account_pending_deposit", accountPendingDeposit);
        map.put("account_released_deposit", accountReleasedDeposit);
        map.put("account_charged_deposit", accountChargedDeposit);
        map.put("account_summary_payment", accountRevenue);
        map.put("account_cancellation_payment", accountCancellationPayment);
        map.put("account_car_fees", acad == 0 ? 0 : accountCarFees / acad * accountRevenue);
        map.put("account_divergence_fees", acad == 0 ? 0 : accountDivergenceFees / acad * accountRevenue);
        map.put("account_add_negatives", accountAdditionalNegative);
        map.put("account_add_positives", accountAdditionalPositive);
    }

    private void mapGlobalStatistics(Map<String, Double> map, Object[] gsr, Object[] gdr) {
        double globalRentalPayment = gsr[0] != null ? (double) gsr[0] : 0.0;
        double globalCancellationPayment = gsr[1] != null ? (double) gsr[1] : 0.0;
        double globalAdditionalNegative = gsr[2] != null ? (double) gsr[2] : 0.0;
        double globalAdditionalPositive = gsr[3] != null ? (double) gsr[3] : 0.0;
        double globalCarFees = gsr[4] != null ? (double) gsr[4] : 0.0;
        double globalDivergenceFees = gsr[5] != null ? (double) gsr[5] : 0.0;

        double globalPendingDeposit = gdr[0] != null ? (double) gdr[0] : 0.0;
        double globalReleasedDeposit = gdr[0] != null ? (double) gdr[1] : 0.0;
        double globalChargedDeposit = gdr[0] != null ? (double) gdr[2] : 0.0;

        double globalRevenue = globalRentalPayment + globalCancellationPayment;
        double globalTotalRevenue = globalRevenue + globalChargedDeposit;
        double gcad = globalCarFees + globalDivergenceFees;

        map.put("global_total_revenue", globalTotalRevenue);
        map.put("global_pending_deposit", globalPendingDeposit);
        map.put("global_released_deposit", globalReleasedDeposit);
        map.put("global_charged_deposit", globalChargedDeposit);
        map.put("global_summary_payment", globalRevenue);
        map.put("global_cancellation_payment", globalCancellationPayment);
        map.put("global_car_fees", gcad == 0 ? 0 : globalCarFees / gcad * globalRevenue);
        map.put("global_divergence_fees", gcad == 0 ? 0 : globalDivergenceFees / gcad * globalRevenue);
        map.put("global_add_negatives", globalAdditionalNegative);
        map.put("global_add_positives", globalAdditionalPositive);
    }
}
