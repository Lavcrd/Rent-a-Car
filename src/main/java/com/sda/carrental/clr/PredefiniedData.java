package com.sda.carrental.clr;

import com.sda.carrental.global.Encryption;
import com.sda.carrental.global.enums.Role;
import com.sda.carrental.model.property.company.Company;
import com.sda.carrental.model.property.department.Country;
import com.sda.carrental.model.operational.Rent;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.operational.Retrieve;
import com.sda.carrental.model.property.car.Car;
import com.sda.carrental.model.property.department.Department;
import com.sda.carrental.model.property.payments.Currency;
import com.sda.carrental.model.property.payments.PaymentDetails;
import com.sda.carrental.model.property.car.CarBase;
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
@SuppressWarnings(value = "all")
public class PredefiniedData implements CommandLineRunner {

    //The purpose of this class is to generate artificial data for visualizing the app's user interface.

    static List<Currency> currencies;
    static List<Country> countries;
    private final BCryptPasswordEncoder encoder;
    private final UserRepository userRepository;
    private final CurrencyRepository currencyRepository;
    private final CountryRepository countryRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final CarBaseRepository carBaseRepository;
    private final CarRepository carRepository;
    private final ReservationRepository reservationRepository;
    private final RentRepository rentRepository;
    private final PaymentDetailsRepository paymentDetailsRepository;
    private final VerificationRepository verificationRepository;
    private final CredentialsRepository credentialsRepository;
    private final RetrieveRepository retrieveRepository;
    private final Encryption e;

    static {
        currencies = List.of(
                new Currency("Polish Zloty", "PLN", 4.35),
                new Currency("British Pound Sterling", "GBP", 0.86),
                new Currency("Euro", "EUR", 1.0)
        );

        countries = List.of(
                new Country("Poland", "PL", "+48", currencies.get(0)),
                new Country("Great Britain", "GB", "+44", currencies.get(1)),
                new Country("Netherlands", "NL", "+31", currencies.get(2)),
                new Country("Germany", "DE", "+49", currencies.get(2))
        );
    }


    @Override
    public void run(String... args) throws RuntimeException {
        createCurrencies();
        createCountries();

        createDepartments();
        createCompany();

        createUsers();
        createCredentials();

        createCarBases();
        createCars();

        createReservation();
        createRent();
        createRetrieve();
        createPayments();

        createVerification();
    }

    private void createCurrencies() {
        currencyRepository.saveAll(currencies);
    }

    private void createCountries() {
        for (Country country : countries) {
            country.setActive(true);
            countryRepository.save(country);
        }
    };

