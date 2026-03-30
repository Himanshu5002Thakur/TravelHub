package com.travel.controller;

import com.travel.entity.Booking;
import com.travel.entity.ContactMessage;
import com.travel.repository.BookingRepository;
import com.travel.repository.ContactRepository;
import com.travel.service.PackageService;
import com.travel.service.PaymentGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import com.travel.entity.User;
import com.travel.repository.UserRepository;
import com.travel.entity.Place;
import com.travel.repository.PlaceRepository;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z ]{2,50}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10,15}$");
    private static final Pattern UTR_PATTERN = Pattern.compile("^[A-Za-z0-9]{8,30}$");
    private static final Pattern RAZORPAY_PAYMENT_ID_PATTERN = Pattern.compile("^pay_[A-Za-z0-9]{8,}$");
    private static final Pattern RAZORPAY_ORDER_ID_PATTERN = Pattern.compile("^order_[A-Za-z0-9]{8,}$");
    private static final Pattern RAZORPAY_SIGNATURE_PATTERN = Pattern.compile("^[A-Fa-f0-9]{64}$");
    private static final int MAX_PLATES_PER_MEAL_PER_TRAVELLER = 6;

    @Autowired private PackageService packageService;
    @Autowired private ContactRepository contactRepo;
    @Autowired private BookingRepository bookingRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private PlaceRepository placeRepo;
    @Autowired private PaymentGatewayService paymentGatewayService;
    @Autowired private Environment environment;

    @Value("${app.payment.upi-id:yourupi@bank}")
    private String upiId;

    @Value("${app.payment.upi-name:Travel Hub}")
    private String upiName;

    @Value("${app.payment.qr-image:/images/travel-banner.jpg}")
    private String upiQrImage;

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
    String query = location != null ? location.trim() : "";
    if (!query.isEmpty()) {
        Place exactMatch = placeRepo.findByNameIgnoreCase(query).orElse(null);
        if (exactMatch != null) {
            String encodedPlace = UriUtils.encodePathSegment(exactMatch.getName(), StandardCharsets.UTF_8);
            return "redirect:/place/" + encodedPlace;
        }

        List<Place> placeMatches = placeRepo.searchPlaces(query);
        if (placeMatches.size() == 1) {
            String encodedPlace = UriUtils.encodePathSegment(placeMatches.get(0).getName(), StandardCharsets.UTF_8);
            return "redirect:/place/" + encodedPlace;
        }

        model.addAttribute("error", "No exact place found. Please enter a place name like Kashmir, Agra, London.");
    }

    List<String> countries = placeRepo.findAllDistinctCountries();
    Map<String, String> countryTaglines = new LinkedHashMap<>();
    for (String country : countries) {
        countryTaglines.put(country, getCountryTagline(country));
    }

    model.addAttribute("countries", countries);
    model.addAttribute("countryTaglines", countryTaglines);
    model.addAttribute("searchQuery", query);

    // 2. Pass the display name for the navbar (from your GlobalAdvice or here)
    if (auth != null && auth.isAuthenticated()) {
        userRepo.findByEmail(auth.getName()).ifPresent(user -> {
            model.addAttribute("displayName", user.getUsername());
        });
    }

    return "home";
}

