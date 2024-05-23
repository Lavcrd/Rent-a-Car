package com.sda.rentacar.web.mvc.common;

import com.sda.rentacar.BaseControllerTest;
import com.sda.rentacar.exceptions.ResourceNotFoundException;
import com.sda.rentacar.model.property.car.CarBase;
import com.sda.rentacar.model.property.company.Settings;
import com.sda.rentacar.model.property.department.Country;
import com.sda.rentacar.model.property.department.Department;
import com.sda.rentacar.model.property.payments.Currency;
import com.sda.rentacar.model.users.Customer;
import com.sda.rentacar.model.users.Employee;
import com.sda.rentacar.model.users.auth.Credentials;
import com.sda.rentacar.service.*;
import com.sda.rentacar.service.auth.CustomUserDetails;
import com.sda.rentacar.web.mvc.form.operational.IndexForm;
import com.sda.rentacar.web.mvc.form.operational.ReservationForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpSession;

import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.util.AssertionErrors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ReservationController.class)
public class ReservationControllerTest extends BaseControllerTest {

    private static MockHttpSession session;

    private static LocalDateTime time1;

    private static LocalDateTime time2;

    private static final CustomUserDetails employee = new CustomUserDetails(
            new Credentials(1L, "", ""),
            new Employee("", "", "", Collections.emptyList(),LocalDate.of(9999, 1, 1), "")
    );

    private static final CustomUserDetails customer = new CustomUserDetails(
            new Credentials(1L, "", ""),
            new Customer("", "", Customer.Status.STATUS_REGISTERED, "")
    );

    @MockBean
    private CarBaseService carBaseService;

    @MockBean
    private DepartmentService depService;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private ReservationService reservationService;

    @MockBean
    private SettingsService settingsService;

    private IndexForm getValidIndexForm() {
        IndexForm form = new IndexForm();
        form.setDepartmentIdFrom(1L);
        form.setDepartmentIdTo(1L);
        form.setDifferentDepartment(false);
        form.setDateFrom(LocalDate.now().plusWeeks(1));
        form.setDateTo(LocalDate.now().plusWeeks(2));

        return form;
    }

    private Department getDepartment() {
        Country country = new Country("Poland", "PL", "+48", new Currency("Polish Zloty", "PLN", 4.35));
        return new Department(country, "Katowice", "ul. Fajna", "1", "40-000", "car-rental-alpha@gmail.com", "500 500 501", false);
    }

    private CarBase getCarBase() {
        return new CarBase("https://cdn2.rcstatic.com/images/car_images/web/fiat/500_lrg.jpg", "Fiat", "Fiat 500", 2007, CarBase.CarType.TYPE_HATCHBACK, 2, 20.0, 190.0);
    }

    @BeforeEach
    void setup() {
        session = new MockHttpSession();
        time1 = LocalDateTime.now().minusMinutes(5);
        time2 = LocalDateTime.now().minusMinutes(1);

        session.setAttribute("process_indexForm", getValidIndexForm());
        session.setAttribute("process_carBaseId", 1L);
        session.setAttribute("process_step1_time", time1);
        session.setAttribute("process_step2_time", time2);

        when(reservationService.isValidRequest(any(), any(), any(), any())).thenReturn(true);
        when(carBaseService.findById(anyLong())).thenReturn(getCarBase());
        when(depService.findById(anyLong())).thenReturn(getDepartment());
        when(settingsService.getInstance()).thenReturn(new Settings());
    }

    // RequestMethod.GET - default