    private void createUsers() throws RuntimeException {
        List<Customer> customers = List.of(
                new Customer("Anna", "Nazwiskowa", Customer.Status.STATUS_REGISTERED, "123312891"),
                new Customer("Jakub", "Kowalski", Customer.Status.STATUS_REGISTERED, "123312892"),
                new Customer("Maciek", "Masło", Customer.Status.STATUS_REGISTERED, "123312893"),
                new Customer("Jan", "Orzech", Customer.Status.STATUS_REGISTERED, "123312894"),
                new Customer("Katarzyna", "Kasztan", Customer.Status.STATUS_REGISTERED, "123312895"),
                new Customer("Igor", "Kasztan", Customer.Status.STATUS_REGISTERED, "123312896"),
                new Customer("Anna", "Kowalska", Customer.Status.STATUS_UNREGISTERED, "123312897")
        );

        for (Customer customer:customers) {
            customer.setContactNumber(e.encrypt(customer.getContactNumber()));
            userRepository.save(customer);
        }

        List<Employee> employees = List.of(
                new Employee("Maria", "Fajna", "1231231111", List.of(departmentRepository.findById(1L).orElse(null)), LocalDate.ofYearDay(9999, 1), "111222333"),
                new Employee("Aleksandra", "Ładna", "1231232222", List.of(departmentRepository.findById(2L).orElse(null)), LocalDate.ofYearDay(9999, 1), "111222444"),
                new Employee("Katarzyna", "Nieładna", "1231233333", List.of(departmentRepository.findById(3L).orElse(null)), LocalDate.ofYearDay(9999, 1), "111222555"),
                new Employee("Anna", "Mniejfajna", "1231234444", List.of(departmentRepository.findById(1L).orElse(null)), LocalDate.ofYearDay(9999, 1), "111222888"),
                new Employee("Magda", "Piąta", "1231235555", List.of(departmentRepository.findById(2L).orElse(null)), LocalDate.ofYearDay(9999, 1), "111222881"),
                new Employee("Wioletta", "Fioletowa", "1231236666", List.of(departmentRepository.findById(3L).orElse(null)), LocalDate.ofYearDay(9999, 1), "111222882"),
                new Employee("Jacek", "Gruby", "1231237777", departmentRepository.findDepartmentsByCountry(countries.get(0)), LocalDate.ofYearDay(9999, 1), "111222883"),
                new Employee("Tomasz", "Sążny", "1231237778", departmentRepository.findDepartmentsByCountry(countries.get(0)), LocalDate.ofYearDay(9999, 1), "111222884")
        );

        for (int i = 0; i < employees.size(); i++) {
            Employee employee = employees.get(i);
            if (i < 3) employee.setRole(Role.ROLE_MANAGER);
            if (i == employees.size() - 2) employee.setRole(Role.ROLE_COORDINATOR);
            if (i == employees.size() - 1) employee.setRole(Role.ROLE_DIRECTOR);

            employee.setPersonalId(e.encrypt(employee.getPersonalId()));
            employee.setContactNumber(e.encrypt(employee.getContactNumber()));

            userRepository.save(employee);
        }

        userRepository.save(new Admin("name", "surname", e.encrypt("100100100")));
    }

    private void createCredentials() throws RuntimeException {
        List<Credentials> credentials = List.of(
                new Credentials(1L, "user1@gmail.com", "password1"),
                new Credentials(2L, "user2@gmail.com", "password1"),
                new Credentials(3L, "user3@gmail.com", "password1"),
                new Credentials(4L, "user4@gmail.com", "password1"),
                new Credentials(5L, "user5@gmail.com", "password1"),
                new Credentials(6L, "user6@gmail.com", "password1"),

                new Credentials(8L, "manager1@gmail.com", "password1"),
                new Credentials(9L, "manager2@gmail.com", "password1"),
                new Credentials(10L, "manager3@gmail.com", "password1"),

                new Credentials(11L, "employee1@gmail.com", "password1"),
                new Credentials(12L, "employee2@gmail.com", "password1"),
                new Credentials(13L, "employee3@gmail.com", "password1"),

                new Credentials(14L, "coordinator1@gmail.com", "password1"),

                new Credentials(15L, "director1@gmail.com", "password1"),

                new Credentials(16L, "admin@gmail.com", "password1")
        );

        for (Credentials credential:credentials) {
            credential.setUsername(e.encrypt(credential.getUsername()));
            credential.setPassword(encoder.encode(credential.getPassword()));

            credentialsRepository.save(credential);
        }
    }

    private void createCompany() {
        companyRepository.save(new Company("Rent-a-Car", "https://github.com/Lavcrd/Rent-a-Car", "groupSIX", "Rent-a-Car"));
    }

    private void createDepartments() {
        departmentRepository.save(new Department(countries.get(0), "Katowice", "ul. Fajna", "1", "40-000", "car-rental-alpha@gmail.com", "500 500 501", false));
        departmentRepository.save(new Department(countries.get(0), "Łódź", "ul. Niefajna", "2", "90-000", "car-rental-beta@gmail.com", "500 500 502", false));
        departmentRepository.save(new Department(countries.get(0), "Gdańsk", "ul. Średnia", "3", "80-000", "car-rental-gamma@gmail.com", "500 500 503", false));
        departmentRepository.save(new Department(countries.get(0), "Warszawa", "ul. Pusta", "4", "00-001", "car-rental-delta@gmail.com", "500 500 504", true));
        departmentRepository.save(new Department(countries.get(0), "Białystok", "ul. Pełna", "5", "15-000", "car-rental-epsilon@gmail.com", "500 500 505", false));
        departmentRepository.save(new Department(countries.get(0), "Poznań", "ul. Półpełna", "6", "60-001", "car-rental-dzeta@gmail.com", "500 500 506", false));
        departmentRepository.save(new Department(countries.get(0), "Wrocław", "ul. Półpusta", "7", "50-001", "car-rental-eta@gmail.com", "500 500 507", false));
    }

