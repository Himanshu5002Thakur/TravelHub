# TravelHub
A travel website project built with Spring Boot, Thymeleaf, and Bootstrap.
# TravelHub - Travel Website

GitHub Repository: [https://github.com/Himanshu5002Thakur/TravelHub](https://github.com/Himanshu5002Thakur/TravelHub)

TravelHub is a demo travel website built with **Spring Boot**, **Thymeleaf**, **Bootstrap**, and **Java**.  
It allows users to browse countries, places, view travel packages, book trips, and submit contact messages.

---

## Table of Contents
1. [Features](#features)
2. [Prerequisites](#prerequisites)
3. [Setup & Installation](#setup--installation)
4. [Running the Project](#running-the-project)
5. [Project Structure](#project-structure)
6. [Usage](#usage)
7. [Screenshots](#screenshots)
8. [Future Enhancements](#future-enhancements)
9. [License](#license)

---

## Features

- Home page with travel hero banner and destination cards
- Country pages with top places
- Place page with travel packages and “Book Now” flow
- Booking form with traveller details
- Booking success page
- Contact page with popup confirmation
- Responsive design with Bootstrap

---

## Prerequisites

Before running the project, make sure you have:

- **Java JDK 17** or later
- **Maven 3.8+**
- **MySQL Server** (optional if you want to connect database)
- **IDE**: IntelliJ, Eclipse, or VS Code recommended
- **Web browser** to view the website

---

## Setup & Installation

1. Clone the repository:

```bash
git clone https://github.com/Himanshu5002Thakur/TravelHub.git
cd TravelHub

2. Import the project in your IDE as Maven project.

Configure database (optional if using DB):

Open src/main/resources/application.properties

Update MySQL credentials (if using DB):

spring.datasource.url=jdbc:mysql://localhost:3306/traveldb?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=1234
spring.jpa.hibernate.ddl-auto=update

If you want a demo version, you can skip DB setup; the contact form works with a popup.

Make sure src/main/resources/static/images/ folder contains required travel images.

Running the Project

Open terminal in project root

Run:

mvn clean install
mvn spring-boot:run

Open browser: http://localhost:8080/home

Project Structure
TravelHub/
├── src/
│   ├── main/
│   │   ├── java/com/travel/
│   │   │   ├── controller/HomeController.java
│   │   │   ├── service/PackageService.java
│   │   │   ├── model/ContactMessage.java
│   │   │   └── repository/ContactMessageRepository.java
│   │   └── resources/
│   │       ├── static/images/         # Banner & place images
│   │       ├── templates/             # Thymeleaf HTML pages
│   │       │   ├── home.html
│   │       │   ├── country.html
│   │       │   ├── place.html
│   │       │   ├── traveller-details.html
│   │       │   ├── booking-success.html
│   │       │   ├── payment.html
│   │       │   ├── about.html
│   │       │   └── contact.html
│   │       └── application.properties
├── pom.xml
└── README.md
Usage

Browse Home page → select a country → choose a place

Click Book Now → fill traveller details → see Booking Confirmation

Click Contact → submit message → see confirmation


Future Enhancements

Add email sending functionality on contact form

Integrate real payment gateway
