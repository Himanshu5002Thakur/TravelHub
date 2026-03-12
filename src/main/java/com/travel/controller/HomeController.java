package com.travel.controller;

import com.travel.service.PackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class HomeController {
    @Autowired
    private PackageService packageService;

    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("countries", new String[]{"India", "US", "Canada", "UK"});
        return "home";
    }

    @GetMapping("/country/{country}")
    public String country(@PathVariable String country, Model model) {
        Map<String, String[]> placesMap = Map.of(
            "India", new String[]{"Kashmir", "Shimla", "Mumbai", "Agra"},
            "US", new String[]{"New York", "Los Angeles", "Miami"},
            "Canada", new String[]{"Toronto", "Vancouver", "Niagara"},
            "UK", new String[]{"London", "Edinburgh", "Manchester"}
        );
        String[] places = placesMap.getOrDefault(country, new String[]{});
        model.addAttribute("country", country);
        model.addAttribute("places", places);
        return "country";
    }

    @GetMapping("/place/{place}")
    public String place(@PathVariable String place, Model model) {
        model.addAttribute("place", place);
        model.addAttribute("packageContent", packageService.getPackageContent(place));
        return "place";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/booking/{place}")
public String booking(@PathVariable String place, Model model) {

    model.addAttribute("place", place);

    return "booking";
}

// Go to Traveller Details Form
@GetMapping("/booking/traveller/{place}")
public String travellerDetails(@PathVariable String place, Model model) {
    model.addAttribute("place", place);
    return "traveller-details";
}

// Booking Confirm POST
@PostMapping("/booking-confirm")
public String bookingConfirm(@RequestParam String place,
                             @RequestParam String name,
                             @RequestParam String email,
                             @RequestParam String phone,
                             @RequestParam int people,
                             @RequestParam String date,
                             Model model) {

    // Generate simple tripId for demo
    String tripId = "TRV" + ((int)(Math.random()*9000)+1000);

    model.addAttribute("place", place);
    model.addAttribute("tripId", tripId);

    return "booking-success";
}

// Payment Page (Optional)
@GetMapping("/payment/{place}")
public String payment(@PathVariable String place, Model model) {
    model.addAttribute("place", place);
    return "payment";
}

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

}