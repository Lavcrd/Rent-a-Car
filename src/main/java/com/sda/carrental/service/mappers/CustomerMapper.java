package com.sda.carrental.service.mappers;


import com.sda.carrental.model.users.Customer;
import com.sda.carrental.web.mvc.form.LocalReservationForm;
import com.sda.carrental.web.mvc.form.RegisterCustomerForm;

public class CustomerMapper {

    public static Customer toEntity(RegisterCustomerForm form) {
        return new Customer(form.getName(), form.getSurname(), form.getCountry(), form.getCity(), Customer.CustomerStatus.STATUS_REGISTERED, form.getAddress(), form.getContactNumber());
    }

    public static Customer toGuestEntity(LocalReservationForm form) {
        return new Customer(form.getName(), form.getSurname(), form.getCountry(), form.getCity(), Customer.CustomerStatus.STATUS_UNREGISTERED, form.getAddress(), form.getContactNumber());
    }
}