    private void createCarBases() {
        carBaseRepository.save(new CarBase("https://cdn2.rcstatic.com/images/car_images/web/fiat/500_lrg.jpg", "Fiat", "Fiat 500", 2007, CarBase.CarType.TYPE_HATCHBACK, 2, 20.0, 190.0));
        carBaseRepository.save(new CarBase("/cars/bmw3.jpg", "BMW", "F34", 2013, CarBase.CarType.TYPE_HATCHBACK, 5, 25.0, 350.0));
        carBaseRepository.save(new CarBase("/cars/yaris.png", "Toyota", "Yaris", 1999, CarBase.CarType.TYPE_HATCHBACK, 4, 22.0, 250.0));
        carBaseRepository.save(new CarBase("/cars/hyundai-elantra.jpg", "Hyundai", "Lantra", 1991, CarBase.CarType.TYPE_COMPACT, 5, 23.0, 325.0));
        carBaseRepository.save(new CarBase("/cars/x5.jpg", "BMW", "X5 I", 1999, CarBase.CarType.TYPE_SUV, 5, 23.0, 300.0));
    }

    private void createCars() {
        carRepository.save(new Car(carBaseRepository.findById(1L).get(), departmentRepository.findById(1L).get(), countries.get(2).getCode() + "-ABC1234", 150000L, Car.CarStatus.STATUS_OPEN));
        carRepository.save(new Car(carBaseRepository.findById(2L).get(), departmentRepository.findById(1L).get(), countries.get(0).getCode() + "-ABC4321", 140000L, Car.CarStatus.STATUS_OPEN));
        carRepository.save(new Car(carBaseRepository.findById(3L).get(), departmentRepository.findById(1L).get(), countries.get(0).getCode() + "-ABC2314", 130000L, Car.CarStatus.STATUS_OPEN));
        carRepository.save(new Car(carBaseRepository.findById(4L).get(), departmentRepository.findById(1L).get(), countries.get(0).getCode() + "-ABC3214", 120000L, Car.CarStatus.STATUS_UNAVAILABLE));

        carRepository.save(new Car(carBaseRepository.findById(1L).get(), departmentRepository.findById(2L).get(), countries.get(1).getCode() + "-XBC1234", 150000L, Car.CarStatus.STATUS_UNAVAILABLE));
        carRepository.save(new Car(carBaseRepository.findById(2L).get(), departmentRepository.findById(2L).get(), countries.get(1).getCode() + "-AXX1234", 140000L, Car.CarStatus.STATUS_OPEN));
        carRepository.save(new Car(carBaseRepository.findById(3L).get(), departmentRepository.findById(2L).get(), countries.get(1).getCode() + "-ABX1234", 130000L, Car.CarStatus.STATUS_RENTED));
        carRepository.save(new Car(carBaseRepository.findById(4L).get(), departmentRepository.findById(2L).get(), countries.get(1).getCode() + "-AXC1234", 120000L, Car.CarStatus.STATUS_OPEN));

        carRepository.save(new Car(carBaseRepository.findById(1L).get(), departmentRepository.findById(3L).get(), countries.get(0).getCode() + "-AAC1234", 150000L, Car.CarStatus.STATUS_OPEN));
        carRepository.save(new Car(carBaseRepository.findById(2L).get(), departmentRepository.findById(3L).get(), countries.get(0).getCode() + "-AVC1234", 140000L, Car.CarStatus.STATUS_RENTED));
        carRepository.save(new Car(carBaseRepository.findById(3L).get(), departmentRepository.findById(3L).get(), countries.get(0).getCode() + "-AVV1234", 130000L, Car.CarStatus.STATUS_OPEN));
        carRepository.save(new Car(carBaseRepository.findById(4L).get(), departmentRepository.findById(3L).get(), countries.get(0).getCode() + "-ACC1234", 120000L, Car.CarStatus.STATUS_UNAVAILABLE));

        carRepository.save(new Car(carBaseRepository.findById(1L).get(), departmentRepository.findById(4L).get(), countries.get(0).getCode() + "-BBC1234", 150000L, Car.CarStatus.STATUS_OPEN));
        carRepository.save(new Car(carBaseRepository.findById(2L).get(), departmentRepository.findById(4L).get(), countries.get(0).getCode() + "-CCC1234", 140000L, Car.CarStatus.STATUS_OPEN));
        carRepository.save(new Car(carBaseRepository.findById(2L).get(), departmentRepository.findById(4L).get(), countries.get(0).getCode() + "-XXX1234", 130000L, Car.CarStatus.STATUS_OPEN));
        carRepository.save(new Car(carBaseRepository.findById(3L).get(), departmentRepository.findById(4L).get(), countries.get(0).getCode() + "-ZZZ1234", 120000L, Car.CarStatus.STATUS_UNAVAILABLE));

        carRepository.save(new Car(carBaseRepository.findById(4L).get(), departmentRepository.findById(5L).get(), countries.get(0).getCode() + "-BBB1234", 150000L, Car.CarStatus.STATUS_OPEN));
        carRepository.save(new Car(carBaseRepository.findById(3L).get(), departmentRepository.findById(5L).get(), countries.get(0).getCode() + "-MMM1234", 140000L, Car.CarStatus.STATUS_OPEN));
        carRepository.save(new Car(carBaseRepository.findById(2L).get(), departmentRepository.findById(5L).get(), countries.get(0).getCode() + "-NNN1234", 130000L, Car.CarStatus.STATUS_RENTED));
        carRepository.save(new Car(carBaseRepository.findById(1L).get(), departmentRepository.findById(5L).get(), countries.get(0).getCode() + "-VVV1234", 120000L, Car.CarStatus.STATUS_UNAVAILABLE));

        carRepository.save(new Car(carBaseRepository.findById(1L).get(), departmentRepository.findById(6L).get(), countries.get(0).getCode() + "-ABC123X", 150000L, Car.CarStatus.STATUS_OPEN));
        carRepository.save(new Car(carBaseRepository.findById(2L).get(), departmentRepository.findById(6L).get(), countries.get(0).getCode() + "-ABC12X4", 120000L, Car.CarStatus.STATUS_UNAVAILABLE));
        carRepository.save(new Car(carBaseRepository.findById(2L).get(), departmentRepository.findById(6L).get(), countries.get(0).getCode() + "-ABC1Z34", 140000L, Car.CarStatus.STATUS_RENTED));
        carRepository.save(new Car(carBaseRepository.findById(3L).get(), departmentRepository.findById(6L).get(), countries.get(0).getCode() + "-ABC1X34", 130000L, Car.CarStatus.STATUS_OPEN));

        carRepository.save(new Car(carBaseRepository.findById(3L).get(), departmentRepository.findById(7L).get(), countries.get(0).getCode() + "-ABC1A34", 150000L, Car.CarStatus.STATUS_OPEN));
        carRepository.save(new Car(carBaseRepository.findById(4L).get(), departmentRepository.findById(7L).get(), countries.get(0).getCode() + "-ABC1D34", 120000L, Car.CarStatus.STATUS_UNAVAILABLE));
        carRepository.save(new Car(carBaseRepository.findById(1L).get(), departmentRepository.findById(7L).get(), countries.get(0).getCode() + "-ABC1DD4", 140000L, Car.CarStatus.STATUS_OPEN));
        carRepository.save(new Car(carBaseRepository.findById(1L).get(), departmentRepository.findById(7L).get(), countries.get(0).getCode() + "-ABC12S4", 130000L, Car.CarStatus.STATUS_RENTED));

        carRepository.save(new Car(carBaseRepository.findById(1L).get(), departmentRepository.findById(1L).get(), countries.get(0).getCode() + "-AFC4322", 130000L, Car.CarStatus.STATUS_OPEN));
        carRepository.save(new Car(carBaseRepository.findById(4L).get(), departmentRepository.findById(1L).get(), countries.get(0).getCode() + "-ABF4323", 120000L, Car.CarStatus.STATUS_OPEN));
    }

