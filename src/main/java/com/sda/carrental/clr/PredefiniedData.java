package com.sda.carrental.clr;

import com.sda.carrental.global.ConstantValues;
import com.sda.carrental.global.enums.Country;
import com.sda.carrental.model.Company;
import com.sda.carrental.model.operational.Renting;
import com.sda.carrental.model.operational.Reservation;
import com.sda.carrental.model.operational.Returning;
import com.sda.carrental.model.property.Car;
import com.sda.carrental.model.property.Department;
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
    private final RentingRepository rentingRepository;
    private final PaymentDetailsRepository paymentDetailsRepository;
    private final VerificationRepository verificationRepository;
    private final CredentialsRepository credentialsRepository;
    private final ReturningRepository returningRepository;
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
        createReturn();
        /*        createInvoice();*/ // currently creates duplicates due to not automated reservation statuses.

        createVerification();
    }

    private void createUsers() {
        userRepository.save(new Customer("Anna", "Nazwiskowa", Country.COUNTRY_PL, "Kraków", Customer.CustomerStatus.STATUS_REGISTERED, "ul. Ulica 123", "123312891"));
        userRepository.save(new Customer("Jakub", "Kowalski", Country.COUNTRY_PL, "Katowice", Customer.CustomerStatus.STATUS_REGISTERED, "ul. Ulica 12", "123312892"));
        userRepository.save(new Customer("Maciek", "Masło", Country.COUNTRY_PL, "Gdynia", Customer.CustomerStatus.STATUS_REGISTERED, "ul. Ulica 1", "123312893"));
        userRepository.save(new Customer("Jan", "Orzech", Country.COUNTRY_PL, "Gdańsk", Customer.CustomerStatus.STATUS_REGISTERED, "ul. Ulica 124", "123312894"));
        userRepository.save(new Customer("Katarzyna", "Kasztan", Country.COUNTRY_PL, "Warszawa", Customer.CustomerStatus.STATUS_REGISTERED, "ul. Ulica 133", "123312895"));
        userRepository.save(new Customer("Igor", "Kasztan", Country.COUNTRY_PL, "Białystok", Customer.CustomerStatus.STATUS_REGISTERED, "ul. Ulica 137", "123312896"));
        userRepository.save(new Customer("Anna", "Kowalska", Country.COUNTRY_PL, "Opole", Customer.CustomerStatus.STATUS_REGISTERED, "ul. Ulica 138", "123312897"));


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
        /*credentialsRepository.save(new Credentials(7L, "user7@gmail.com", encoder.encode("password1")));*/

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
        rentingRepository.save(new Renting(12L, 1L, "N/D", LocalDate.now()));
        rentingRepository.save(new Renting(13L, 2L, "N/D", LocalDate.now()));
        rentingRepository.save(new Renting(14L, 3L, "N/D", LocalDate.now()));
        rentingRepository.save(new Renting(14L, 4L, "N/D", LocalDate.now()));
        rentingRepository.save(new Renting(13L, 5L, "N/D", LocalDate.now()));
    }

    private void createReturn() {
        returningRepository.save(new Returning(1L, 13L, LocalDate.now().plusWeeks(1), "Some notes after return of the car."));
    }

    private void createPayments() {
        //paymentDetailsRepository.save(new PaymentDetails( carRepository.findById(7L).get().getPrice_day() * (reservationRepository.findById(1L).get().getDateFrom().until(reservationRepository.findById(1L).get().getDateTo(), ChronoUnit.DAYS)), gv.getDeptReturnPriceDiff(), carRepository.findById(7L).get().getDepositValue(), carRepository.findById(7L).get().getPrice_day() * (reservationRepository.findById(1L).get().getDateFrom().until(reservationRepository.findById(1L).get().getDateTo(), ChronoUnit.DAYS)),  carRepository.findById(7L).get().getDepositValue(), reservationRepository.findById(1L).orElse(null)));
        //paymentDetailsRepository.save(new PaymentDetails(carRepository.findById(10L).get().getPrice_day() * (reservationRepository.findById(2L).get().getDateFrom().until(reservationRepository.findById(2L).get().getDateTo(), ChronoUnit.DAYS)), gv.getDeptReturnPriceDiff(), carRepository.findById(10L).get().getDepositValue(), carRepository.findById(10L).get().getPrice_day() * (reservationRepository.findById(2L).get().getDateFrom().until(reservationRepository.findById(2L).get().getDateTo(), ChronoUnit.DAYS)), carRepository.findById(10L).get().getDepositValue(), reservationRepository.findById(2L).orElse(null)));
        //paymentDetailsRepository.save(new PaymentDetails(carRepository.findById(19L).get().getPrice_day() * (reservationRepository.findById(3L).get().getDateFrom().until(reservationRepository.findById(3L).get().getDateTo(), ChronoUnit.DAYS)), gv.getDeptReturnPriceDiff(), carRepository.findById(19L).get().getDepositValue(), carRepository.findById(19L).get().getPrice_day() * (reservationRepository.findById(3L).get().getDateFrom().until(reservationRepository.findById(3L).get().getDateTo(), ChronoUnit.DAYS)), carRepository.findById(19L).get().getDepositValue(), reservationRepository.findById(3L).orElse(null)));
        //paymentDetailsRepository.save(new PaymentDetails(carRepository.findById(23L).get().getPrice_day() * (reservationRepository.findById(4L).get().getDateFrom().until(reservationRepository.findById(4L).get().getDateTo(), ChronoUnit.DAYS)), gv.getDeptReturnPriceDiff(), carRepository.findById(23L).get().getDepositValue(), carRepository.findById(23L).get().getPrice_day() * (reservationRepository.findById(4L).get().getDateFrom().until(reservationRepository.findById(4L).get().getDateTo(), ChronoUnit.DAYS)), carRepository.findById(23L).get().getDepositValue(), reservationRepository.findById(4L).orElse(null)));
        //paymentDetailsRepository.save(new PaymentDetails(carRepository.findById(28L).get().getPrice_day() * (reservationRepository.findById(5L).get().getDateFrom().until(reservationRepository.findById(5L).get().getDateTo(), ChronoUnit.DAYS)), gv.getDeptReturnPriceDiff(), carRepository.findById(28L).get().getDepositValue(), carRepository.findById(28L).get().getPrice_day() * (reservationRepository.findById(5L).get().getDateFrom().until(reservationRepository.findById(5L).get().getDateTo(), ChronoUnit.DAYS)), carRepository.findById(28L).get().getDepositValue(), reservationRepository.findById(5L).orElse(null)));
    }

    private void createVerification() {
        verificationRepository.save(new Verification(1L, "123123", "678678"));
    }
}
