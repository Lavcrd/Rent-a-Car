package com.sda.carrental.clr;

import com.sda.carrental.constants.GlobalValues;
import com.sda.carrental.constants.enums.Country;
import com.sda.carrental.model.Company;
import com.sda.carrental.model.operational.Renting;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.property.Car;
import com.sda.carrental.model.property.Department;
import com.sda.carrental.model.property.PaymentDetails;
import com.sda.carrental.model.users.Customer;
import com.sda.carrental.model.users.Employee;
import com.sda.carrental.model.users.Verification;
import com.sda.carrental.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@RequiredArgsConstructor
@Service
public class PredefiniedData implements CommandLineRunner {

    //The purpose of this class is to generate artificial data for visualizing the app's user interface.

    private final BCryptPasswordEncoder encoder;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final CarRepository carRepository;
    private final ReservationRepository reservationRepository;
    private final RentingRepository rentingRepository;
    private final PaymentDetailsRepository paymentDetailsRepository;
    private final VerificationRepository verificationRepository;
    private final GlobalValues gv;

    @Override
    public void run(String... args) {
        createDepartments();
        createCompany();

        createUsers();
        createCars();

        createReservation();
        createRent();
/*        createInvoice();*/ // currently creates duplicates due to not automated reservation statuses.

        createVerification();
    }

    private void createUsers() {
        userRepository.save(new Customer("user1@gmail.com", encoder.encode("password1"), "Anna", "Nazwiskowa", Country.COUNTRY_PL, "Kraków", "ul. Ulica 123", "123312891"));
        userRepository.save(new Customer("user2@gmail.com", encoder.encode("password1"), "Jakub", "Kowalski", Country.COUNTRY_PL, "Katowice", "ul. Ulica 12", "123312892"));
        userRepository.save(new Customer("user3@gmail.com", encoder.encode("password1"), "Maciek", "Masło", Country.COUNTRY_PL, "Gdynia", "ul. Ulica 1", "123312893"));
        userRepository.save(new Customer("user4@gmail.com", encoder.encode("password1"), "Jan", "Orzech", Country.COUNTRY_PL, "Gdańsk", "ul. Ulica 124", "123312894"));
        userRepository.save(new Customer("user5@gmail.com", encoder.encode("password1"), "Katarzyna", "Kasztan", Country.COUNTRY_PL, "Warszawa", "ul. Ulica 133", "123312895"));
        userRepository.save(new Customer("user6@gmail.com", encoder.encode("password1"), "Igor", "Kasztan", Country.COUNTRY_PL, "Białystok", "ul. Ulica 137", "123312896"));
        userRepository.save(new Customer("user7@gmail.com", encoder.encode("password1"), "Anna", "Kowalska", Country.COUNTRY_PL, "Opole", "ul. Ulica 138", "123312897"));
        userRepository.save(new Customer("a@a", encoder.encode("a"), "Andrzej", "Nowak", Country.COUNTRY_PL, "Poznań", "ul. Ulica 139", "123312898"));


        userRepository.save(new Employee("manager1@gmail.com", encoder.encode("manager1"), "Maria", "Fajna", departmentRepository.findById(1L).orElse(null), Employee.Titles.RANK_MANAGER));
        userRepository.save(new Employee("manager2@gmail.com", encoder.encode("manager2"), "Aleksandra", "Ładna", departmentRepository.findById(2L).orElse(null), Employee.Titles.RANK_MANAGER));
        userRepository.save(new Employee("manager3@gmail.com", encoder.encode("manager3"), "Katarzyna", "Nieładna", departmentRepository.findById(3L).orElse(null), Employee.Titles.RANK_MANAGER));
        userRepository.save(new Employee("pracownik@gmail.com", encoder.encode("pracownik"), "Anna", "Mniejfajna", departmentRepository.findById(1L).orElse(null), Employee.Titles.RANK_CLERK));
        userRepository.save(new Employee("pracownik2@gmail.com", encoder.encode("pracownik2"), "Magda", "Piąta", departmentRepository.findById(2L).orElse(null), Employee.Titles.RANK_CLERK));
        userRepository.save(new Employee("pracownik3@gmail.com", encoder.encode("pracownik3"), "Wioletta", "Fioletowa", departmentRepository.findById(3L).orElse(null), Employee.Titles.RANK_CLERK));
    }

    private void createCompany() {
        companyRepository.save(new Company("Rent-a-Car", "www.rentcar.com", "groupSIX", "Rent-a-Car"));
    }