    private void createReservation() {
        List<Reservation> reservationList = new LinkedList<>();
        reservationList.add(new Reservation((Customer) userRepository.findById(2L).orElse(null), carRepository.findById(1L).orElse(null).getCarBase(), departmentRepository.findById(1L).orElse(null), departmentRepository.findById(2L).orElse(null), LocalDate.now().minusDays(1), LocalDate.now().plusDays(3), LocalDate.of(2022, 10, 7)));
        reservationList.add(new Reservation((Customer) userRepository.findById(2L).orElse(null), carRepository.findById(1L).orElse(null).getCarBase(), departmentRepository.findById(1L).orElse(null), departmentRepository.findById(3L).orElse(null), LocalDate.now().minusDays(0), LocalDate.now().plusDays(2), LocalDate.of(2022, 10, 7)));
        reservationList.add(new Reservation((Customer) userRepository.findById(2L).orElse(null), carRepository.findById(1L).orElse(null).getCarBase(), departmentRepository.findById(1L).orElse(null), departmentRepository.findById(4L).orElse(null), LocalDate.now().minusDays(3), LocalDate.now().plusDays(4), LocalDate.of(2022, 10, 7)));
        reservationList.add(new Reservation((Customer) userRepository.findById(1L).orElse(null), carRepository.findById(1L).orElse(null).getCarBase(), departmentRepository.findById(2L).orElse(null), departmentRepository.findById(3L).orElse(null), LocalDate.now().minusDays(1), LocalDate.now().plusDays(3), LocalDate.of(2022, 10, 7)));
        reservationList.add(new Reservation((Customer) userRepository.findById(1L).orElse(null), carRepository.findById(10L).orElse(null).getCarBase(), departmentRepository.findById(3L).orElse(null), departmentRepository.findById(2L).orElse(null), LocalDate.now().minusDays(3), LocalDate.now().plusDays(1), LocalDate.of(2022, 11, 4)));
        reservationList.add(new Reservation((Customer) userRepository.findById(1L).orElse(null), carRepository.findById(19L).orElse(null).getCarBase(), departmentRepository.findById(5L).orElse(null), departmentRepository.findById(7L).orElse(null), LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), LocalDate.of(2022, 11, 8)));
        reservationList.add(new Reservation((Customer) userRepository.findById(1L).orElse(null), carRepository.findById(23L).orElse(null).getCarBase(), departmentRepository.findById(6L).orElse(null), departmentRepository.findById(5L).orElse(null), LocalDate.now().plusDays(0), LocalDate.now().plusDays(4), LocalDate.of(2022, 12, 5)));
        reservationList.add(new Reservation((Customer) userRepository.findById(1L).orElse(null), carRepository.findById(28L).orElse(null).getCarBase(), departmentRepository.findById(7L).orElse(null), departmentRepository.findById(6L).orElse(null), LocalDate.now().minusDays(2), LocalDate.now().plusDays(8), LocalDate.of(2022, 12, 9)));

