package com.sda.rentacar.web.mvc.common;

import com.sda.rentacar.BaseControllerTest;
import com.sda.rentacar.global.Utility;
import com.sda.rentacar.service.DepartmentService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = IndexController.class)
public class IndexControllerTest extends BaseControllerTest {
    @MockBean
    private Utility utility;

    @MockBean
    private DepartmentService departmentService;

    @Test
    void indexPageHasAnHtmlForm() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("<form action=\"/\" method=\"POST\">")));
    }

    @Test
    void findButtonShouldWork() throws Exception {
        mvc.perform(
                post("/")
                        .param("departmentIdFrom", "1")
                        .param("departmentIdTo", "1")
                        .param("differentDepartment", "true")
                        .param("dateFrom", LocalDate.now().plusWeeks(1).toString())
                        .param("dateTo", LocalDate.now().plusWeeks(2).toString()))
                .andExpect(redirectedUrl("/cars"));
    }
}