    @Test
    void shouldReturnValidHtmlDoc() throws Exception {
        mvc.perform(get("/reservation")
                        .session(session)
                        .flashAttr("s1_time", time1))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("<!DOCTYPE html>")))
                .andExpect(view().name("common/reservationRecap"));
    }

    @Test
    void shouldRedirectDueToExpiredSession() throws Exception {
        mvc.perform(get("/reservation")
                        .session(new MockHttpSession())
                        .flashAttr("s1_time", time1))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(view().name("redirect:/"));
    }


    @Test
    void shouldRedirectDueToInvalidRequest() throws Exception {
        // Invalid request
        when(reservationService.isValidRequest(any(), any(), any(), any())).thenReturn(false);

        mvc.perform(get("/reservation")
                        .session(session)
                        .flashAttr("s1_time", time1))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(view().name("redirect:/"));
    }


    @Test
    void shouldRedirectDueToNoCarBase() throws Exception {
        // CarBase ResourceNotFoundExc
        when(carBaseService.findById(anyLong())).thenThrow(new ResourceNotFoundException());

        mvc.perform(get("/reservation")
                        .session(session)
                        .flashAttr("s1_time", time1))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(view().name("redirect:/"));
    }

    @Test
    void shouldRedirectDueToNoDepartment() throws Exception {
        // Department ResourceNotFoundExc
        when(depService.findById(anyLong())).thenThrow(new ResourceNotFoundException());

        mvc.perform(get("/reservation")
                        .session(session)
                        .flashAttr("s1_time", time1))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(view().name("redirect:/"));
    }


    @Test
    void shouldRedirectDueToBrowserTabMismatch() throws Exception {
        mvc.perform(get("/reservation")
                        .session(session)
                        .flashAttr("s1_time", LocalDateTime.now()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(view().name("redirect:/"));
    }

    @Test
    void shouldReturnValidMap() throws Exception {
        mvc.perform(get("/reservation")
                        .session(session)
                        .flashAttr("s1_time", time1))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(
                        "s1_time",
                        "s2_time",
                        "diff_return_price",
                        "total_price",
                        "days",
                        "dptF",
                        "dptT",
                        "reservationData",
                        "carBase",
                        "currency",
                        "raw_price",
                        "deposit"))
                .andExpect(view().name("common/reservationRecap"));
    }

    @Test
    void shouldHaveDifferentParams() throws Exception {
        MvcResult result1 = mvc.perform(get("/reservation")
                        .session(session)
                        .flashAttr("s1_time", time1))
                .andExpect(status().isOk())
                .andExpect(view().name("common/reservationRecap"))
                .andReturn();

        // Modified index form
        IndexForm modifiedForm = getValidIndexForm();
        modifiedForm.setDifferentDepartment(true);
        modifiedForm.setDepartmentIdTo(2L);
        session.setAttribute("process_indexForm", modifiedForm);

        MvcResult result2 = mvc.perform(get("/reservation")
                        .session(session)
                        .flashAttr("s1_time", time1))
                .andExpect(status().isOk())
                .andExpect(view().name("common/reservationRecap"))
                .andReturn();

        Double value1 = Double.parseDouble(result1.getModelAndView().getModel().get("diff_return_price").toString());
        Double value2 = Double.parseDouble(result2.getModelAndView().getModel().get("diff_return_price").toString());

        assertNotEquals("diff_return_price", value1, value2);
    }

    @Test
    void shouldClearSessionIllArgExc() throws Exception {
        when(reservationService.isValidRequest(any(), any(), any(), any())).thenReturn(false);

        mvc.perform(get("/reservation")
                        .session(session)
                        .flashAttr("s1_time", time1))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));

        String[] keys = {"process_indexForm", "process_carBaseId", "process_step1_time", "process_step2_time"};
        boolean hasKeys = false;

        for (String key:keys) {
            if (Arrays.stream(session.getValueNames()).toList().contains(key)) hasKeys = true;
        }

        assertFalse("Incorrect session", hasKeys);
    }

    @Test
    void shouldClearSessionRunTimeExc() throws Exception {
        when(carBaseService.findById(anyLong())).thenThrow(new ResourceNotFoundException());

        mvc.perform(get("/reservation")
                        .session(session)
                        .flashAttr("s1_time", time1))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));

        String[] keys = {"process_indexForm", "process_carBaseId", "process_step1_time", "process_step2_time"};
        boolean hasKeys = false;

        for (String key:keys) {
            if (Arrays.stream(session.getValueNames()).toList().contains(key)) hasKeys = true;
        }

        assertFalse("Incorrect session", hasKeys);
    }

    // RequestMethod.POST - back

    @Test
    void shouldProperlyRedirectBackToCars() throws Exception {
        mvc.perform(post("/reservation/back")
                        .session(session)
                        .param("s1_time", time1.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("s1_time"))
                .andExpect(view().name("redirect:/cars"));

        assertNull("Session is unmodified.", session.getValue("process_step2_time"));
        assertNull("Session is unmodified.", session.getValue("process_carBaseId"));
    }

    // RequestMethod.POST - confirm

    @Test
    void shouldConfirmBeUnauthorized() throws Exception {
        mvc.perform(post("/reservation/confirm")
                        .session(session)
                        .param("s1_time", time1.toString())
                        .param("s2_time", time2.toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldEmployeeConfirmBeAuthorized() throws Exception {
        mvc.perform(post("/reservation/confirm")
                        .with(user(employee))
                        .session(session)
                        .param("carBaseId", "1")
                        .param("indexData.departmentIdFrom", String.valueOf(getValidIndexForm().getDepartmentIdFrom()))
                        .param("indexData.departmentIdTo", String.valueOf(getValidIndexForm().getDepartmentIdTo()))
                        .param("indexData.differentDepartment", String.valueOf(getValidIndexForm().isDifferentDepartment()))
                        .param("indexData.dateFrom", String.valueOf(getValidIndexForm().getDateFrom()))
                        .param("indexData.dateTo", String.valueOf(getValidIndexForm().getDateTo()))
                        .param("s1_time", time1.toString())
                        .param("s2_time", time2.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/loc-res"));
    }

    @Test
    void shouldCustomerConfirmBeAuthorized() throws Exception {
        when(customerService.appendReservationToCustomer(eq(customer.getId()), any(ReservationForm.class))).thenReturn(HttpStatus.CREATED);

        mvc.perform(post("/reservation/confirm")
                        .with(user(customer))
                        .session(session)
                        .param("carBaseId", "1")
                        .param("indexData.departmentIdFrom", String.valueOf(getValidIndexForm().getDepartmentIdFrom()))
                        .param("indexData.departmentIdTo", String.valueOf(getValidIndexForm().getDepartmentIdTo()))
                        .param("indexData.differentDepartment", String.valueOf(getValidIndexForm().isDifferentDepartment()))
                        .param("indexData.dateFrom", String.valueOf(getValidIndexForm().getDateFrom()))
                        .param("indexData.dateTo", String.valueOf(getValidIndexForm().getDateTo()))
                        .param("s1_time", time1.toString())
                        .param("s2_time", time2.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(view().name("redirect:/reservations"));
    }

    @Test
    void shouldEmployeeConfirmHaveValidResult() throws Exception {
        MvcResult result = mvc.perform(post("/reservation/confirm")
                        .with(user(employee))
                        .session(session)
                        .param("carBaseId", "1")
                        .param("indexData.departmentIdFrom", String.valueOf(getValidIndexForm().getDepartmentIdFrom()))
                        .param("indexData.departmentIdTo", String.valueOf(getValidIndexForm().getDepartmentIdTo()))
                        .param("indexData.differentDepartment", String.valueOf(getValidIndexForm().isDifferentDepartment()))
                        .param("indexData.dateFrom", String.valueOf(getValidIndexForm().getDateFrom()))
                        .param("indexData.dateTo", String.valueOf(getValidIndexForm().getDateTo()))
                        .param("s1_time", time1.toString())
                        .param("s2_time", time2.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/loc-res"))
                .andReturn();

        assertNotNull("'reservationDetails' aren't present.", result.getFlashMap().get("reservationDetails"));
        assertNotNull("'s1_time' is not present.", result.getFlashMap().get("s1_time"));
        assertNotNull("'s2_time' is not present.", result.getFlashMap().get("s2_time"));
    }

    @Test
    void shouldCustomerConfirmHaveValidResult() throws Exception {
        when(customerService.appendReservationToCustomer(eq(customer.getId()), any(ReservationForm.class))).thenReturn(HttpStatus.CREATED);

        MvcResult result = mvc.perform(post("/reservation/confirm")
                        .with(user(customer))
                        .session(session)
                        .param("carBaseId", "1")
                        .param("indexData.departmentIdFrom", String.valueOf(getValidIndexForm().getDepartmentIdFrom()))
                        .param("indexData.departmentIdTo", String.valueOf(getValidIndexForm().getDepartmentIdTo()))
                        .param("indexData.differentDepartment", String.valueOf(getValidIndexForm().isDifferentDepartment()))
                        .param("indexData.dateFrom", String.valueOf(getValidIndexForm().getDateFrom()))
                        .param("indexData.dateTo", String.valueOf(getValidIndexForm().getDateTo()))
                        .param("s1_time", time1.toString())
                        .param("s2_time", time2.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(view().name("redirect:/reservations"))
                .andReturn();

        assertNull("'reservationDetails' are present.", result.getFlashMap().get("reservationDetails"));
        assertNull("'s1_time' is present.", result.getFlashMap().get("s1_time"));
        assertNull("'s2_time' is present.", result.getFlashMap().get("s2_time"));
        assertNull("'process_indexForm' is present.", session.getAttribute("process_indexForm"));
        assertNull("'process_carBaseId' is present.", session.getAttribute("process_carBaseId"));
        assertNull("'process_step1_time' is present.", session.getAttribute("process_step1_time"));
        assertNull("'process_step2_time' is present.", session.getAttribute("process_step2_time"));
    }

    @Test
    void shouldCustomerAuthorizedConfirmFailDueToService() throws Exception {
        when(customerService.appendReservationToCustomer(eq(customer.getId()), any(ReservationForm.class))).thenReturn(HttpStatus.NOT_FOUND);

        mvc.perform(post("/reservation/confirm")
                        .with(user(customer))
                        .session(session)
                        .param("carBaseId", "1")
                        .param("indexData.departmentIdFrom", String.valueOf(getValidIndexForm().getDepartmentIdFrom()))
                        .param("indexData.departmentIdTo", String.valueOf(getValidIndexForm().getDepartmentIdTo()))
                        .param("indexData.differentDepartment", String.valueOf(getValidIndexForm().isDifferentDepartment()))
                        .param("indexData.dateFrom", String.valueOf(getValidIndexForm().getDateFrom()))
                        .param("indexData.dateTo", String.valueOf(getValidIndexForm().getDateTo()))
                        .param("s1_time", time1.toString())
                        .param("s2_time", time2.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(view().name("redirect:/"));
    }

    @Test
    void shouldCustomerAuthorizedConfirmFailDueToInvalidParam() throws Exception {
        when(customerService.appendReservationToCustomer(eq(customer.getId()), any(ReservationForm.class))).thenReturn(HttpStatus.CREATED);

        mvc.perform(post("/reservation/confirm")
                        .with(user(customer))
                        .session(session)
                        .param("carBaseId", "1")
                        .param("indexData.departmentIdFrom", String.valueOf(getValidIndexForm().getDepartmentIdFrom()))
                        .param("indexData.departmentIdTo", String.valueOf(getValidIndexForm().getDepartmentIdTo() + 1L))
                        .param("indexData.differentDepartment", String.valueOf(getValidIndexForm().isDifferentDepartment()))
                        .param("indexData.dateFrom", String.valueOf(getValidIndexForm().getDateFrom()))
                        .param("indexData.dateTo", String.valueOf(getValidIndexForm().getDateTo()))
                        .param("s1_time", time1.toString())
                        .param("s2_time", time2.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(view().name("redirect:/"));
    }

    @Test
    void shouldCustomerAuthorizedConfirmFailDueToNullParam() throws Exception {
        when(customerService.appendReservationToCustomer(eq(customer.getId()), any(ReservationForm.class))).thenReturn(HttpStatus.CREATED);

        mvc.perform(post("/reservation/confirm")
                        .with(user(customer))
                        .session(session)
                        .param("carBaseId", "1")
                        .param("s1_time", time1.toString())
                        .param("s2_time", time2.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(view().name("redirect:/"));
    }

    @Test
    void shouldCustomerAuthorizedConfirmFailDueToInvalidRequest() throws Exception {
        when(reservationService.isValidRequest(any(), any(), any(), any())).thenReturn(false);
        when(customerService.appendReservationToCustomer(eq(customer.getId()), any(ReservationForm.class))).thenReturn(HttpStatus.CREATED);

        mvc.perform(post("/reservation/confirm")
                        .with(user(customer))
                        .session(session)
                        .param("carBaseId", "1")
                        .param("indexData.departmentIdFrom", String.valueOf(getValidIndexForm().getDepartmentIdFrom()))
                        .param("indexData.departmentIdTo", String.valueOf(getValidIndexForm().getDepartmentIdTo() + 1L))
                        .param("indexData.differentDepartment", String.valueOf(getValidIndexForm().isDifferentDepartment()))
                        .param("indexData.dateFrom", String.valueOf(getValidIndexForm().getDateFrom()))
                        .param("indexData.dateTo", String.valueOf(getValidIndexForm().getDateTo()))
                        .param("s1_time", time1.toString())
                        .param("s2_time", time2.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(view().name("redirect:/"));
    }

    @Test
    void shouldCustomerAuthorizedConfirmFailDueToInvalidSession() throws Exception {
        when(customerService.appendReservationToCustomer(eq(customer.getId()), any(ReservationForm.class))).thenReturn(HttpStatus.CREATED);
        session.removeAttribute("process_indexForm");

        mvc.perform(post("/reservation/confirm")
                        .with(user(customer))
                        .session(session)
                        .param("carBaseId", "1")
                        .param("indexData.departmentIdFrom", String.valueOf(getValidIndexForm().getDepartmentIdFrom()))
                        .param("indexData.departmentIdTo", String.valueOf(getValidIndexForm().getDepartmentIdTo() + 1L))
                        .param("indexData.differentDepartment", String.valueOf(getValidIndexForm().isDifferentDepartment()))
                        .param("indexData.dateFrom", String.valueOf(getValidIndexForm().getDateFrom()))
                        .param("indexData.dateTo", String.valueOf(getValidIndexForm().getDateTo()))
                        .param("s1_time", time1.toString())
                        .param("s2_time", time2.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(view().name("redirect:/"));
    }

    @Test
    void shouldCustomerAuthorizedConfirmFailDueToInvalidBrowserTab() throws Exception {
        when(customerService.appendReservationToCustomer(eq(customer.getId()), any(ReservationForm.class))).thenReturn(HttpStatus.CREATED);
        session.setAttribute("process_step2_time", LocalDateTime.now());

        mvc.perform(post("/reservation/confirm")
                        .with(user(customer))
                        .session(session)
                        .param("carBaseId", "1")
                        .param("indexData.departmentIdFrom", String.valueOf(getValidIndexForm().getDepartmentIdFrom()))
                        .param("indexData.departmentIdTo", String.valueOf(getValidIndexForm().getDepartmentIdTo() + 1L))
                        .param("indexData.differentDepartment", String.valueOf(getValidIndexForm().isDifferentDepartment()))
                        .param("indexData.dateFrom", String.valueOf(getValidIndexForm().getDateFrom()))
                        .param("indexData.dateTo", String.valueOf(getValidIndexForm().getDateTo()))
                        .param("s1_time", time1.toString())
                        .param("s2_time", time2.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(view().name("redirect:/"));
    }
}
