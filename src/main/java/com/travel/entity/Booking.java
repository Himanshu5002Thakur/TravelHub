package com.travel.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String placeName;
    private String travellerName;
    private String email;
    private String phone;
    private int totalPeople;
    private LocalDate travelDate;
    private String tourType; // PRIVATE, GROUP
    private String transportType; // Flight, Bus, Train
    private boolean wantsFood;
    private String mealPlan; // NONE, BREAKFAST, LUNCH, DINNER, FULL_DAY
    private String foodPreference; // VEG, NON_VEG
    private String foodCombo; // NONE, LOCAL_THALI, STREET_DELIGHT, PREMIUM_FUSION
    private boolean wantsGuide;
    private double totalPrice;
    private String tripId;
    private String paymentStatus; // PENDING, PAID
    private String paymentMethod; // CARD, UPI_QR
    private String paymentReference; // UPI UTR / gateway payment id
    private String paymentOrderId; // gateway order id (e.g., Razorpay order)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPlaceName() { return placeName; }
    public void setPlaceName(String placeName) { this.placeName = placeName; }
    public String getTravellerName() { return travellerName; }
    public void setTravellerName(String travellerName) { this.travellerName = travellerName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public int getTotalPeople() { return totalPeople; }
    public void setTotalPeople(int totalPeople) { this.totalPeople = totalPeople; }
    public LocalDate getTravelDate() { return travelDate; }
    public void setTravelDate(LocalDate travelDate) { this.travelDate = travelDate; }
    public String getTourType() { return tourType; }
    public void setTourType(String tourType) { this.tourType = tourType; }
    public String getTransportType() { return transportType; }
    public void setTransportType(String transportType) { this.transportType = transportType; }
    public boolean isWantsFood() { return wantsFood; }
    public void setWantsFood(boolean wantsFood) { this.wantsFood = wantsFood; }
    public String getMealPlan() { return mealPlan; }
    public void setMealPlan(String mealPlan) { this.mealPlan = mealPlan; }
    public String getFoodPreference() { return foodPreference; }
    public void setFoodPreference(String foodPreference) { this.foodPreference = foodPreference; }
    public String getFoodCombo() { return foodCombo; }
    public void setFoodCombo(String foodCombo) { this.foodCombo = foodCombo; }
    public boolean isWantsGuide() { return wantsGuide; }
    public void setWantsGuide(boolean wantsGuide) { this.wantsGuide = wantsGuide; }
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
    public String getPaymentOrderId() { return paymentOrderId; }
    public void setPaymentOrderId(String paymentOrderId) { this.paymentOrderId = paymentOrderId; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}



