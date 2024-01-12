package com.sda.carrental.service.mappers;


import com.sda.carrental.model.users.Customer;
import com.sda.carrental.web.mvc.form.operational.LocalReservationForm;
import com.sda.carrental.web.mvc.form.users.RegisterCustomerForm;

public class CustomerMapper {

    public static Customer toRegisteredEntity(RegisterCustomerForm form) {
        return new Customer(form.getName(), form.getSurname(), Customer.Status.STATUS_REGISTERED, form.getContactNumber());
    }

    public static Customer toGuestEntity(LocalReservationForm form) {
        return new Customer(form.getName(), form.getSurname(), Customer.Status.STATUS_UNREGISTERED, form.getContactNumber());
    }
}