        reservationList.add(new Reservation((Customer) userRepository.findById(3L).orElse(null), carRepository.findById(1L).orElse(null).getCarBase(), departmentRepository.findById(1L).orElse(null), departmentRepository.findById(2L).orElse(null), LocalDate.now().minusMonths(1), LocalDate.now().minusDays(15), LocalDate.of(2022, 10, 7)));
        reservationList.add(new Reservation((Customer) userRepository.findById(3L).orElse(null), carRepository.findById(1L).orElse(null).getCarBase(), departmentRepository.findById(1L).orElse(null), departmentRepository.findById(2L).orElse(null), LocalDate.now().minusDays(1), LocalDate.now().plusDays(15), LocalDate.of(2022, 10, 7)));

        byte i = 0;
        for (Reservation r : reservationList) {
            r.setStatus(Reservation.ReservationStatus.STATUS_RESERVED);
            if (i <= 3 || i == 8) r.setStatus(Reservation.ReservationStatus.STATUS_COMPLETED);
            if (i == 4 || i == 9) r.setStatus(Reservation.ReservationStatus.STATUS_PROGRESS);
            reservationRepository.save(r);
            i++;
        }
    }

    private void createRent() {
        rentRepository.save(new Rent(1L, reservationRepository.findById(1L).get(), carRepository.findById(1L).get(), 12L, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent imperdiet euismod ex, eget luctus lorem cursus vitae. Morbi mattis vel diam vitae hendrerit. Vivamus efficitur, elit vel pulvinar bibendum, enim. ", LocalDate.now().minusDays(20L), 100000L));
        rentRepository.save(new Rent(2L, reservationRepository.findById(2L).get(), carRepository.findById(1L).get(), 13L, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent imperdiet euismod ex, eget luctus lorem cursus vitae. Morbi mattis vel diam vitae hendrerit. Vivamus efficitur, elit vel pulvinar bibendum, enim. ", LocalDate.now().minusDays(19L), 100000L));
        rentRepository.save(new Rent(3L, reservationRepository.findById(3L).get(), carRepository.findById(1L).get(), 14L, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent imperdiet euismod ex, eget luctus lorem cursus vitae. Morbi mattis vel diam vitae hendrerit. Vivamus efficitur, elit vel pulvinar bibendum, enim. ", LocalDate.now().minusDays(18L), 100000L));
        rentRepository.save(new Rent(4L, reservationRepository.findById(4L).get(), carRepository.findById(1L).get(), 14L, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent imperdiet euismod ex, eget luctus lorem cursus vitae. Morbi mattis vel diam vitae hendrerit. Vivamus efficitur, elit vel pulvinar bibendum, enim. ", LocalDate.now().minusDays(17L), 100000L));
        rentRepository.save(new Rent(5L, reservationRepository.findById(5L).get(), carRepository.findById(10L).get(), 13L, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent imperdiet euismod ex, eget luctus lorem cursus vitae. Morbi mattis vel diam vitae hendrerit. Vivamus efficitur, elit vel pulvinar bibendum, enim. ", LocalDate.now(), 100000L));
        rentRepository.save(new Rent(9L, reservationRepository.findById(9L).get(), carRepository.findById(1L).get(), 12L, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent imperdiet euismod ex, eget luctus lorem cursus vitae. Morbi mattis vel diam vitae hendrerit. Vivamus efficitur, elit vel pulvinar bibendum, enim. ", LocalDate.now().minusDays(16L), 100000L));
        rentRepository.save(new Rent(10L, reservationRepository.findById(10L).get(), carRepository.findById(1L).get(), 12L, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent imperdiet euismod ex, eget luctus lorem cursus vitae. Morbi mattis vel diam vitae hendrerit. Vivamus efficitur, elit vel pulvinar bibendum, enim. ", LocalDate.now(), 100000L));
    }

    private void createRetrieve() {
        retrieveRepository.save(new Retrieve(1L, rentRepository.findById(1L).get(), 13L, LocalDate.now().minusDays(15), "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent imperdiet euismod ex, eget luctus lorem cursus vitae. Morbi mattis vel diam vitae hendrerit. Vivamus efficitur, elit vel pulvinar bibendum, enim. ", departmentRepository.findById(1L).get(), 120000L));
        retrieveRepository.save(new Retrieve(2L, rentRepository.findById(2L).get(), 14L, LocalDate.now().minusDays(15), "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent imperdiet euismod ex, eget luctus lorem cursus vitae. Morbi mattis vel diam vitae hendrerit. Vivamus efficitur, elit vel pulvinar bibendum, enim. ", departmentRepository.findById(2L).get(), 120000L));
        retrieveRepository.save(new Retrieve(3L, rentRepository.findById(3L).get(), 15L, LocalDate.now().minusDays(15), "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent imperdiet euismod ex, eget luctus lorem cursus vitae. Morbi mattis vel diam vitae hendrerit. Vivamus efficitur, elit vel pulvinar bibendum, enim. ", departmentRepository.findById(3L).get(), 120000L));
        retrieveRepository.save(new Retrieve(4L, rentRepository.findById(4L).get(), 11L, LocalDate.now().minusDays(15), "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent imperdiet euismod ex, eget luctus lorem cursus vitae. Morbi mattis vel diam vitae hendrerit. Vivamus efficitur, elit vel pulvinar bibendum, enim. ", departmentRepository.findById(4L).get(), 120000L));
        retrieveRepository.save(new Retrieve(9L, rentRepository.findById(9L).get(), 11L, LocalDate.now().minusDays(15), "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent imperdiet euismod ex, eget luctus lorem cursus vitae. Morbi mattis vel diam vitae hendrerit. Vivamus efficitur, elit vel pulvinar bibendum, enim. ", departmentRepository.findById(4L).get(), 130000L));
    }

    private void createPayments() {
        PaymentDetails pd1 = new PaymentDetails(600D, 120D, 1500D, 0D, 750D, 1L, currencies.get(0));
        PaymentDetails pd2 = new PaymentDetails(500D, 0D, 1500D, 0D, 800D, 2L, currencies.get(0));
        PaymentDetails pd3 = new PaymentDetails(700D, 120D, 1500D, 0D, 800D, 3L, currencies.get(0));
        PaymentDetails pd4 = new PaymentDetails(800D, 0D, 1500D, 0D, 750D, 4L, currencies.get(0));
        PaymentDetails pd5 = new PaymentDetails(800D, 0D, 1500D, 0D, 1500D, 5L, currencies.get(0));
        PaymentDetails pd9 = new PaymentDetails(800D, 0D, 1500D, 0D, 1500D, 9L, currencies.get(0));
        PaymentDetails pd10 = new PaymentDetails(800D, 0D, 1500D, 0D, 1500D, 10L, currencies.get(0));

        pd1.setSecured(720D);
        pd2.setSecured(700D);
        pd3.setSecured(920D);
        pd4.setSecured(900D);
        pd5.setSecured(800D);
        pd9.setSecured(800D);
        pd10.setSecured(800D);

        pd1.setPaymentAccepted(720D);
        pd2.setPaymentAccepted(500D);
        pd3.setPaymentAccepted(820D);
        pd4.setPaymentAccepted(800D);
        pd5.setPaymentAccepted(800D);
        pd9.setPaymentAccepted(800D);
        pd10.setPaymentAccepted(800D);

        pd1.setReleasedDeposit(0);
        pd2.setReleasedDeposit(100);
        pd3.setReleasedDeposit(700);
        pd4.setReleasedDeposit(100);
        pd5.setReleasedDeposit(0);
        pd9.setReleasedDeposit(0);
        pd10.setReleasedDeposit(0);

        paymentDetailsRepository.save(pd1);
        paymentDetailsRepository.save(pd2);
        paymentDetailsRepository.save(pd3);
        paymentDetailsRepository.save(pd4);
        paymentDetailsRepository.save(pd5);
        paymentDetailsRepository.save(pd9);
        paymentDetailsRepository.save(pd10);
    }

    private void createVerification() throws RuntimeException {
        List<Verification> verifications = List.of(new Verification(1L, countries.get(0), "123123123", "678678"));

        for (Verification verification:verifications) {
            verification.setDriverId(e.encrypt(verification.getDriverId()));
            verification.setPersonalId(e.encrypt(verification.getPersonalId()));
            verificationRepository.save(verification);
        }
    }
}
