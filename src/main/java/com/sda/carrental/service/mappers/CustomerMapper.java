package com.sda.carrental.service.mappers;


import com.sda.carrental.model.users.Customer;
import com.sda.carrental.web.mvc.form.RegisterCustomerForm;

public class CustomerMapper {

    public static Customer toEntity(RegisterCustomerForm form) {
        return new Customer(form.getEmail(), form.getPassword(), form.getName(), form.getSurname(), form.getCountry(), form.getAddress());
    }
}
