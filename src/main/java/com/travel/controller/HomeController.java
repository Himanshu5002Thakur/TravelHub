package com.travel.controller;

import com.travel.entity.Booking;
import com.travel.entity.ContactMessage;
import com.travel.repository.BookingRepository;
import com.travel.repository.ContactRepository;
import com.travel.service.PackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import com.travel.entity.User;
import com.travel.repository.UserRepository;
import com.travel.entity.Place;
import com.travel.repository.PlaceRepository;
import java.util.List;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Controller
public class HomeController {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z ]{2,50}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10,15}$");

    @Autowired private PackageService packageService;
    @Autowired private ContactRepository contactRepo;
    @Autowired private BookingRepository bookingRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private PlaceRepository placeRepo;

   @ControllerAdvice
public class GlobalControllerAdvice {
    
    @Autowired 
    private UserRepository userRepo;

    @ModelAttribute
    public void addAttributes(Model model, Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            userRepo.findByEmail(auth.getName()).ifPresent(user -> {
                // This makes 'displayName' available on every page automatically
                model.addAttribute("displayName", user.getUsername());
            });
        }
    }
}


   @GetMapping("/home")
public String home(@RequestParam(required = false) String location, Authentication auth, Model model) {
    // 1. Handle the search logic
    if (location != null && !location.isEmpty()) {
        // Query your database for countries matching the search string
        model.addAttribute("countries", placeRepo.findCountriesBySearch(location));
    } else {
        // Show default list if no search is performed
        model.addAttribute("countries", List.of("India", "US", "Canada", "UK"));
    }

    // 2. Pass the display name for the navbar (from your GlobalAdvice or here)
    if (auth != null && auth.isAuthenticated()) {
        userRepo.findByEmail(auth.getName()).ifPresent(user -> {
            model.addAttribute("displayName", user.getUsername());
        });
    }

    return "home";
}

    @GetMapping("/country/{country}")
public String country(@PathVariable String country, Model model) {
    // Instead of a hardcoded Map, we fetch from the database
    // For this to work, you'll need a PlaceRepository (shown below)
    List<Place> places = placeRepo.findByCountryName(country);
    
    model.addAttribute("country", country);
    model.addAttribute("places", places);
    return "country";
}


   @GetMapping("/place/{placeName}")
public String place(@PathVariable String placeName, Model model) {
    Place placeObj = placeRepo.findByNameIgnoreCase(placeName).orElse(null);

    model.addAttribute("placeName", placeName);
    model.addAttribute("place", placeObj);
    model.addAttribute("country", placeObj != null ? placeObj.getCountryName() : null);
    return "place";
}

    @GetMapping("/contact")
    public String contact() { return "contact"; }

    @PostMapping("/contact")
    public String saveContact(@RequestParam String name, @RequestParam String email, 
                              @RequestParam String message, RedirectAttributes ra) {
        ContactMessage msg = new ContactMessage();
        msg.setName(name);
        msg.setEmail(email);
        msg.setMessage(message);
        contactRepo.save(msg);
        ra.addFlashAttribute("success", "Message saved successfully! We will contact you soon.");
        return "redirect:/contact";
    }

    @GetMapping("/booking/traveller/{place}")
    public String travellerDetails(@PathVariable String place, Model model) {
        model.addAttribute("place", place);
        return "traveller-details";
    }

    @PostMapping("/booking-confirm")
