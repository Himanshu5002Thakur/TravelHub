package com.travel.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "places")
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String countryName;
    private Double price;  // Added
    private Double rating; // Added
    
    @Column(columnDefinition = "TEXT")
    private String description;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCountryName() { return countryName; }
    public void setCountryName(String countryName) { this.countryName = countryName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    @Column(columnDefinition = "TEXT")
private String itinerary;

// YOU MUST ADD THIS GETTER METHOD
public String getItinerary() {
    return itinerary;
}

public void setItinerary(String itinerary) {
    this.itinerary = itinerary;
}
}