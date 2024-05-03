package com.sda.rentacar.web.mvc.common;

import com.sda.rentacar.BaseControllerTest;
import com.sda.rentacar.global.Utility;
import com.sda.rentacar.service.DepartmentService;
import com.sda.rentacar.web.mvc.form.operational.IndexForm;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = IndexController.class)
public class IndexControllerTest extends BaseControllerTest {
    @MockBean
    private Utility utility;

    @MockBean
    private DepartmentService departmentService;

    @Test
    void shouldReturnValidHtmlDoc() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("<!DOCTYPE html>")))
                .andExpect(view().name("common/index"));
    }

    @Test
    void shouldRedirectOnSuccess() throws Exception {
        mvc.perform(
                post("/")
                        .param("departmentIdFrom", "1")
                        .param("departmentIdTo", "1")
                        .param("differentDepartment", "true")
                        .param("dateFrom", LocalDate.now().plusWeeks(1).toString())
                        .param("dateTo", LocalDate.now().plusWeeks(2).toString()))
                .andExpect(redirectedUrl("/cars"));
    }

    @Test
    void shouldReturnValidIndexPage() throws Exception {
        // GET results
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("departments"))
                .andExpect(model().attributeExists("indexForm"));

        // Additional checks
        verify(utility, times(1)).retrieveSessionMessage(any(), any());
        verify(departmentService).findAll();
    }

    @Test
    void shouldCorrectDepartmentIdParam() throws Exception {
        // Params
        MockHttpSession session = new MockHttpSession();

        // Form pre-correction
        IndexForm form = new IndexForm();
        form.setDepartmentIdFrom(1L);
        form.setDepartmentIdTo(2L); // department in question is different
        form.setDifferentDepartment(false); // assumes same department as return target
        form.setDateFrom(LocalDate.now().plusWeeks(1));
        form.setDateTo(LocalDate.now().plusWeeks(2));

        // POST request
        mvc.perform(post("/")
                .session(session)
                .flashAttr("indexForm", form));

        // Verify change
        IndexForm modifiedForm = (IndexForm) session.getAttribute("process_indexForm");
        assertEquals("Department ID value was not corrected", 1L, modifiedForm.getDepartmentIdTo());
    }

    @Test
    void shouldCorrectDepartmentReturnFeeParam() throws Exception {
        // Params
        MockHttpSession session = new MockHttpSession();

        // Form pre-correction
        IndexForm form = new IndexForm();
        form.setDepartmentIdFrom(1L);
        form.setDepartmentIdTo(1L); // department in question is the same
        form.setDifferentDepartment(true); // assumes different department as return target
        form.setDateFrom(LocalDate.now().plusWeeks(1));
        form.setDateTo(LocalDate.now().plusWeeks(2));

        // POST request
        mvc.perform(post("/")
                .session(session)
                .flashAttr("indexForm", form));

        // Verify change
        IndexForm modifiedForm = (IndexForm) session.getAttribute("process_indexForm");
        assertEquals("Fee boolean value was not corrected", false, modifiedForm.isDifferentDepartment());
    }

    @Test
    void shouldProcessValidForm() throws Exception {
        // Params
        MockHttpSession session = new MockHttpSession();

        // Valid form
        IndexForm form = new IndexForm();
        form.setDepartmentIdFrom(1L);
        form.setDepartmentIdTo(1L);
        form.setDifferentDepartment(true);
        form.setDateFrom(LocalDate.now().plusWeeks(1));
        form.setDateTo(LocalDate.now().plusWeeks(2));

        // POST request
        ResultActions result = mvc.perform(post("/")
                .session(session)
                .flashAttr("indexForm", form));

        // Verify result
        result
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/cars"));

        // Additional checks
        assertNotNull("No session", session);
        assertNotNull("No index form in session", session.getAttribute("process_indexForm"));
        assertNotNull("No timestamp in session", session.getAttribute("process_step1_time"));
        verifyNoInteractions(departmentService);
    }

    @Test
    void shouldReturnToIndexOnInvalidForm() throws Exception {
        // Invalid form
        IndexForm form = new IndexForm();
        form.setDepartmentIdFrom(1L);
        form.setDepartmentIdTo(1L);
        form.setDifferentDepartment(true);
        form.setDateFrom(LocalDate.now().plusWeeks(2));
        form.setDateTo(LocalDate.now().plusWeeks(1));

        // Simulate POST request
        ResultActions result = mvc.perform(post("/")
                .flashAttr("indexForm", form));

        // Verify result
        result
                .andExpect(status().isOk())
                .andExpect(view().name("common/index"))
                .andExpect(model().attributeExists("departments"));

        // Verify departmentService interaction
        verify(departmentService).findAll();
    }
}
