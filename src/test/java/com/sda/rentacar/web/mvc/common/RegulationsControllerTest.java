package com.sda.rentacar.web.mvc.common;

import com.sda.rentacar.BaseControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RegulationsController.class)
public class RegulationsControllerTest extends BaseControllerTest {

    @Test
    void shouldReturnValidHtmlDoc() throws Exception {
        mvc.perform(get("/regulations"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("<!DOCTYPE html>")))
                .andExpect(view().name("common/regulations"));
    }
}
