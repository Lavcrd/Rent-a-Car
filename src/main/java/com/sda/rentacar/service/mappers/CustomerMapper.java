package com.sda.rentacar.service.mappers;


import com.sda.rentacar.model.users.Customer;
import com.sda.rentacar.web.mvc.form.operational.LocalReservationForm;
import com.sda.rentacar.web.mvc.form.users.customer.RegisterCustomerForm;

public class CustomerMapper {

    public static Customer toRegisteredEntity(RegisterCustomerForm form) {
        return new Customer(form.getName(), form.getSurname(), Customer.Status.STATUS_REGISTERED, form.getContactNumber());
    }

    public static Customer toGuestEntity(LocalReservationForm form) {
        return new Customer(form.getName(), form.getSurname(), Customer.Status.STATUS_UNREGISTERED, form.getContactNumber());
    }
}
