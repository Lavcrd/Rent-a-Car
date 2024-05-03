package com.sda.rentacar.web.mvc.common;

import com.sda.rentacar.BaseControllerTest;
import com.sda.rentacar.model.property.department.Country;
import com.sda.rentacar.model.property.department.Department;
import com.sda.rentacar.model.property.payments.Currency;
import com.sda.rentacar.service.CountryService;
import com.sda.rentacar.service.DepartmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ContactController.class)
public class ContactControllerTest extends BaseControllerTest {
    @MockBean
    private DepartmentService departmentService;

    @MockBean
    private CountryService countryService;

    @BeforeEach
    void setup() {
        when(departmentService.findAllWhereCountry(any())).thenReturn(
                List.of(new Department(new Country("", "", "", new Currency()), "", "", "", "", "", "", false)));
        when(departmentService.findAllWhereCountryAndHq(any())).thenReturn(
                new Department(new Country("", "", "", new Currency()), "", "", "", "", "", "", true));
    }

    @Test
    void shouldReturnValidHtmlDoc() throws Exception {
        mvc.perform(get("/contact"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("<!DOCTYPE html>")))
                .andExpect(view().name("common/contact"));
    }

    @Test
    void shouldReturnValidContactPage() throws Exception {
        // GET results
        mvc.perform(get("/contact"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("dealer"))
                .andExpect(model().attributeExists("hq"))
                .andExpect(model().attributeDoesNotExist("message"));

        // Additional checks
        verify(departmentService, times(1)).findAllWhereCountry(any());
        verify(departmentService, times(1)).findAllWhereCountryAndHq(any());
    }

    @Test
    void shouldRedirectOnException() throws Exception {
        when(departmentService.findAllWhereCountry(any())).thenThrow(new RuntimeException());

        mvc.perform(get("/contact"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(view().name("redirect:/"));
    }
}
