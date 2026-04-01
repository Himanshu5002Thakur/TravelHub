# Travel Website (Spring Boot)

Travel booking web application built with Spring Boot, Thymeleaf, Spring Security, and MySQL.

## Overview

This project provides:
- Destination browsing by country and place
- Itinerary pages
- Authenticated booking flow
- Payment step (UPI/manual and Razorpay card flow)
- Booking success and booking history pages
- Contact form persistence
- Admin-style report page

The app runs on port `8081` by default.

## Tech Stack

- Java 17
- Spring Boot 3.5.11
- Spring MVC + Thymeleaf
- Spring Security
- Spring Data JPA (Hibernate)
- MySQL
- Maven Wrapper (`mvnw`, `mvnw.cmd`)

## Main Features

- Home page with search and curated country cards
- Country pages for places in each country
- Place detail page with itinerary and pricing
- Traveller details page with live cost breakdown
- Food/meal selection with business-rule validation
- Payment page with Razorpay integration support
- Booking confirmation and My Bookings list
- Signup/Login with BCrypt password hashing

## Business Rules Implemented

- Standard package display rate is aligned to a single label: `₹60,000 per person` (country cards).
- Country naming normalized for consistency:
  - `US`, `USA` -> `United States`
  - `UK`, `U.K.` -> `United Kingdom`
- Meal plate limit is traveller-aware:
  - Maximum per meal type = `6 × number of travellers`
  - Applied in both frontend checks and backend validation
  - Example: 8 travellers -> max 48 Breakfast plates, 48 Lunch plates, 48 Dinner plates

## Security and Access

Public routes include:
- `/`, `/home`, `/about`, `/contact`, `/login`, `/signup`
- Static assets under `/css/**`, `/js/**`, `/images/**`

All other routes require login.

## Important Routes

- `GET /home`
- `GET /country/{country}`
- `GET /place/{placeName}`
- `GET /booking/traveller/{place}`
- `POST /booking-confirm`
- `GET /payment/{bookingId}`
- `POST /payment/{bookingId}`
- `GET /booking-success/{bookingId}`
- `GET /my-bookings`
- `GET /booking/cancel/{id}`
- `GET /report`

## Project Structure

```text
src/main/java/com/travel/
  config/          # Security + data initialization
  controller/      # AuthController, HomeController
  entity/          # User, Place, Booking, ContactMessage
  repository/      # JPA repositories
  service/         # UserService, PackageService, PaymentGatewayService

src/main/resources/
  templates/       # Thymeleaf pages
  static/css/      # premium-pages.css
  static/images/   # images
  packages/        # itinerary text files
  application.properties
```

## Prerequisites

- JDK 17+
- MySQL 8+
- Maven (optional if using wrapper)

## Configuration

Edit `src/main/resources/application.properties`:

```properties
server.port=8081

spring.datasource.url=jdbc:mysql://localhost:3306/traveldb?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.thymeleaf.cache=false
```

Payment-related keys are also configured in `application.properties`.
Use your own values locally and do not commit real secrets.

## Run Locally

Windows:

```bash
./mvnw.cmd spring-boot:run
```

macOS/Linux:

```bash
./mvnw spring-boot:run
```

Open:

```text
http://localhost:8081/home
```

## Build and Test

Compile only:

```bash
./mvnw.cmd -DskipTests compile
```

Run tests:

```bash
./mvnw.cmd test
```

## Notes

- Place data is auto-initialized/normalized at startup by `DataLoader`.
- If app startup fails, first verify MySQL is running and DB credentials are correct.
- If Razorpay is not configured, card checkout is unavailable; UPI/manual flow remains available.

### Phase 4: Admin Panel
- [ ] Dashboard with bookings, users, and revenue metrics
- [ ] Content management (add/edit/delete places and packages)
- [ ] User management (approve signups, manage accounts)
- [ ] Analytics and reporting
- [ ] Support ticket system for customer inquiries

### Phase 5: Performance & Scale
- [ ] Database indexing and query optimization
- [ ] Redis caching for frequently accessed data
- [ ] CDN integration for images
- [ ] Load testing and performance monitoring
- [ ] API rate limiting and security hardening

---

## Troubleshooting

**Port 8080 Already in Use**
```bash
# Windows: Kill the process using port 8080
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac:
lsof -i :8080
kill -9 <PID>
```

**Database Connection Error**
- Verify MySQL is running
- Check credentials in `application.properties`
- Ensure database `traveldb` exists or allow Hibernate to create it

**Thymeleaf Template Not Loading**
- Verify template file exists in `src/main/resources/templates/`
- Check controller route mapping
- Verify return statement in controller (e.g., `return "home"`)

**Images Not Displaying**
- Add images to `src/main/resources/static/images/`
- Browser fallback will show `/images/travel-banner.jpg` if image is missing
- Clear browser cache (Ctrl+Shift+Del)

---

## License

This project is open-source. Feel free to fork, modify, and use for personal or educational purposes.

---

## Contributing

Issues and pull requests are welcome! To contribute:
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/YourFeatureName`)
3. Commit your changes (`git commit -m 'Add feature'`)
4. Push to the branch (`git push origin feature/YourFeatureName`)
5. Open a pull request

---