    private void createDepartments() {
        departmentRepository.save(new Department(Country.COUNTRY_PL, "Katowice", "ul. Fajna 1", "40-000", "car-rental-alpha@gmail.com", "500 500 501", false));
        departmentRepository.save(new Department(Country.COUNTRY_PL, "Łódź", "ul. Niefajna 2", "90-000", "car-rental-beta@gmail.com", "500 500 502", false));
        departmentRepository.save(new Department(Country.COUNTRY_PL, "Gdańsk", "ul. Średnia 3", "80-000", "car-rental-gamma@gmail.com", "500 500 503", false));
        departmentRepository.save(new Department(Country.COUNTRY_PL, "Warszawa", "ul. Pusta 4", "00-001", "car-rental-delta@gmail.com", "500 500 504", true));
        departmentRepository.save(new Department(Country.COUNTRY_PL, "Białystok", "ul. Pełna 5", "15-000", "car-rental-epsilon@gmail.com", "500 500 505", false));
        departmentRepository.save(new Department(Country.COUNTRY_PL, "Poznań", "ul. Półpełna 6", "60-001", "car-rental-dzeta@gmail.com", "500 500 506", false));
        departmentRepository.save(new Department(Country.COUNTRY_PL, "Wrocław", "ul. Półpusta 7", "50-001", "car-rental-eta@gmail.com", "500 500 507", false));
    }