public String confirm(@RequestParam String place, @RequestParam String name,
                      @RequestParam String email, @RequestParam String phone,
                      @RequestParam int people, @RequestParam String date,
                      @RequestParam(defaultValue = "Bus") String transport,
                      @RequestParam(defaultValue = "false") boolean food,
                      @RequestParam(defaultValue = "false") boolean guide,
                          Authentication auth, Model model, RedirectAttributes ra) {

        String cleanName = name != null ? name.trim() : "";
        String cleanEmail = email != null ? email.trim().toLowerCase() : "";
        String cleanPhone = phone != null ? phone.trim() : "";

        if (!NAME_PATTERN.matcher(cleanName).matches()) {
            ra.addFlashAttribute("error", "Traveller name must contain only letters and spaces (2 to 50 characters).");
            ra.addAttribute("place", place);
            return "redirect:/booking/traveller/{place}";
        }

        if (!EMAIL_PATTERN.matcher(cleanEmail).matches()) {
            ra.addFlashAttribute("error", "Please enter a valid email address.");
            ra.addAttribute("place", place);
            return "redirect:/booking/traveller/{place}";
        }

        if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
            ra.addFlashAttribute("error", "Phone number must be 10 to 15 digits only.");
            ra.addAttribute("place", place);
            return "redirect:/booking/traveller/{place}";
        }

        if (people < 1 || people > 20) {
            ra.addFlashAttribute("error", "Number of travellers must be between 1 and 20.");
            ra.addAttribute("place", place);
            return "redirect:/booking/traveller/{place}";
        }

        if (!transport.equals("Bus") && !transport.equals("Train") && !transport.equals("Flight")) {
            ra.addFlashAttribute("error", "Please choose a valid transport option.");
            ra.addAttribute("place", place);
            return "redirect:/booking/traveller/{place}";
        }

        LocalDate travelDate;
        try {
            travelDate = LocalDate.parse(date);
        } catch (DateTimeParseException ex) {
            ra.addFlashAttribute("error", "Please select a valid travel date.");
            ra.addAttribute("place", place);
            return "redirect:/booking/traveller/{place}";
        }

        if (travelDate.isBefore(LocalDate.now())) {
            ra.addFlashAttribute("error", "Travel date cannot be in the past.");
            ra.addAttribute("place", place);
            return "redirect:/booking/traveller/{place}";
        }

    // 1. Calculation Logic
    double basePrice = 399.0;
    double transportExtra = transport.equals("Flight") ? 150.0 : (transport.equals("Train") ? 50.0 : 0.0);
    double foodExtra = food ? 40.0 : 0.0;
    double guideExtra = guide ? 60.0 : 0.0;
    double finalPrice = (basePrice + transportExtra + foodExtra + guideExtra) * people;

    // 2. Map data to Booking Entity
    Booking b = new Booking();
    b.setPlaceName(place);
    b.setTravellerName(cleanName);
    b.setEmail(cleanEmail);
    b.setPhone(cleanPhone);
    b.setTotalPeople(people);
    b.setTravelDate(travelDate);
    b.setTransportType(transport);
    b.setWantsFood(food);
    b.setWantsGuide(guide);
    b.setTotalPrice(finalPrice);
    b.setTripId("TRV-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());

    // 3. Link to Logged-in User
    if (auth != null && auth.isAuthenticated()) {
        userRepo.findByEmail(auth.getName()).ifPresent(user -> {
            b.setUser(user); 
        });
    }

    bookingRepo.save(b);
    return "redirect:/my-bookings";
}

   // Show the "My Bookings" page
@GetMapping("/my-bookings")
public String myBookings(Authentication auth, Model model) {
    if (auth != null) {
        String email = auth.getName(); // Our security uses email as the username
        User user = userRepo.findByEmail(email).orElse(null);
        if (user != null) {
            model.addAttribute("bookings", bookingRepo.findByUser(user));
        }
    }
    return "my-bookings";
}

// Cancel (Delete) a booking
@GetMapping("/booking/cancel/{id}")
public String cancelBooking(@PathVariable Long id, RedirectAttributes ra) {
    bookingRepo.deleteById(id);
    ra.addFlashAttribute("success", "Booking cancelled successfully.");
    return "redirect:/my-bookings";
}

    @GetMapping("/about")
    public String about() { return "about"; }

    @GetMapping("/")
    public String index() { return "redirect:/login"; }

    @ModelAttribute
public void addAttributes(Model model, Authentication auth) {
    if (auth != null && auth.isAuthenticated()) {
        // Find user by Email (which is the login ID) and put their Username in the model
        userRepo.findByEmail(auth.getName()).ifPresent(user -> {
            model.addAttribute("displayName", user.getUsername());
        });
    }
}
}