private String getCountryTagline(String country) {
    String key = "app.home.country-tagline." + country.toLowerCase().replaceAll("[^a-z0-9]+", "-");
    String configured = environment.getProperty(key);
    if (configured != null && !configured.trim().isEmpty()) {
        return configured.trim();
    }
    return "Explore premium itineraries across " + country + " with curated stays and local highlights.";
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
    String itineraryText;
    if (placeObj != null && placeObj.getItinerary() != null && !placeObj.getItinerary().trim().isEmpty()) {
        itineraryText = placeObj.getItinerary().trim();
    } else {
        itineraryText = packageService.getPackageContent(placeName);
    }

    model.addAttribute("placeName", placeName);
    model.addAttribute("place", placeObj);
    model.addAttribute("itineraryText", itineraryText);
    model.addAttribute("country", placeObj != null ? placeObj.getCountryName() : null);
    return "place";
}

    @GetMapping("/contact")
    public String contact() { return "contact"; }

    @PostMapping("/contact")
    public String saveContact(@RequestParam String name, @RequestParam String email, 
                              @RequestParam String message, RedirectAttributes ra) {
        String cleanName = name != null ? name.trim() : "";
        String cleanEmail = email != null ? email.trim().toLowerCase() : "";
        String cleanMessage = message != null ? message.trim() : "";

        if (!NAME_PATTERN.matcher(cleanName).matches()) {
            ra.addFlashAttribute("error", "Name should contain only letters and spaces (2-50 characters).");
            ra.addFlashAttribute("contactName", cleanName);
            ra.addFlashAttribute("contactEmail", cleanEmail);
            ra.addFlashAttribute("contactMessage", cleanMessage);
            return "redirect:/contact";
        }

        if (!EMAIL_PATTERN.matcher(cleanEmail).matches()) {
            ra.addFlashAttribute("error", "Please enter a valid email address.");
            ra.addFlashAttribute("contactName", cleanName);
            ra.addFlashAttribute("contactEmail", cleanEmail);
            ra.addFlashAttribute("contactMessage", cleanMessage);
            return "redirect:/contact";
        }

        if (cleanMessage.length() < 10 || cleanMessage.length() > 600) {
            ra.addFlashAttribute("error", "Message must be between 10 and 600 characters.");
            ra.addFlashAttribute("contactName", cleanName);
            ra.addFlashAttribute("contactEmail", cleanEmail);
            ra.addFlashAttribute("contactMessage", cleanMessage);
            return "redirect:/contact";
        }

        ContactMessage msg = new ContactMessage();
        msg.setName(cleanName);
        msg.setEmail(cleanEmail);
        msg.setMessage(cleanMessage);
        contactRepo.save(msg);
        ra.addFlashAttribute("success", "Message saved successfully! We will contact you soon.");
        return "redirect:/contact";
    }

    @GetMapping("/booking/traveller/{place}")
    public String travellerDetails(@PathVariable String place, Model model) {
        Place placeObj = placeRepo.findByNameIgnoreCase(place).orElse(null);
        double basePrice = (placeObj != null && placeObj.getPrice() != null) ? placeObj.getPrice() : 24999.0;
        model.addAttribute("place", place);
        model.addAttribute("basePrice", basePrice);
        return "traveller-details";
    }

    @PostMapping("/booking-confirm")