    private void createCars() {
        carRepository.save(new Car(departmentRepository.findById(1L).orElse(null), "https://cdn2.rcstatic.com/images/car_images/web/fiat/500_lrg.jpg", "Fiat", "Fiat 500", 2007, 150000L, 2, 85.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_OPEN, 750.0));
        carRepository.save(new Car(departmentRepository.findById(1L).orElse(null), "/cars/bmw3.jpg", "BMW", "F34", 2013, 140000L, 5, 95.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_OPEN, 1500.0));
        carRepository.save(new Car(departmentRepository.findById(1L).orElse(null), "/cars/yaris.png", "Toyota", "Yaris", 1999, 130000L, 4, 102.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_OPEN, 1500.0));
        carRepository.save(new Car(departmentRepository.findById(1L).orElse(null), "/cars/hyundai-elantra.jpg", "Hyundai", "Lantra", 1990, 120000L, 5, 81.0, Car.CarType.TYPE_COMPACT, Car.CarStatus.STATUS_UNAVAILABLE, 500.0));

        carRepository.save(new Car(departmentRepository.findById(2L).orElse(null), "https://cdn2.rcstatic.com/images/car_images/web/fiat/500_lrg.jpg", "Fiat", "Fiat 500", 2007, 150000L, 2, 86.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_UNAVAILABLE, 750.0));
        carRepository.save(new Car(departmentRepository.findById(2L).orElse(null), "/cars/bmw3.jpg", "BMW", "F34", 2013, 140000L, 5, 90.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_OPEN, 1500.0));
        carRepository.save(new Car(departmentRepository.findById(2L).orElse(null), "/cars/yaris.png", "Toyota", "Yaris", 1999, 130000L, 4, 90.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_RENTED, 1500.0));
        carRepository.save(new Car(departmentRepository.findById(2L).orElse(null), "/cars/hyundai-elantra.jpg", "Hyundai", "Lantra", 1990, 120000L, 5, 82.0, Car.CarType.TYPE_COMPACT, Car.CarStatus.STATUS_OPEN, 500.0));

        carRepository.save(new Car(departmentRepository.findById(3L).orElse(null), "https://cdn2.rcstatic.com/images/car_images/web/fiat/500_lrg.jpg", "Fiat", "Fiat 500", 2007, 150000L, 2, 88.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_OPEN, 750.0));
        carRepository.save(new Car(departmentRepository.findById(3L).orElse(null), "/cars/bmw3.jpg", "BMW", "F34", 2013, 140000L, 5, 100.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_RENTED, 1500.0));
        carRepository.save(new Car(departmentRepository.findById(3L).orElse(null), "/cars/yaris.png", "Toyota", "Yaris", 1999, 130000L, 4, 100.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_OPEN, 1500.0));
        carRepository.save(new Car(departmentRepository.findById(3L).orElse(null), "/cars/hyundai-elantra.jpg", "Hyundai", "Lantra", 1990, 120000L, 5, 84.0, Car.CarType.TYPE_COMPACT, Car.CarStatus.STATUS_UNAVAILABLE, 1000.0));

        carRepository.save(new Car(departmentRepository.findById(4L).orElse(null), "https://cdn2.rcstatic.com/images/car_images/web/fiat/500_lrg.jpg", "Fiat", "Fiat 500", 2007, 150000L, 2, 89.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_OPEN, 750.0));
        carRepository.save(new Car(departmentRepository.findById(4L).orElse(null), "/cars/bmw3.jpg", "BMW", "F34", 2013, 140000L, 5, 98.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_OPEN, 1500.0));
        carRepository.save(new Car(departmentRepository.findById(4L).orElse(null), "/cars/yaris.png", "Toyota", "Yaris", 1999, 130000L, 4, 99.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_OPEN, 1500.0));
        carRepository.save(new Car(departmentRepository.findById(4L).orElse(null), "/cars/hyundai-elantra.jpg", "Hyundai", "Lantra", 1990, 120000L, 5, 84.0, Car.CarType.TYPE_COMPACT, Car.CarStatus.STATUS_UNAVAILABLE, 500.0));

        carRepository.save(new Car(departmentRepository.findById(5L).orElse(null), "https://cdn2.rcstatic.com/images/car_images/web/fiat/500_lrg.jpg", "Fiat", "Fiat 500", 2007, 150000L, 2, 82.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_OPEN, 750.0));
        carRepository.save(new Car(departmentRepository.findById(5L).orElse(null), "/cars/bmw3.jpg", "BMW", "F34", 2013, 140000L, 5, 90.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_OPEN, 2000.0));
        carRepository.save(new Car(departmentRepository.findById(5L).orElse(null), "/cars/yaris.png", "Toyota", "Yaris", 1999, 130000L, 4, 90.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_RENTED, 2000.0));
        carRepository.save(new Car(departmentRepository.findById(5L).orElse(null), "/cars/hyundai-elantra.jpg", "Hyundai", "Lantra", 1990, 120000L, 5, 82.0, Car.CarType.TYPE_COMPACT, Car.CarStatus.STATUS_UNAVAILABLE, 1000.0));

        carRepository.save(new Car(departmentRepository.findById(6L).orElse(null), "https://cdn2.rcstatic.com/images/car_images/web/fiat/500_lrg.jpg", "Fiat", "Fiat 500", 2007, 150000L, 2, 81.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_OPEN, 750.0));
        carRepository.save(new Car(departmentRepository.findById(6L).orElse(null), "/cars/hyundai-elantra.jpg", "Hyundai", "Lantra", 1990, 120000L, 5, 95.0, Car.CarType.TYPE_COMPACT, Car.CarStatus.STATUS_UNAVAILABLE, 2000.0));
        carRepository.save(new Car(departmentRepository.findById(6L).orElse(null), "/cars/bmw3.jpg", "BMW", "F34", 2013, 140000L, 5, 100.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_RENTED, 2000.0));
        carRepository.save(new Car(departmentRepository.findById(6L).orElse(null), "/cars/yaris.png", "Toyota", "Yaris", 1999, 130000L, 4, 81.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_OPEN, 500.0));

        carRepository.save(new Car(departmentRepository.findById(7L).orElse(null), "https://cdn2.rcstatic.com/images/car_images/web/fiat/500_lrg.jpg", "Fiat", "Fiat 500", 2007, 150000L, 2, 88.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_OPEN, 750.0));
        carRepository.save(new Car(departmentRepository.findById(7L).orElse(null), "/cars/hyundai-elantra.jpg", "Hyundai", "Lantra", 1990, 120000L, 5, 100.0, Car.CarType.TYPE_COMPACT, Car.CarStatus.STATUS_UNAVAILABLE, 2000.0));
        carRepository.save(new Car(departmentRepository.findById(7L).orElse(null), "/cars/bmw3.jpg", "BMW", "F34", 2013, 140000L, 5, 95.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_OPEN, 2000.0));
        carRepository.save(new Car(departmentRepository.findById(7L).orElse(null), "/cars/yaris.png", "Toyota", "Yaris", 1999, 130000L, 4, 82.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_RENTED, 500.0));
    }

