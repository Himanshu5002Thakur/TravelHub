# TravelHub - Premium Travel Booking Platform

**GitHub Repository:** [https://github.com/Himanshu5002Thakur/TravelHub](https://github.com/Himanshu5002Thakur/TravelHub)

TravelHub is a full-featured travel booking platform built with **Spring Boot 3.5.11**, **Thymeleaf**, **Bootstrap 5.3.3**, **MySQL 8.0.39**, and **Java 17**.  
It provides a seamless user experience for browsing destinations, exploring travel packages, booking trips, and managing bookings—all backed by a premium, responsive design system.

---

## Table of Contents
1. [Features](#features)
2. [Tech Stack](#tech-stack)
3. [Design System](#design-system)
4. [Prerequisites](#prerequisites)
5. [Setup & Installation](#setup--installation)
6. [Running the Project](#running-the-project)
7. [Project Structure](#project-structure)
8. [Key Pages](#key-pages)
9. [Usage Guide](#usage-guide)
10. [Future Enhancements](#future-enhancements)

---

## Features

### Core Functionality
- **Home Page**: Hero section with search carousel, featured destination cards, travel tips
- **Country Pages**: Browse countries with curated top places and travel stats (duration, group size, language, trustworthiness)
- **Place Details**: Complete itinerary view, transparent pricing, amenities (What's Included), star rating system, prominent booking CTA
- **Booking Flow**: 
  - Traveler details form (name, email, phone, travel date, group size)
  - Transport selection (flight, train, bus, car)
  - Add-ons (premium food service, expert guide)
  - **Live price calculator** with instant total updates based on party size and services
  - Booking confirmation page with trip summary and trip ID
  - Trip preview card showing destination image and travel dates
- **User Management**: Full authentication (signup, login, logout) with secure session management
- **View Bookings**: Dashboard showing all past and upcoming bookings with transport info, dates, and pricing
- **Contact System**: Full-featured contact form with backend persistence (saves to database, not just popups)
- **About Page**: Company story, value propositions, and trust metrics
- **Responsive Design**: Mobile-first, optimized for all screen sizes

### Premium Design Features
- **Unified Design System**: Centralized `premium-pages.css` (1500+ lines) providing consistent branding across all pages
- **Color Palette**: Professional color scheme (ink #0b1220, sand #f8f4ec, ocean #0f4c81, sun #f0a500)
- **Smart Image Handling**: Fixed-height optimization (210px cards, 380px hero, 420px carousel) with automatic fallbacks
- **Responsive Navigation**: Mobile-first collapsible navbar with Spring Security integration
- **Premium Shadows**: Layered depth with brand-specific drop shadows (4-45px blur radius)
- **Typography**: Plus Jakarta Sans font with responsive clamp() sizing for mobile-friendly text
- **Interactive Components**: Feature cards, stat cards, transport badges, gradient button effects

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Backend** | Spring Boot 3.5.11, Spring Data JPA, Hibernate 6.6.42, Spring Security, Spring MVC |
| **Frontend** | Thymeleaf Template Engine, Bootstrap 5.3.3, Font Awesome 6.4.0, Custom CSS |
| **Database** | MySQL 8.0.39 with JPA/Hibernate ORM |
| **Build Tool** | Apache Maven 3.8+ |
| **Java Version** | JDK 17+ (LTS) |
| **Authentication** | Spring Security with form-based login and session management |

---

## Design System

### Centralized Stylesheet Architecture
All styling is managed through a single source of truth: `src/main/resources/static/css/premium-pages.css`

**Key Features:**
- **Scoped CSS Classes**: Each page wrapped with class hook (`.home-page`, `.country-page`, `.place-page`, etc.) for isolated styling
- **Consistent Spacing**: Standardized padding (20-34px) and gaps (3-4 units) across all components
- **Image Optimization**: Fixed heights instead of aspect-ratio for pixel-perfect consistency
  - 210px: Destination cards
  - 240px: Country pages
  - 380px: Hero sections
  - 420px: Carousel slides
  - 170px: Trip preview cards
- **Color Variables**: CSS custom properties for brand consistency
- **Responsive Breakpoints**:
  - 992px: Tablet optimization
  - 768px: Mobile optimization

### Premium Components
- **Hero Sections**: Background images with gradient overlays, clamp() responsive text
- **Card Grid Layouts**: Flex-based, 3-column desktop → 1-column mobile
- **Sticky Panels**: Booking sidebar and summary cards stick while scrolling
- **Gradient Effects**: Linear gradients on headers, buttons, and accent elements
- **Badges & Pills**: Transport type badges, status indicators with semantic colors
- **Form Styling**: Consistent input heights (46px), focus states, validation indicators

---

## Prerequisites

Before running the project, ensure you have installed:

- **Java Development Kit (JDK) 17** or later ([Download](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html))
- **Apache Maven 3.8+** ([Download](https://maven.apache.org/download.cgi))
- **MySQL Server 8.0+** ([Download](https://dev.mysql.com/downloads/mysql/))
  - Or use `docker run -d --name mysql8 -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 mysql:8.0`
- **Git** for cloning the repository
- **IDE**: IntelliJ IDEA, Eclipse, VS Code, or similar
- **Web Browser**: Chrome, Firefox, Safari, or Edge (latest version recommended)

---

## Setup & Installation

### 1. Clone the Repository
```bash
git clone https://github.com/Himanshu5002Thakur/TravelHub.git
cd TravelHub
```

### 2. Configure the Database
Open `src/main/resources/application.properties` and update the MySQL credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/traveldb?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_password_here
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
```

**Note:** If the database doesn't exist, Hibernate will create it automatically.

### 3. (Optional) Add Travel Images
Place destination image files in `src/main/resources/static/images/`:
- The app has fallback images, but adding destination-specific images enhances the experience
- Supported formats: JPG, PNG
- Recommended size: 400x300px or larger

### 4. Build the Project
```bash
mvn clean install
```

This downloads all dependencies and compiles the application.

---

## Running the Project

### Using Maven
```bash
mvn spring-boot:run
```

### Using IDE
- IntelliJ: Right-click `TravelWebsiteApplication.java` → Run
- Eclipse: Right-click project → Run As → Spring Boot App

### Access the Application
Open your browser and navigate to:
```
http://localhost:8080/home
```

**Port Note:** The app runs on port 8080 by default. To change it, add to `application.properties`:
```properties
server.port=8081
```

### Verify Startup
You should see logs like:
```
Started TravelWebsiteApplication in X seconds
Hibernate: create table...
Server started successfully at http://localhost:8080
```

---

## Project Structure

```
TravelHub
├── src/
│   ├── main/
│   │   ├── java/com/travel/
│   │   │   ├── TravelWebsiteApplication.java          # Main Spring Boot entry point
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java                # Spring Security configuration
│   │   │   ├── controller/
│   │   │   │   ├── HomeController.java                # Home page, country, place routes
│   │   │   │   └── AuthController.java                # Login, signup, logout
│   │   │   ├── entity/
│   │   │   │   ├── User.java                          # User model
│   │   │   │   ├── Place.java                         # Travel destination model
│   │   │   │   ├── Booking.java                       # Booking record model
│   │   │   │   └── ContactMessage.java                # Contact form submissions
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java                # Database queries for users
│   │   │   │   ├── PlaceRepository.java               # Database queries for places
│   │   │   │   ├── BookingRepository.java             # Database queries for bookings
│   │   │   │   └── ContactRepository.java             # Database queries for messages
│   │   │   └── service/
│   │   │       ├── UserService.java                   # User business logic
│   │   │       └── PackageService.java                # Package/pricing logic
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── css/
│   │       │   │   └── premium-pages.css              # Centralized styling (1500+ lines)
│   │       │   ├── images/                            # Destination and banner images
│   │       │   └── js/                                # Frontend JavaScript (if any)
│   │       ├── templates/
│   │       │   ├── base.html                          # Master layout template
│   │       │   ├── home.html                          # Landing page
│   │       │   ├── country.html                       # Country places list
│   │       │   ├── place.html                         # Place details & itinerary
│   │       │   ├── traveller-details.html             # Booking form
│   │       │   ├── booking-success.html               # Confirmation page
│   │       │   ├── my-bookings.html                   # Booking history dashboard
│   │       │   ├── about.html                         # Company info
│   │       │   ├── contact.html                       # Contact form
│   │       │   ├── login.html                         # User login
│   │       │   └── signup.html                        # User registration
│   │       ├── packages/                              # Package data files (fallback)
│   │       │   └── *.txt                              # Place descriptions & pricing
│   │       └── application.properties                 # Configuration file
│   └── test/
│       └── java/com/travel/
│           └── TravelWebsiteApplicationTests.java     # Unit tests
├── pom.xml                                             # Maven build configuration
└── README.md                                           # This file
```

---

## Key Pages

| Page | Route | Purpose |
|------|-------|---------|
| Home | `/home` | Landing page, search carousel, featured destinations |
| Country | `/country/{name}` | List all places in a country with stats |
| Place | `/place/{name}` | View full itinerary, pricing, and book option |
| Book Trip | `/book/{placeName}` | Collect traveler details and preferences |
| Confirmation | `/booking-success` | Show booking confirmation after successful booking |
| My Bookings | `/my-bookings` | View all user bookings and manage them |
| About | `/about` | Company story and trust metrics |
| Contact | `/contact` | Send contact message (saved to database) |
| Login | `/login` | User authentication |
| Signup | `/signup` | Create new user account |

---

## Usage Guide

### For End Users

**Booking a Trip:**
1. Start at **Home** page (`/home`)
2. Browse the carousel or destination cards
3. Click on a destination to view the **Country** page
4. Select a specific **Place** to view full details and itinerary
5. Click **"Continue to Booking"** button
6. Fill in traveler details (name, email, phone, travel date, group size)
7. Choose transport and add-ons (food, guide)
8. Review the live price total
9. Click **"Book Trip"** to confirm
10. See booking confirmation with trip ID
11. View all bookings in **"My Bookings"** dashboard

**Contacting Support:**
1. Navigate to **Contact** page (`/contact`)
2. Fill in name, email, and message
3. Submit the form
4. Message is saved to database and processed by admin

**Viewing Bookings:**
1. Login with your account
2. Click **"My Bookings"** in navbar
3. View all past and upcoming trips
4. See transport, dates, and pricing for each booking
5. Option to cancel bookings (if available)

### For Developers

**Adding a New Package:**
1. Place package data in `src/main/resources/packages/` (`.txt` format)
2. Add corresponding place entity in database
3. Create destination image in `static/images/`
4. New place will automatically appear on relevant country pages

**Styling New Pages:**
1. Create wrapper div with class hook: `<div class="your-page-name">`
2. Add CSS rules in `premium-pages.css` under appropriate section
3. Use existing color variables and spacing system for consistency

**Adding Authentication:**
- Already integrated with Spring Security
- Login endpoint: `/login` (form-based)
- Logout: Click navbar logout link
- Access secured routes using `sec:authorize` in Thymeleaf templates

---

## Future Enhancements

### Phase 1: Payment Integration
- [ ] Integrate Stripe or Razorpay payment gateway
- [ ] Secure checkout flow with card validation
- [ ] Payment confirmation emails

### Phase 2: Advanced Booking Features
- [ ] Coupon and discount code system
- [ ] Group booking discounts
- [ ] Dynamic pricing based on demand and season
- [ ] Cancellation and refund policies

### Phase 3: User Experience
- [ ] Reviews and ratings system (5-star reviews with photos)
- [ ] Wishlist/favorites functionality
- [ ] Advanced search filters (budget, duration, ratings, difficulty)
- [ ] Personalized travel recommendations
- [ ] Email notifications for bookings, updates, deals

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
