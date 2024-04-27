package com.sda.rentacar;

import com.sda.rentacar.service.auth.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
public class BaseControllerTest {
    @Autowired
    protected MockMvc mvc;

    @MockBean
    protected CustomUserDetailsService customUserDetailsService;
}
