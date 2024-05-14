package com.sda.rentacar.web.mvc.common;

import com.sda.rentacar.BaseControllerTest;
import com.sda.rentacar.exceptions.ResourceNotFoundException;
import com.sda.rentacar.model.property.car.CarBase;
import com.sda.rentacar.model.property.department.Country;
import com.sda.rentacar.model.property.department.Department;
import com.sda.rentacar.model.property.payments.Currency;
import com.sda.rentacar.service.CarBaseService;
import com.sda.rentacar.service.DepartmentService;
import com.sda.rentacar.web.mvc.form.operational.IndexForm;
import com.sda.rentacar.web.mvc.form.property.cars.GenericCarForm;
import com.sda.rentacar.web.mvc.form.property.cars.SelectCarBaseFilterForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertNotNull;
import static org.springframework.test.util.AssertionErrors.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SelectCarController.class)
public class SelectCarControllerTest extends BaseControllerTest {

    private static MockHttpSession session;

    @MockBean
    private DepartmentService departmentService;

    @MockBean
    private CarBaseService carBaseService;

    private IndexForm getValidIndexForm() {
        IndexForm form = new IndexForm();
        form.setDepartmentIdFrom(1L);
        form.setDepartmentIdTo(1L);
        form.setDifferentDepartment(true);
        form.setDateFrom(LocalDate.now().plusWeeks(1));
        form.setDateTo(LocalDate.now().plusWeeks(2));

        return form;
    }

    private Department getDepartment() {
        Country country = new Country("Poland", "PL", "+48", new Currency("Polish Zloty", "PLN", 4.35));
        return new Department(country, "Katowice", "ul. Fajna", "1", "40-000", "car-rental-alpha@gmail.com", "500 500 501", false);
    }

    private List<CarBase> getCarBases() {
        return List.of(new CarBase("https://cdn2.rcstatic.com/images/car_images/web/fiat/500_lrg.jpg", "Fiat", "Fiat 500", 2007, CarBase.CarType.TYPE_HATCHBACK, 2, 20.0, 190.0),
                new CarBase("/cars/bmw3.jpg", "BMW", "F34", 2013, CarBase.CarType.TYPE_HATCHBACK, 5, 25.0, 350.0),
                new CarBase("/cars/yaris.png", "Toyota", "Yaris", 1999, CarBase.CarType.TYPE_HATCHBACK, 4, 22.0, 250.0),
                new CarBase("/cars/hyundai-elantra.jpg", "Hyundai", "Lantra", 1991, CarBase.CarType.TYPE_COMPACT, 5, 23.0, 325.0),
                new CarBase("/cars/x5.jpg", "BMW", "X5 I", 1999, CarBase.CarType.TYPE_SUV, 5, 23.0, 300.0));
    }

    private Map<String, Object> getFilterMap() {
        Map<String, Object> map = new HashMap<>();
        CarBase cb = getCarBases().get(0);

        map.put("brands", Set.of(cb.getBrand()));
        map.put("types", Set.of(cb.getCarType()));
        map.put("seats", Set.of(cb.getSeats()));

        return map;
    }

    @BeforeEach
    void setup() {
        session = new MockHttpSession();
        session.setAttribute("process_indexForm", getValidIndexForm());
        session.setAttribute("process_step1_time", LocalDateTime.now().minusMinutes(5));

        when(departmentService.findById(anyLong())).thenReturn(getDepartment());
        when(carBaseService.findAvailableCarBasesInCountry(any(), any(), any())).thenReturn(getCarBases());
        when(carBaseService.getFilterProperties(anyList(), eq(false))).thenReturn(getFilterMap());
    }

    // RequestMethod.GET - default