public String confirm(@RequestParam String place, @RequestParam String name,
                      @RequestParam String email, @RequestParam String phone,
                      @RequestParam int people, @RequestParam String date,
                      @RequestParam(defaultValue = "PRIVATE") String tourType,
                      @RequestParam(defaultValue = "Bus") String transport,
                      @RequestParam(defaultValue = "NONE") String mealPlan,
                      @RequestParam(defaultValue = "MIXED") String foodPreference,
                      @RequestParam(defaultValue = "NONE") String foodCombo,
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

        String cleanTourType = tourType != null ? tourType.trim().toUpperCase() : "PRIVATE";
        if (!"PRIVATE".equals(cleanTourType) && !"GROUP".equals(cleanTourType)) {
            ra.addFlashAttribute("error", "Please choose a valid tour type.");
            ra.addAttribute("place", place);
            return "redirect:/booking/traveller/{place}";
        }

        if ("GROUP".equals(cleanTourType) && people < 2) {
            ra.addFlashAttribute("error", "Group tour requires minimum 2 travellers.");
            ra.addAttribute("place", place);
            return "redirect:/booking/traveller/{place}";
        }

        String cleanMealPlanRaw = mealPlan != null ? mealPlan.trim().toUpperCase() : "NONE";
        Set<String> selectedMealPlans = new LinkedHashSet<>();
        if (!cleanMealPlanRaw.isEmpty() && !"NONE".equals(cleanMealPlanRaw)) {
            for (String value : cleanMealPlanRaw.split(",")) {
                String plan = value.trim();
                if (!plan.isEmpty()) {
                    selectedMealPlans.add(plan);
                }
            }
        }

        Set<String> allowedMealPlans = Set.of("BREAKFAST", "LUNCH", "DINNER", "FULL_DAY");
        for (String plan : selectedMealPlans) {
            if (!allowedMealPlans.contains(plan)) {
                ra.addFlashAttribute("error", "Please choose a valid meal plan.");
                ra.addAttribute("place", place);
                return "redirect:/booking/traveller/{place}";
            }
        }

        if (selectedMealPlans.contains("FULL_DAY") && selectedMealPlans.size() > 1) {
            selectedMealPlans.clear();
            selectedMealPlans.add("FULL_DAY");
        }

        String cleanFoodComboRaw = foodCombo != null ? foodCombo.trim().toUpperCase() : "NONE";
        Map<String, Integer> selectedFoodCombos = new LinkedHashMap<>();
        Set<String> selectedFoodPreferences = new LinkedHashSet<>();
        if (!cleanFoodComboRaw.isEmpty() && !"NONE".equals(cleanFoodComboRaw)) {
            for (String value : cleanFoodComboRaw.split(",")) {
                String comboToken = value != null ? value.trim().toUpperCase() : "";
                if (comboToken.isEmpty()) {
                    continue;
                }

                String[] parts = comboToken.split(":");
                String comboCode;
                String comboPreference;
                int comboQty;

                if (parts.length == 1) {
                    comboCode = parts[0].trim();
                    comboPreference = "VEG";
                    comboQty = 1;
                } else if (parts.length == 3) {
                    comboCode = parts[0].trim();
                    comboPreference = parts[1].trim();
                    try {
                        comboQty = Integer.parseInt(parts[2].trim());
                    } catch (NumberFormatException ex) {
                        ra.addFlashAttribute("error", "Please choose a valid quantity for selected dish.");
                        ra.addAttribute("place", place);
                        return "redirect:/booking/traveller/{place}";
                    }
                } else {
                    ra.addFlashAttribute("error", "Please choose valid dish selections.");
                    ra.addAttribute("place", place);
                    return "redirect:/booking/traveller/{place}";
                }

                if (!"VEG".equals(comboPreference) && !"NON_VEG".equals(comboPreference)) {
                    ra.addFlashAttribute("error", "Please choose a valid food preference for each dish.");
                    ra.addAttribute("place", place);
                    return "redirect:/booking/traveller/{place}";
                }

                if (comboQty < 1 || comboQty > 9) {
                    ra.addFlashAttribute("error", "Dish quantity must be between 1 and 9 plates.");
                    ra.addAttribute("place", place);
                    return "redirect:/booking/traveller/{place}";
                }

                String comboKey = comboCode + ":" + comboPreference;
                int mergedQty = selectedFoodCombos.getOrDefault(comboKey, 0) + comboQty;
                if (mergedQty > 9) {
                    ra.addFlashAttribute("error", "Max 9 plates allowed per dish variant.");
                    ra.addAttribute("place", place);
                    return "redirect:/booking/traveller/{place}";
                }
                selectedFoodCombos.put(comboKey, mergedQty);
                selectedFoodPreferences.add(comboPreference);
            }
        }

        Set<String> allowedFoodCombos = Set.of(
                "BF_CLASSIC", "BF_NORTH", "BF_HEALTH",
                "LD_THALI", "LD_TANDOOR", "LD_CONTINENTAL",
                "DN_THALI", "DN_TANDOOR", "DN_CONTINENTAL",
                "FD_BALANCED", "FD_PREMIUM", "FD_KIDS"
        );

        for (String comboWithPreference : selectedFoodCombos.keySet()) {
            String combo = comboWithPreference.split(":")[0];
            if (!allowedFoodCombos.contains(combo)) {
                ra.addFlashAttribute("error", "Please choose a valid dish selection.");
                ra.addAttribute("place", place);
                return "redirect:/booking/traveller/{place}";
            }
        }

        String cleanFoodPreference;
        if (selectedMealPlans.isEmpty()) {
            cleanFoodPreference = "VEG";
            selectedFoodCombos.clear();
            selectedFoodPreferences.clear();
        } else if (selectedFoodCombos.isEmpty()) {
            ra.addFlashAttribute("error", "Please select at least one dish for your chosen meal plan.");
            ra.addAttribute("place", place);
            return "redirect:/booking/traveller/{place}";
        } else if (selectedFoodPreferences.size() > 1) {
            cleanFoodPreference = "MIXED";
        } else {
            cleanFoodPreference = selectedFoodPreferences.iterator().next();
        }

        Set<String> comboPool = new LinkedHashSet<>();
        if (selectedMealPlans.contains("FULL_DAY")) {
            comboPool.add("FD_BALANCED");
            comboPool.add("FD_PREMIUM");
            comboPool.add("FD_KIDS");
        } else {
            if (selectedMealPlans.contains("BREAKFAST")) {
                comboPool.add("BF_CLASSIC");
                comboPool.add("BF_NORTH");
                comboPool.add("BF_HEALTH");
            }
            if (selectedMealPlans.contains("LUNCH")) {
                comboPool.add("LD_THALI");
                comboPool.add("LD_TANDOOR");
                comboPool.add("LD_CONTINENTAL");
            }
            if (selectedMealPlans.contains("DINNER")) {
                comboPool.add("DN_THALI");
                comboPool.add("DN_TANDOOR");
                comboPool.add("DN_CONTINENTAL");
            }
        }

        for (String comboWithPreference : selectedFoodCombos.keySet()) {
            String combo = comboWithPreference.split(":")[0];
            if (!comboPool.contains(combo)) {
                ra.addFlashAttribute("error", "Selected dishes do not match selected meal plans.");
                ra.addAttribute("place", place);
                return "redirect:/booking/traveller/{place}";
            }
        }

        Map<String, Integer> mealWisePlateCounts = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> comboEntry : selectedFoodCombos.entrySet()) {
            String comboCode = comboEntry.getKey().split(":")[0];
            String mealKey = getMealKeyForComboCode(comboCode);
            if (mealKey.isEmpty()) {
                continue;
            }
            int updatedQty = mealWisePlateCounts.getOrDefault(mealKey, 0) + comboEntry.getValue();
            mealWisePlateCounts.put(mealKey, updatedQty);
            if (updatedQty > MAX_PLATES_PER_MEAL_PER_TRAVELLER) {
                String mealLabel = switch (mealKey) {
                    case "BREAKFAST" -> "Breakfast";
                    case "LUNCH" -> "Lunch";
                    case "DINNER" -> "Dinner";
                    case "FULL_DAY" -> "Full Day";
                    default -> "meal";
                };
                ra.addFlashAttribute("error", "Max " + MAX_PLATES_PER_MEAL_PER_TRAVELLER + " plates allowed for " + mealLabel + " per traveller.");
                ra.addAttribute("place", place);
                return "redirect:/booking/traveller/{place}";
            }
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
    Place placeObj = placeRepo.findByNameIgnoreCase(place).orElse(null);
    double basePrice = (placeObj != null && placeObj.getPrice() != null) ? placeObj.getPrice() : 24999.0;
    double transportExtra = transport.equals("Flight") ? 8500.0 : (transport.equals("Train") ? 2000.0 : 0.0);
    double mealPlanExtra;
    if (selectedMealPlans.contains("FULL_DAY")) {
        mealPlanExtra = 1800.0;
    } else {
        mealPlanExtra = 0.0;
        if (selectedMealPlans.contains("BREAKFAST")) {
            mealPlanExtra += 500.0;
        }
        if (selectedMealPlans.contains("LUNCH")) {
            mealPlanExtra += 550.0;
        }
        if (selectedMealPlans.contains("DINNER")) {
            mealPlanExtra += 650.0;
        }
    }
    double foodPreferenceExtra = 0.0;
    double foodComboExtra = 0.0;
    for (Map.Entry<String, Integer> comboEntry : selectedFoodCombos.entrySet()) {
        String comboCode = comboEntry.getKey().split(":")[0];
        int comboQty = comboEntry.getValue();
        double comboPrice = switch (comboCode) {
            case "BF_CLASSIC" -> 350.0;
            case "BF_NORTH" -> 390.0;
            case "BF_HEALTH" -> 430.0;
                case "LD_THALI" -> 680.0;
                case "LD_TANDOOR" -> 820.0;
                case "LD_CONTINENTAL" -> 740.0;
                case "DN_THALI" -> 620.0;
                case "DN_TANDOOR" -> 860.0;
                case "DN_CONTINENTAL" -> 760.0;
            case "FD_BALANCED" -> 1050.0;
            case "FD_PREMIUM" -> 1450.0;
            case "FD_KIDS" -> 910.0;
            default -> 0.0;
        };
        foodComboExtra += comboPrice * comboQty;
    }
    double foodExtra = mealPlanExtra + foodPreferenceExtra + foodComboExtra;
    double guideExtra = guide ? 1800.0 : 0.0;
    double subtotal = (basePrice + transportExtra + foodExtra + guideExtra) * people;
    double groupDiscountRate = "GROUP".equals(cleanTourType) ? 0.12 : 0.0;
    double finalPrice = subtotal * (1 - groupDiscountRate);

    // 2. Map data to Booking Entity
    Booking b = new Booking();
    b.setPlaceName(place);
    b.setTravellerName(cleanName);
    b.setEmail(cleanEmail);
    b.setPhone(cleanPhone);
    b.setTotalPeople(people);
    b.setTravelDate(travelDate);
    b.setTourType(cleanTourType);
    b.setTransportType(transport);
    b.setWantsFood(!selectedMealPlans.isEmpty());
    b.setMealPlan(selectedMealPlans.isEmpty() ? "NONE" : String.join(",", selectedMealPlans));
    b.setFoodPreference(cleanFoodPreference);
    if (selectedFoodCombos.isEmpty()) {
        b.setFoodCombo("NONE");
    } else {
        String normalizedFoodCombo = selectedFoodCombos.entrySet().stream()
                .map(entry -> entry.getKey() + ":" + entry.getValue())
                .collect(Collectors.joining(","));
        b.setFoodCombo(normalizedFoodCombo);
    }
    b.setWantsGuide(guide);
    b.setTotalPrice(finalPrice);
    b.setTripId("TRV-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());

    // 3. Link to Logged-in User
    if (auth != null && auth.isAuthenticated()) {
        userRepo.findByEmail(auth.getName()).ifPresent(user -> {
            b.setUser(user); 
        });
    }

    b.setPaymentStatus("PENDING");
    bookingRepo.save(b);
    return "redirect:/payment/" + b.getId();
}

@GetMapping("/payment/{bookingId}")
public String paymentPage(@PathVariable Long bookingId, Authentication auth, Model model, RedirectAttributes ra) {
    if (auth == null || !auth.isAuthenticated()) {
        ra.addFlashAttribute("error", "You are not allowed to access this payment.");
        return "redirect:/my-bookings";
    }

    Booking booking = bookingRepo.findByIdAndUserEmail(bookingId, auth.getName()).orElse(null);
    if (booking == null) {
        ra.addFlashAttribute("error", "Booking not found.");
        return "redirect:/my-bookings";
    }

    if ("PAID".equalsIgnoreCase(booking.getPaymentStatus())) {
        ra.addFlashAttribute("success", "This booking is already paid.");
        return "redirect:/my-bookings";
    }

    model.addAttribute("booking", booking);
    model.addAttribute("upiId", upiId);
    model.addAttribute("upiName", upiName);
    model.addAttribute("upiQrImage", upiQrImage);

    boolean razorpayEnabled = paymentGatewayService.isRazorpayEnabled();
    model.addAttribute("razorpayEnabled", razorpayEnabled);
    model.addAttribute("razorpayKeyId", paymentGatewayService.getRazorpayKeyId());

    if (razorpayEnabled) {
        try {
            long amountPaise = Math.max(100L, Math.round(booking.getTotalPrice() * 100));
            PaymentGatewayService.RazorpayOrder order = paymentGatewayService.createOrder(booking.getTripId(), amountPaise);
            booking.setPaymentOrderId(order.orderId());
            bookingRepo.save(booking);

            model.addAttribute("razorpayOrderId", order.orderId());
            model.addAttribute("razorpayAmount", order.amount());
            model.addAttribute("razorpayCurrency", order.currency());
        } catch (Exception ex) {
            model.addAttribute("gatewayError", "Card gateway is unavailable right now. Please try UPI QR or retry later.");
            model.addAttribute("razorpayEnabled", false);
        }
    }

    return "payment";
}

@PostMapping("/payment/{bookingId}")
public String processPayment(@PathVariable Long bookingId,
                             @RequestParam String paymentMethod,
                             @RequestParam(required = false) String upiReference,
                             @RequestParam(required = false) String razorpayPaymentId,
                             @RequestParam(required = false) String razorpayOrderId,
                             @RequestParam(required = false) String razorpaySignature,
                             Authentication auth,
                             RedirectAttributes ra) {
    if (auth == null || !auth.isAuthenticated()) {
        ra.addFlashAttribute("error", "You are not allowed to pay for this booking.");
        return "redirect:/my-bookings";
    }

    Booking booking = bookingRepo.findByIdAndUserEmail(bookingId, auth.getName()).orElse(null);
    if (booking == null) {
        ra.addFlashAttribute("error", "Booking not found.");
        return "redirect:/my-bookings";
    }

    if (!"CARD".equals(paymentMethod) && !"UPI_QR".equals(paymentMethod)) {
        ra.addFlashAttribute("error", "Please select a valid payment method.");
        return "redirect:/payment/" + bookingId;
    }

    if ("UPI_QR".equals(paymentMethod)) {
        String cleanedReference = upiReference != null ? upiReference.trim() : "";
        if (!UTR_PATTERN.matcher(cleanedReference).matches()) {
            ra.addFlashAttribute("error", "Please enter a valid UPI transaction reference (8-30 letters/numbers).");
            return "redirect:/payment/" + bookingId;
        }
        booking.setPaymentReference(cleanedReference);
    } else {
        if (!paymentGatewayService.isRazorpayEnabled()) {
            ra.addFlashAttribute("error", "Card payment gateway is not configured.");
            return "redirect:/payment/" + bookingId;
        }

        String paymentId = razorpayPaymentId != null ? razorpayPaymentId.trim() : "";
        String orderId = razorpayOrderId != null ? razorpayOrderId.trim() : "";
        String signature = razorpaySignature != null ? razorpaySignature.trim() : "";

        if (paymentId.isEmpty() || orderId.isEmpty() || signature.isEmpty()) {
            ra.addFlashAttribute("error", "Invalid card payment response. Please retry.");
            return "redirect:/payment/" + bookingId;
        }

        if (!RAZORPAY_PAYMENT_ID_PATTERN.matcher(paymentId).matches()
                || !RAZORPAY_ORDER_ID_PATTERN.matcher(orderId).matches()
                || !RAZORPAY_SIGNATURE_PATTERN.matcher(signature).matches()) {
            ra.addFlashAttribute("error", "Payment validation failed. Please retry card payment.");
            return "redirect:/payment/" + bookingId;
        }

        if (booking.getPaymentOrderId() == null || !booking.getPaymentOrderId().equals(orderId)) {
            ra.addFlashAttribute("error", "Order verification failed. Please retry payment.");
            return "redirect:/payment/" + bookingId;
        }

        boolean verified = paymentGatewayService.verifySignature(orderId, paymentId, signature);
        if (!verified) {
            ra.addFlashAttribute("error", "Payment signature verification failed.");
            return "redirect:/payment/" + bookingId;
        }

        booking.setPaymentReference(paymentId);
    }

    booking.setPaymentMethod(paymentMethod);
    booking.setPaymentStatus("PAID");
    bookingRepo.save(booking);

    return "redirect:/booking-success/" + booking.getId();
}

@GetMapping("/booking-success/{bookingId}")
public String bookingSuccess(@PathVariable Long bookingId, Authentication auth, Model model, RedirectAttributes ra) {
    if (auth == null || !auth.isAuthenticated()) {
        ra.addFlashAttribute("error", "Please login to view booking details.");
        return "redirect:/login";
    }

    Booking booking = bookingRepo.findByIdAndUserEmail(bookingId, auth.getName()).orElse(null);
    if (booking == null) {
        ra.addFlashAttribute("error", "Booking not found.");
        return "redirect:/my-bookings";
    }

    if (!"PAID".equalsIgnoreCase(booking.getPaymentStatus())) {
        ra.addFlashAttribute("error", "Please complete payment before viewing confirmation.");
        return "redirect:/payment/" + bookingId;
    }

    model.addAttribute("booking", booking);
    model.addAttribute("mealPlanDisplay", formatMealPlanDisplay(booking.getMealPlan()));
    model.addAttribute("foodComboDisplay", formatFoodComboDisplay(booking.getFoodCombo()));
    model.addAttribute("foodSummaryDisplay", formatFoodSummaryDisplay(booking));
    return "booking-success";
}

private String formatMealPlanDisplay(String mealPlanCsv) {
    if (mealPlanCsv == null || mealPlanCsv.isBlank() || "NONE".equalsIgnoreCase(mealPlanCsv.trim())) {
        return "No Meal Plan";
    }

    StringBuilder output = new StringBuilder();
    for (String part : mealPlanCsv.split(",")) {
        String plan = part.trim().toUpperCase();
        if (plan.isEmpty()) {
            continue;
        }
        String label = switch (plan) {
            case "BREAKFAST" -> "Breakfast";
            case "LUNCH" -> "Lunch";
            case "DINNER" -> "Dinner";
            case "FULL_DAY" -> "Full Day";
            default -> plan;
        };
        if (!output.isEmpty()) {
            output.append(" + ");
        }
        output.append(label);
    }
    return output.isEmpty() ? "No Meal Plan" : output.toString();
}

private String formatFoodComboDisplay(String foodComboCsv) {
    if (foodComboCsv == null || foodComboCsv.isBlank() || "NONE".equalsIgnoreCase(foodComboCsv.trim())) {
        return "No Dish";
    }

    StringBuilder output = new StringBuilder();
    for (String part : foodComboCsv.split(",")) {
        String token = part.trim().toUpperCase();
        if (token.isEmpty()) {
            continue;
        }

        String[] pieces = token.split(":");
        String combo = pieces[0].trim();
        String preference = pieces.length >= 2 ? pieces[1].trim() : "VEG";
        int qty = 1;
        if (pieces.length >= 3) {
            try {
                qty = Integer.parseInt(pieces[2].trim());
            } catch (NumberFormatException ex) {
                qty = 1;
            }
        }
        if (qty < 1) {
            qty = 1;
        }

        String label = switch (combo) {
            case "BF_CLASSIC" -> "Classic Idli Plate";
            case "BF_NORTH" -> "Paratha and Curd";
            case "BF_HEALTH" -> "Healthy Bowl";
            case "LD_THALI" -> "Regional Thali";
            case "LD_TANDOOR" -> "Tandoor Grill Platter";
            case "LD_CONTINENTAL" -> "Continental Bowl";
            case "DN_THALI" -> "Soup and Salad Platter";
            case "DN_TANDOOR" -> "Signature Dinner Biryani";
            case "DN_CONTINENTAL" -> "Dinner with Dessert";
            case "FD_BALANCED" -> "Balanced Full Day";
            case "FD_PREMIUM" -> "Premium Full Day";
            case "FD_KIDS" -> "Kids Friendly Full Day";
            default -> combo;
        };

        String prefLabel = "NON_VEG".equals(preference) ? "Non-Veg" : "Veg";
        if (!output.isEmpty()) {
            output.append(", ");
        }
        output.append(qty).append("x ").append(prefLabel).append(" ").append(label);
    }
    return output.isEmpty() ? "No Dish" : output.toString();
}

private String formatFoodSummaryDisplay(Booking booking) {
    if (booking == null || booking.getMealPlan() == null || "NONE".equalsIgnoreCase(booking.getMealPlan().trim())) {
        return "No Meal Plan";
    }
    String mealLabel = formatMealPlanDisplay(booking.getMealPlan());
    String comboLabel = formatFoodComboDisplay(booking.getFoodCombo());
    return mealLabel + " / " + comboLabel;
}

private String getMealKeyForComboCode(String comboCode) {
    if (comboCode == null) {
        return "";
    }
    String normalized = comboCode.trim().toUpperCase();
    if (normalized.startsWith("BF_")) {
        return "BREAKFAST";
    }
    if (normalized.startsWith("LD_")) {
        return "LUNCH";
    }
    if (normalized.startsWith("DN_")) {
        return "DINNER";
    }
    if (normalized.startsWith("FD_")) {
        return "FULL_DAY";
    }
    return "";
}

   // Show the "My Bookings" page
@GetMapping("/my-bookings")
public String myBookings(Authentication auth, Model model) {
    if (auth != null) {
        String email = auth.getName(); // Our security uses email as the username
        User user = userRepo.findByEmail(email).orElse(null);
        if (user != null) {
            List<Booking> bookings = bookingRepo.findByUser(user);
            Map<Long, String> foodSummaryByBookingId = new LinkedHashMap<>();
            for (Booking booking : bookings) {
                if (booking.getId() != null) {
                    foodSummaryByBookingId.put(booking.getId(), formatFoodSummaryDisplay(booking));
                }
            }
            model.addAttribute("bookings", bookings);
            model.addAttribute("foodSummaryByBookingId", foodSummaryByBookingId);
        }
    }
    return "my-bookings";
}

@GetMapping("/report")
public String userReport(Authentication auth, Model model) {
    if (auth == null || !auth.isAuthenticated()) {
        return "redirect:/login";
    }

    User user = userRepo.findByEmail(auth.getName()).orElse(null);
    if (user == null) {
        return "redirect:/login";
    }

    List<Booking> allBookings = bookingRepo.findByUser(user);

    long totalBookings = allBookings.size();
    long paidBookings = allBookings.stream().filter(b -> "PAID".equalsIgnoreCase(b.getPaymentStatus())).count();
    long pendingBookings = totalBookings - paidBookings;
    double totalRevenue = allBookings.stream()
        .filter(b -> "PAID".equalsIgnoreCase(b.getPaymentStatus()))
        .mapToDouble(Booking::getTotalPrice)
        .sum();

    Map<String, Long> tourTypeCounts = allBookings.stream()
        .collect(Collectors.groupingBy(
            b -> b.getTourType() != null ? b.getTourType() : "PRIVATE",
            LinkedHashMap::new,
            Collectors.counting()));

    Map<String, Long> mealPlanCounts = allBookings.stream()
        .collect(Collectors.groupingBy(
            b -> b.getMealPlan() != null ? b.getMealPlan() : "NONE",
            LinkedHashMap::new,
            Collectors.counting()));

    Map<String, Long> comboCounts = allBookings.stream()
        .collect(Collectors.groupingBy(
            b -> b.getFoodCombo() != null ? b.getFoodCombo() : "NONE",
            LinkedHashMap::new,
            Collectors.counting()));

    List<Booking> recentBookings = allBookings.stream()
        .sorted(Comparator.comparing(Booking::getId).reversed())
        .limit(12)
        .toList();

    model.addAttribute("totalBookings", totalBookings);
    model.addAttribute("paidBookings", paidBookings);
    model.addAttribute("pendingBookings", pendingBookings);
    model.addAttribute("totalRevenue", Math.round(totalRevenue));
    model.addAttribute("tourTypeCounts", tourTypeCounts);
    model.addAttribute("mealPlanCounts", mealPlanCounts);
    model.addAttribute("comboCounts", comboCounts);
    model.addAttribute("recentBookings", recentBookings);

    return "admin-report";
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