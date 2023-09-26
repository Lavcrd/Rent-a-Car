package com.sda.carrental.clr;

import com.sda.carrental.global.ConstantValues;
import com.sda.carrental.global.enums.Country;
import com.sda.carrental.model.Company;
import com.sda.carrental.model.operational.Rent;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.operational.Retrieve;
import com.sda.carrental.model.property.Car;
import com.sda.carrental.model.property.Department;
import com.sda.carrental.model.property.PaymentDetails;
import com.sda.carrental.model.users.*;
import com.sda.carrental.model.users.auth.Credentials;
import com.sda.carrental.model.users.auth.Verification;
import com.sda.carrental.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

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
    private final RentRepository rentRepository;
    private final PaymentDetailsRepository paymentDetailsRepository;
    private final VerificationRepository verificationRepository;
    private final CredentialsRepository credentialsRepository;
    private final RetrieveRepository retrieveRepository;
    private final ConstantValues cv;

    @Override
    public void run(String... args) {
        createDepartments();
        createCompany();

        createUsers();
        createCredentials();
        createCars();

        createReservation();
        createRent();
        createRetrieve();
        createPayments();

        createVerification();
    }

    private void createUsers() {
        userRepository.save(new Customer("Anna", "Nazwiskowa", Customer.CustomerStatus.STATUS_REGISTERED, "123312891"));
        userRepository.save(new Customer("Jakub", "Kowalski", Customer.CustomerStatus.STATUS_REGISTERED, "123312892"));
        userRepository.save(new Customer("Maciek", "Masło", Customer.CustomerStatus.STATUS_REGISTERED, "123312893"));
        userRepository.save(new Customer("Jan", "Orzech", Customer.CustomerStatus.STATUS_REGISTERED, "123312894"));
        userRepository.save(new Customer("Katarzyna", "Kasztan", Customer.CustomerStatus.STATUS_REGISTERED, "123312895"));
        userRepository.save(new Customer("Igor", "Kasztan", Customer.CustomerStatus.STATUS_REGISTERED, "123312896"));
        userRepository.save(new Customer("Anna", "Kowalska", Customer.CustomerStatus.STATUS_UNREGISTERED, "123312897"));


        userRepository.save(new Manager("Maria", "Fajna", departmentRepository.findById(1L).orElse(null), LocalDate.ofYearDay(9999, 1)));
        userRepository.save(new Manager("Aleksandra", "Ładna", departmentRepository.findById(2L).orElse(null), LocalDate.ofYearDay(2023, 1)));
        userRepository.save(new Manager("Katarzyna", "Nieładna", departmentRepository.findById(3L).orElse(null), LocalDate.ofYearDay(9999, 1)));

        userRepository.save(new Employee("Anna", "Mniejfajna", departmentRepository.findById(1L).orElse(null), LocalDate.ofYearDay(9999, 1)));
        userRepository.save(new Employee("Magda", "Piąta", departmentRepository.findById(2L).orElse(null), LocalDate.ofYearDay(9999, 1)));
        userRepository.save(new Employee("Wioletta", "Fioletowa", departmentRepository.findById(3L).orElse(null), LocalDate.ofYearDay(9999, 1)));

        userRepository.save(new Coordinator("Jacek", "Gruby", departmentRepository.findDepartmentsByCountry(Country.COUNTRY_PL), LocalDate.ofYearDay(9999, 1)));

        userRepository.save(new Admin("admin", "admin"));
    }

    private void createCredentials() {
        credentialsRepository.save(new Credentials(1L, "user1@gmail.com", encoder.encode("password1")));
        credentialsRepository.save(new Credentials(2L, "user2@gmail.com", encoder.encode("password1")));
        credentialsRepository.save(new Credentials(3L, "user3@gmail.com", encoder.encode("password1")));
        credentialsRepository.save(new Credentials(4L, "user4@gmail.com", encoder.encode("password1")));
        credentialsRepository.save(new Credentials(5L, "user5@gmail.com", encoder.encode("password1")));
        credentialsRepository.save(new Credentials(6L, "user6@gmail.com", encoder.encode("password1")));

        credentialsRepository.save(new Credentials(8L, "manager1@gmail.com", encoder.encode("manager1")));
        credentialsRepository.save(new Credentials(9L, "manager2@gmail.com", encoder.encode("manager1")));
        credentialsRepository.save(new Credentials(10L, "manager3@gmail.com", encoder.encode("manager1")));

        credentialsRepository.save(new Credentials(11L, "employee1@gmail.com", encoder.encode("employee1")));
        credentialsRepository.save(new Credentials(12L, "employee2@gmail.com", encoder.encode("employee1")));
        credentialsRepository.save(new Credentials(13L, "employee3@gmail.com", encoder.encode("employee1")));

        credentialsRepository.save(new Credentials(14L, "coordinator1@gmail.com", encoder.encode("coordinator1")));

        credentialsRepository.save(new Credentials(15L, "admin@gmail.com", encoder.encode("admin")));
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
        carRepository.save(new Car(1L, "https://cdn2.rcstatic.com/images/car_images/web/fiat/500_lrg.jpg", "Fiat", "Fiat 500", 2007, Country.COUNTRY_NL.getCode() + "-ABC1234", 150000L, 2, 85.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_OPEN, 750.0));
        carRepository.save(new Car(1L, "/cars/bmw3.jpg", "BMW", "F34", 2013, Country.COUNTRY_PL.getCode() + "-ABC4321", 140000L, 5, 95.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_OPEN, 1500.0));
        carRepository.save(new Car(1L, "/cars/yaris.png", "Toyota", "Yaris", 1999, Country.COUNTRY_PL.getCode() + "-ABC2314", 130000L, 4, 102.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_OPEN, 1500.0));
        carRepository.save(new Car(1L, "/cars/hyundai-elantra.jpg", "Hyundai", "Lantra", 1990, Country.COUNTRY_PL.getCode() + "-ABC3214", 120000L, 5, 81.0, Car.CarType.TYPE_COMPACT, Car.CarStatus.STATUS_UNAVAILABLE, 500.0));

        carRepository.save(new Car(2L, "https://cdn2.rcstatic.com/images/car_images/web/fiat/500_lrg.jpg", "Fiat", "Fiat 500", 2007, Country.COUNTRY_GB.getCode() + "-XBC1234", 150000L, 2, 86.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_UNAVAILABLE, 750.0));
        carRepository.save(new Car(2L, "/cars/bmw3.jpg", "BMW", "F34", 2013, Country.COUNTRY_GB.getCode() + "-AXX1234", 140000L, 5, 90.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_OPEN, 1500.0));
        carRepository.save(new Car(2L, "/cars/yaris.png", "Toyota", "Yaris", 1999, Country.COUNTRY_GB.getCode() + "-ABX1234", 130000L, 4, 90.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_RENTED, 1500.0));
        carRepository.save(new Car(2L, "/cars/hyundai-elantra.jpg", "Hyundai", "Lantra", 1990, Country.COUNTRY_GB.getCode() + "-AXC1234", 120000L, 5, 82.0, Car.CarType.TYPE_COMPACT, Car.CarStatus.STATUS_OPEN, 500.0));

        carRepository.save(new Car(3L, "https://cdn2.rcstatic.com/images/car_images/web/fiat/500_lrg.jpg", "Fiat", "Fiat 500", 2007, Country.COUNTRY_PL.getCode() + "-AAC1234", 150000L, 2, 88.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_OPEN, 750.0));
        carRepository.save(new Car(3L, "/cars/bmw3.jpg", "BMW", "F34", 2013, Country.COUNTRY_PL.getCode() + "-AVC1234", 140000L, 5, 100.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_RENTED, 1500.0));
        carRepository.save(new Car(3L, "/cars/yaris.png", "Toyota", "Yaris", 1999, Country.COUNTRY_PL.getCode() + "-AVV1234", 130000L, 4, 100.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_OPEN, 1500.0));
        carRepository.save(new Car(3L, "/cars/hyundai-elantra.jpg", "Hyundai", "Lantra", 1990, Country.COUNTRY_PL.getCode() + "-ACC1234", 120000L, 5, 84.0, Car.CarType.TYPE_COMPACT, Car.CarStatus.STATUS_UNAVAILABLE, 1000.0));

        carRepository.save(new Car(4L, "https://cdn2.rcstatic.com/images/car_images/web/fiat/500_lrg.jpg", "Fiat", "Fiat 500", 2007, Country.COUNTRY_PL.getCode() + "-BBC1234", 150000L, 2, 89.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_OPEN, 750.0));
        carRepository.save(new Car(4L, "/cars/bmw3.jpg", "BMW", "F34", 2013, Country.COUNTRY_PL.getCode() + "-CCC1234", 140000L, 5, 98.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_OPEN, 1500.0));
        carRepository.save(new Car(4L, "/cars/yaris.png", "Toyota", "Yaris", 1999, Country.COUNTRY_PL.getCode() + "-XXX1234", 130000L, 4, 99.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_OPEN, 1500.0));
        carRepository.save(new Car(4L, "/cars/hyundai-elantra.jpg", "Hyundai", "Lantra", 1990, Country.COUNTRY_PL.getCode() + "-ZZZ1234", 120000L, 5, 84.0, Car.CarType.TYPE_COMPACT, Car.CarStatus.STATUS_UNAVAILABLE, 500.0));

        carRepository.save(new Car(5L, "https://cdn2.rcstatic.com/images/car_images/web/fiat/500_lrg.jpg", "Fiat", "Fiat 500", 2007, Country.COUNTRY_PL.getCode() + "-BBB1234", 150000L, 2, 82.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_OPEN, 750.0));
        carRepository.save(new Car(5L, "/cars/bmw3.jpg", "BMW", "F34", 2013, Country.COUNTRY_PL.getCode() + "-MMM1234", 140000L, 5, 90.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_OPEN, 2000.0));
        carRepository.save(new Car(5L, "/cars/yaris.png", "Toyota", "Yaris", 1999, Country.COUNTRY_PL.getCode() + "-NNN1234", 130000L, 4, 90.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_RENTED, 2000.0));
        carRepository.save(new Car(5L, "/cars/hyundai-elantra.jpg", "Hyundai", "Lantra", 1990, Country.COUNTRY_PL.getCode() + "-VVV1234", 120000L, 5, 82.0, Car.CarType.TYPE_COMPACT, Car.CarStatus.STATUS_UNAVAILABLE, 1000.0));

        carRepository.save(new Car(6L, "https://cdn2.rcstatic.com/images/car_images/web/fiat/500_lrg.jpg", "Fiat", "Fiat 500", 2007, Country.COUNTRY_PL.getCode() + "-ABC123X", 150000L, 2, 81.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_OPEN, 750.0));
        carRepository.save(new Car(6L, "/cars/hyundai-elantra.jpg", "Hyundai", "Lantra", 1990, Country.COUNTRY_PL.getCode() + "-ABC12X4", 120000L, 5, 95.0, Car.CarType.TYPE_COMPACT, Car.CarStatus.STATUS_UNAVAILABLE, 2000.0));
        carRepository.save(new Car(6L, "/cars/bmw3.jpg", "BMW", "F34", 2013, Country.COUNTRY_PL.getCode() + "-ABC1Z34", 140000L, 5, 100.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_RENTED, 2000.0));
        carRepository.save(new Car(6L, "/cars/yaris.png", "Toyota", "Yaris", 1999, Country.COUNTRY_PL.getCode() + "-ABC1X34", 130000L, 4, 81.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_OPEN, 500.0));

        carRepository.save(new Car(7L, "https://cdn2.rcstatic.com/images/car_images/web/fiat/500_lrg.jpg", "Fiat", "Fiat 500", 2007, Country.COUNTRY_PL.getCode() + "-ABC1A34", 150000L, 2, 88.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_OPEN, 750.0));
        carRepository.save(new Car(7L, "/cars/hyundai-elantra.jpg", "Hyundai", "Lantra", 1990, Country.COUNTRY_PL.getCode() + "-ABC1D34", 120000L, 5, 100.0, Car.CarType.TYPE_COMPACT, Car.CarStatus.STATUS_UNAVAILABLE, 2000.0));
        carRepository.save(new Car(7L, "/cars/bmw3.jpg", "BMW", "F34", 2013, Country.COUNTRY_PL.getCode() + "-ABC1DD4", 140000L, 5, 95.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_OPEN, 2000.0));
        carRepository.save(new Car(7L, "/cars/yaris.png", "Toyota", "Yaris", 1999, Country.COUNTRY_PL.getCode() + "-ABC12S4", 130000L, 4, 82.0, Car.CarType.TYPE_HATCHBACK, Car.CarStatus.STATUS_RENTED, 500.0));
    }

    private void createReservation() {
        List<Reservation> reservationList = new LinkedList<>();
        reservationList.add(new Reservation((Customer) userRepository.findById(2L).orElse(null), carRepository.findById(1L).orElse(null), departmentRepository.findById(1L).orElse(null), departmentRepository.findById(2L).orElse(null), LocalDate.now().minusDays(1), LocalDate.now().plusDays(3), LocalDate.of(2022, 10, 7)));
        reservationList.add(new Reservation((Customer) userRepository.findById(2L).orElse(null), carRepository.findById(2L).orElse(null), departmentRepository.findById(1L).orElse(null), departmentRepository.findById(3L).orElse(null), LocalDate.now().minusDays(0), LocalDate.now().plusDays(2), LocalDate.of(2022, 10, 7)));
        reservationList.add(new Reservation((Customer) userRepository.findById(2L).orElse(null), carRepository.findById(3L).orElse(null), departmentRepository.findById(1L).orElse(null), departmentRepository.findById(4L).orElse(null), LocalDate.now().minusDays(3), LocalDate.now().plusDays(4), LocalDate.of(2022, 10, 7)));
        reservationList.add(new Reservation((Customer) userRepository.findById(1L).orElse(null), carRepository.findById(7L).orElse(null), departmentRepository.findById(2L).orElse(null), departmentRepository.findById(3L).orElse(null), LocalDate.now().minusDays(1), LocalDate.now().plusDays(3), LocalDate.of(2022, 10, 7)));
        reservationList.add(new Reservation((Customer) userRepository.findById(1L).orElse(null), carRepository.findById(10L).orElse(null), departmentRepository.findById(3L).orElse(null), departmentRepository.findById(2L).orElse(null), LocalDate.now().minusDays(3), LocalDate.now().plusDays(1), LocalDate.of(2022, 11, 4)));
        reservationList.add(new Reservation((Customer) userRepository.findById(1L).orElse(null), carRepository.findById(19L).orElse(null), departmentRepository.findById(5L).orElse(null), departmentRepository.findById(7L).orElse(null), LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), LocalDate.of(2022, 11, 8)));
        reservationList.add(new Reservation((Customer) userRepository.findById(1L).orElse(null), carRepository.findById(23L).orElse(null), departmentRepository.findById(6L).orElse(null), departmentRepository.findById(5L).orElse(null), LocalDate.now().plusDays(0), LocalDate.now().plusDays(4), LocalDate.of(2022, 12, 5)));
        reservationList.add(new Reservation((Customer) userRepository.findById(1L).orElse(null), carRepository.findById(28L).orElse(null), departmentRepository.findById(7L).orElse(null), departmentRepository.findById(6L).orElse(null), LocalDate.now().minusDays(2), LocalDate.now().plusDays(8), LocalDate.of(2022, 12, 9)));

        for (Reservation r : reservationList) {
            r.setStatus(Reservation.ReservationStatus.STATUS_RESERVED);
            reservationRepository.save(r);
        }
    }

    private void createRent() {
        rentRepository.save(new Rent(1L, 12L, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent imperdiet euismod ex, eget luctus lorem cursus vitae. Morbi mattis vel diam vitae hendrerit. Vivamus efficitur, elit vel pulvinar bibendum, enim. ", LocalDate.now(), 100000L));
        rentRepository.save(new Rent(2L, 13L, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent imperdiet euismod ex, eget luctus lorem cursus vitae. Morbi mattis vel diam vitae hendrerit. Vivamus efficitur, elit vel pulvinar bibendum, enim. ", LocalDate.now(), 100000L));
        rentRepository.save(new Rent(3L, 14L, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent imperdiet euismod ex, eget luctus lorem cursus vitae. Morbi mattis vel diam vitae hendrerit. Vivamus efficitur, elit vel pulvinar bibendum, enim. ", LocalDate.now(), 100000L));
        rentRepository.save(new Rent(4L, 14L, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent imperdiet euismod ex, eget luctus lorem cursus vitae. Morbi mattis vel diam vitae hendrerit. Vivamus efficitur, elit vel pulvinar bibendum, enim. ", LocalDate.now(), 100000L));
        rentRepository.save(new Rent(5L, 13L, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent imperdiet euismod ex, eget luctus lorem cursus vitae. Morbi mattis vel diam vitae hendrerit. Vivamus efficitur, elit vel pulvinar bibendum, enim. ", LocalDate.now(), 100000L));
    }

    private void createRetrieve() {
        retrieveRepository.save(new Retrieve(1L, reservationRepository.findById(1L).get(), rentRepository.findById(1L).get(), 13L, LocalDate.now().plusDays(7), "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent imperdiet euismod ex, eget luctus lorem cursus vitae. Morbi mattis vel diam vitae hendrerit. Vivamus efficitur, elit vel pulvinar bibendum, enim. ", departmentRepository.findById(1L).get(), 120000L));
        retrieveRepository.save(new Retrieve(2L, reservationRepository.findById(2L).get(), rentRepository.findById(2L).get(), 14L, LocalDate.now().plusDays(8), "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent imperdiet euismod ex, eget luctus lorem cursus vitae. Morbi mattis vel diam vitae hendrerit. Vivamus efficitur, elit vel pulvinar bibendum, enim. ", departmentRepository.findById(2L).get(), 120000L));
        retrieveRepository.save(new Retrieve(3L, reservationRepository.findById(3L).get(), rentRepository.findById(3L).get(), 15L, LocalDate.now().plusDays(9), "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent imperdiet euismod ex, eget luctus lorem cursus vitae. Morbi mattis vel diam vitae hendrerit. Vivamus efficitur, elit vel pulvinar bibendum, enim. ", departmentRepository.findById(3L).get(), 120000L));
        retrieveRepository.save(new Retrieve(4L, reservationRepository.findById(4L).get(), rentRepository.findById(4L).get(), 11L, LocalDate.now().plusDays(10), "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent imperdiet euismod ex, eget luctus lorem cursus vitae. Morbi mattis vel diam vitae hendrerit. Vivamus efficitur, elit vel pulvinar bibendum, enim. ", departmentRepository.findById(4L).get(), 120000L));
    }

    private void createPayments() {
        PaymentDetails pd1 = new PaymentDetails(600D, 120D, 1500D, 0D, 750D, reservationRepository.findById(1L).get());
        PaymentDetails pd2 = new PaymentDetails(500D, 0D, 1500D, 0D, 800D, reservationRepository.findById(2L).get());
        PaymentDetails pd3 = new PaymentDetails(700D, 120D, 1500D, 0D, 800D, reservationRepository.findById(3L).get());
        PaymentDetails pd4 = new PaymentDetails(800D, 0D, 1500D, 0D, 750D, reservationRepository.findById(4L).get());

        pd1.setSecured(600D);
        pd2.setSecured(700D);
        pd3.setSecured(920D);
        pd4.setSecured(900D);

        pd1.setReleasedDeposit(0);
        pd2.setReleasedDeposit(100);
        pd3.setReleasedDeposit(700);
        pd4.setReleasedDeposit(100);

        paymentDetailsRepository.save(pd1);
        paymentDetailsRepository.save(pd2);
        paymentDetailsRepository.save(pd3);
        paymentDetailsRepository.save(pd4);
    }

    private void createVerification() {
        verificationRepository.save(new Verification(1L, Country.COUNTRY_PL, "123123123", "678678"));
    }
}