    @Test
    void shouldReturnValidHtmlDoc() throws Exception {
        mvc.perform(get("/cars")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        containsString("<!DOCTYPE html>")))
                .andExpect(view().name("common/selectCar"));
    }

    @Test
    void shouldRedirectDueToNoCars() throws Exception {
        // Empty carBaseList
        when(carBaseService.findAvailableCarBasesInCountry(any(), any(), any())).thenReturn(Collections.emptyList());

        mvc.perform(get("/cars")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(view().name("redirect:/"));
    }

    @Test
    void shouldRedirectDueToExpiredSession() throws Exception {
        mvc.perform(get("/cars")
                        .session(new MockHttpSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(view().name("redirect:/"));
    }

    @Test
    void shouldRedirectDueToNoDepartment() throws Exception {
        // Invalid department
        when(departmentService.findById(anyLong())).thenThrow(new ResourceNotFoundException());

        mvc.perform(get("/cars")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(view().name("redirect:/"));
    }

    @Test
    void shouldRedirectDueToException() throws Exception {
        // DataAccess exceptions etc.
        when(departmentService.findById(anyLong())).thenThrow(new RuntimeException());

        mvc.perform(get("/cars")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(view().name("redirect:/"));
    }

    @Test
    void shouldRedirectDueToBrowserTabMismatch() throws Exception {
        mvc.perform(get("/cars")
                        .session(session)
                        .flashAttr("s1_time", LocalDateTime.now()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(view().name("redirect:/"));
    }

    @Test
    void shouldRedirectDueToNullPointer() throws Exception {
        Department department = getDepartment();
        department.getCountry().setCurrency(null);

        when(departmentService.findById(anyLong())).thenReturn(department);

        mvc.perform(get("/cars")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(view().name("redirect:/"));
    }

    @Test
    void shouldReturnValidMap() throws Exception {
        mvc.perform(get("/cars")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(
                        "currency",
                        "exchange",
                        "multiplier",
                        "carBases",
                        "s1_time",
                        "brands",
                        "types",
                        "seats",
                        "days",
                        "carFilterForm"))
                .andExpect(view().name("common/selectCar"));
    }

    // RequestMethod.POST - proceed

    @Test
    void shouldProceedSuccessfully() throws Exception {
        mvc.perform(post("/cars/proceed")
                        .param("select", "1")
                        .param("s1_time", session.getValue("process_step1_time").toString())
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/reservation"));
    }

    @Test
    void shouldProceedFailDueToNoIndexForm() throws Exception {
        session.removeAttribute("process_indexForm");

        mvc.perform(post("/cars/proceed")
                        .param("select", "1")
                        .param("s1_time", session.getValue("process_step1_time").toString())
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(view().name("redirect:/"));
    }

    @Test
    void shouldProceedFailDueToExpiredSession() throws Exception {
        mvc.perform(post("/cars/proceed")
                        .param("select", "1")
                        .param("s1_time", session.getValue("process_step1_time").toString())
                        .session(new MockHttpSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(view().name("redirect:/"));
    }

    @Test
    void shouldProceedFailDueToBrowserTabMismatch() throws Exception {
        mvc.perform(post("/cars/proceed")
                        .param("select", "1")
                        .param("s1_time", String.valueOf(LocalDateTime.now()))
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(view().name("redirect:/"));
    }

    @Test
    void shouldProceedSuccessfullyWithValidMapAndSession() throws Exception {
        mvc.perform(post("/cars/proceed")
                        .param("select", "1")
                        .param("s1_time", session.getValue("process_step1_time").toString())
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("s1_time"))
                .andExpect(view().name("redirect:/reservation"));

        // Session checks
        assertNotNull("process_carBaseId was not added to session", session.getAttribute("process_carBaseId"));
        assertTrue("process_carBaseId was incorrectly added (value: " + session.getAttribute("process_carBaseId") + ")", ((Long) session.getAttribute("process_carBaseId")).equals(1L));
        assertNotNull("process_step2_time was not added to session", session.getAttribute("process_step2_time"));
        assertTrue("process_step2_time was incorrectly added (value: " + session.getAttribute("process_step2_time") + ")", ((LocalDateTime) session.getAttribute("process_step2_time")).isAfter((LocalDateTime) session.getValue("process_step1_time")));
    }

    // RequestMethod.POST - filter

    @Test
    void shouldFilterSuccessfully() throws Exception {
        mvc.perform(post("/cars/filter")
                        .param("carFilterForm", String.valueOf(new SelectCarBaseFilterForm()))
                        .param("s1_time", session.getValue("process_step1_time").toString())
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/cars"));
    }

    @Test
    void shouldFilterFailDueToExpiredSession() throws Exception {
        mvc.perform(post("/cars/filter")
                        .param("carFilterForm", String.valueOf(new SelectCarBaseFilterForm()))
                        .param("s1_time", session.getValue("process_step1_time").toString())
                        .session(new MockHttpSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(view().name("redirect:/"));
    }

    @Test
    void shouldFilterFailDueToBrowserTabMismatch() throws Exception {
        mvc.perform(post("/cars/filter")
                        .param("carFilterForm", String.valueOf(new SelectCarBaseFilterForm()))
                        .param("s1_time", String.valueOf(LocalDateTime.now()))
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(view().name("redirect:/"));
    }

    @Test
    void shouldFilterFailDueToNoDepartment() throws Exception {
        when(carBaseService.findCarBasesByForm(any())).thenThrow(new ResourceNotFoundException());

        mvc.perform(post("/cars/filter")
                        .param("carFilterForm", String.valueOf(new SelectCarBaseFilterForm()))
                        .param("s1_time", session.getValue("process_step1_time").toString())
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(view().name("redirect:/"));
    }

    @Test
    void shouldFilterFailDueToNoIndexForm() throws Exception {
        session.removeAttribute("process_indexForm");

        mvc.perform(post("/cars/filter")
                        .param("carFilterForm", String.valueOf(new SelectCarBaseFilterForm()))
                        .param("s1_time", session.getValue("process_step1_time").toString())
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("message"))
                .andExpect(view().name("redirect:/"));
    }

    @Test
    void shouldFilterSuccessfullyWithValidMapAndSession() throws Exception {
        mvc.perform(post("/cars/filter")
                        .param("carFilterForm", String.valueOf(new SelectCarBaseFilterForm()))
                        .param("s1_time", session.getValue("process_step1_time").toString())
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("s1_time"))
                .andExpect(flash().attributeExists("filteredCarBases"))
                .andExpect(flash().attributeExists("carFilterForm"))
                .andExpect(view().name("redirect:/cars"));

        // Additional checks
        verify(carBaseService, times(1)).findCarBasesByForm(any());
    }
}
