# Rent-A-Car System

## Few words:
    The project was started as part of the SDA Java course in a group of three people.
    However, due to the end of activities related to doing this project, the group disbanded at the end of the course.
    Being aware that even if it is a common project and I will be alone, I will get a lot from it, and that's what happened.

## Technology:
    Backend: Spring Boot
    Frontend: Thymeleaf, Bootstrap
    Database: MySQL
    Others: Leaflet (used in car page)

## Features:

    > Customer POV
    -   Log in
    -   Log out
    -   Registration
    -   Profile view
    -   Password change
    -   Contact change
    -   View reservations (list of all by date order)

    > Rental process
    -   Car reservation (customer/employee process POV, car filtering based on availability)
    -   Car rental (payment, notes, car confirmation)
    -   Car retrieval (car transfer, notes, possibility of retrieving by plates)

    > Overview
    -   Department view (QoL view available to common employee)
    -   Statistics (common, but available to coordinator+)

    > Customer management
    -   Search
    -   Modifying data
    -   Manipulation of data (Deletion, merge)
    -   Verification

    > Reservation management
    -   Search
    -   Car substitution (swapping car in reservation)
    -   Cancel (without cancellation fee%)
    -   Refund (with cancellation fee%)
    -   Payment manipulation (ability for discounts or additional costs)

    > Payment management
    -   Search
    -   Settlement of deposit (Release/Charge)
    -   Inspection of transactions (history and current, notes, rental details)
    -   Automatic refunds

    > Car management
    -   Search
    -   Adding car templates (Image, car type, seats, base price)
    -   Adding car under these templates
    -   Modification of data (Status, mileage, department)
    -   Manipulation of data (Deletion, car base transfer)
    -   Car inspection (individual params like mileage, history, location etc.)

    > Department management
    -   Search
    -   Modification of data (local price multiplier, activity switch, hq switch, details)

    > Employee management
    -   Registration
    -   Details (personal data and role)
    -   Password change
    -   Department access
    -   Ability to set expiration date

    > Company management
    -   Logotype change
    -   Details change (name, website link, owner name)
    -   Currency used within app (details etc.) (exchange rate manual or scheduled if json provided)
    -   Countries used within app (with settings such as local currency, price multiplier or details)
    -   Global settings (cancellation % fee, cancellation timeframe, refund timeframe, car cooldown for customer filtering)

    > Encryption
    -   Password (Spring BCryptPasswordEncoder)
    -   Data (AES with Base64 key)

    > Other
    -   Common pages (about us, regulations, contact)
    -   Profile view (user data, employee included)
    -   Role hierarchy within app
    -   Access to data within app (department, employee-customer, reservation, access to pages depending on role etc.)
    -   Custom form validators
    -   Predefined data to play around within app

    > Tests
    -   TODO

## Screenshots
    -   TODO