    private void createReservation() {
        reservationRepository.save(new Reservation((Customer) userRepository.findById(1L).orElse(null), carRepository.findById(7L).orElse(null), departmentRepository.findById(2L).orElse(null), departmentRepository.findById(3L).orElse(null), LocalDate.now().minusDays(1), LocalDate.now().plusDays(3), LocalDate.of(2022, 10, 7)));
        reservationRepository.save(new Reservation((Customer) userRepository.findById(1L).orElse(null), carRepository.findById(10L).orElse(null), departmentRepository.findById(3L).orElse(null), departmentRepository.findById(2L).orElse(null), LocalDate.now().minusDays(3), LocalDate.now().plusDays(1), LocalDate.of(2022, 11, 4)));
        reservationRepository.save(new Reservation((Customer) userRepository.findById(1L).orElse(null), carRepository.findById(19L).orElse(null), departmentRepository.findById(5L).orElse(null), departmentRepository.findById(7L).orElse(null), LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), LocalDate.of(2022, 11, 8)));
        reservationRepository.save(new Reservation((Customer) userRepository.findById(1L).orElse(null), carRepository.findById(23L).orElse(null), departmentRepository.findById(6L).orElse(null), departmentRepository.findById(5L).orElse(null), LocalDate.now().plusDays(0), LocalDate.now().plusDays(4), LocalDate.of(2022, 12, 5)));
        reservationRepository.save(new Reservation((Customer) userRepository.findById(1L).orElse(null), carRepository.findById(28L).orElse(null), departmentRepository.findById(7L).orElse(null), departmentRepository.findById(6L).orElse(null), LocalDate.now().minusDays(2), LocalDate.now().plusDays(8), LocalDate.of(2022, 12, 9)));
    }

    private void createRent() {
        rentingRepository.save(new Renting(userRepository.findById(12L).orElse(null), reservationRepository.findById(1L).orElse(null), reservationRepository.findById(1L).get().getDateFrom(), ""));
        rentingRepository.save(new Renting(userRepository.findById(13L).orElse(null), reservationRepository.findById(2L).orElse(null), reservationRepository.findById(2L).get().getDateFrom(), ""));
        rentingRepository.save(new Renting(userRepository.findById(14L).orElse(null), reservationRepository.findById(3L).orElse(null), reservationRepository.findById(3L).get().getDateFrom(), ""));
        rentingRepository.save(new Renting(userRepository.findById(14L).orElse(null), reservationRepository.findById(4L).orElse(null), reservationRepository.findById(4L).get().getDateFrom(), ""));
        rentingRepository.save(new Renting(userRepository.findById(13L).orElse(null), reservationRepository.findById(5L).orElse(null), reservationRepository.findById(5L).get().getDateFrom(), ""));
    }

    private void createPayments() {
        paymentDetailsRepository.save(new PaymentDetails( carRepository.findById(7L).get().getPrice_day() * (reservationRepository.findById(1L).get().getDateFrom().until(reservationRepository.findById(1L).get().getDateTo(), ChronoUnit.DAYS)), carRepository.findById(7L).get().getDepositValue(), reservationRepository.findById(1L).orElse(null)));
        paymentDetailsRepository.save(new PaymentDetails(carRepository.findById(10L).get().getPrice_day() * (reservationRepository.findById(2L).get().getDateFrom().until(reservationRepository.findById(2L).get().getDateTo(), ChronoUnit.DAYS)), carRepository.findById(10L).get().getDepositValue(), reservationRepository.findById(2L).orElse(null)));
        paymentDetailsRepository.save(new PaymentDetails(carRepository.findById(19L).get().getPrice_day() * (reservationRepository.findById(3L).get().getDateFrom().until(reservationRepository.findById(3L).get().getDateTo(), ChronoUnit.DAYS)), carRepository.findById(19L).get().getDepositValue(), reservationRepository.findById(3L).orElse(null)));
        paymentDetailsRepository.save(new PaymentDetails(carRepository.findById(23L).get().getPrice_day() * (reservationRepository.findById(4L).get().getDateFrom().until(reservationRepository.findById(4L).get().getDateTo(), ChronoUnit.DAYS)), carRepository.findById(23L).get().getDepositValue(), reservationRepository.findById(4L).orElse(null)));
        paymentDetailsRepository.save(new PaymentDetails(carRepository.findById(28L).get().getPrice_day() * (reservationRepository.findById(5L).get().getDateFrom().until(reservationRepository.findById(5L).get().getDateTo(), ChronoUnit.DAYS)), carRepository.findById(28L).get().getDepositValue(), reservationRepository.findById(5L).orElse(null)));
    }

    private void createVerification() {
        verificationRepository.save(new Verification((Customer) userRepository.findById(1L).orElse(null), "123123", "678678"));
    }
}
