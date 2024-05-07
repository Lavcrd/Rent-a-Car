package com.sda.rentacar.web.mvc.common;

import com.sda.rentacar.BaseControllerTest;
import com.sda.rentacar.service.DepartmentService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;


import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ErrorController.class)
public class ErrorControllerTest extends BaseControllerTest {
    // Test for handling invalid URL is unsupported
    @MockBean
    private DepartmentService departmentService;


    @Test
    void shouldReturnValidHtmlDoc() throws Exception {
        mvc.perform(get("/error"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("<!DOCTYPE html>")))
                .andExpect(view().name("common/index"));
    }

    @Test
    void shouldReturnValidIndexPage() throws Exception {
        // GET results
        mvc.perform(get("/error"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("departments"))
                .andExpect(model().attributeExists("message"))
                .andExpect(model().attributeExists("indexForm"));

        // Additional checks
        verify(departmentService).findAll();
    }
}
