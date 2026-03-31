package com.travel.config;

import com.travel.entity.Place;
import com.travel.repository.PlaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Autowired
    private PlaceRepository placeRepo;

    @Bean
    public ApplicationRunner init() {
        return args -> {
            // Check if places already exist to avoid duplicates
            if (placeRepo.count() > 0) {
                System.out.println("✓ Places data already loaded. Skipping initialization.");
                return;
            }

            System.out.println("Loading initial places data...");

            // INDIA
            placeRepo.save(createPlace("Kashmir", "India", "Beautiful hill destination", 45000.0, 5.0, 
                "Day 1: Houseboat\nDay 2: Gulmarg\nDay 3: Pahalgam"));
            placeRepo.save(createPlace("Shimla", "India", "Snow and mountains", 35000.0, 4.6,
                "Day 1: Mall Road\nDay 2: Kufri\nDay 3: Jakhu Temple"));
            placeRepo.save(createPlace("Agra", "India", "City of Taj Mahal", 30000.0, 4.8,
                "Day 1: Taj Mahal\nDay 2: Agra Fort"));
            placeRepo.save(createPlace("Mumbai", "India", "Famous for beaches and nightlife", 25000.0, 4.4,
                "Day 1: Gateway of India\nDay 2: Marine Drive\nDay 3: Elephanta Caves"));
            placeRepo.save(createPlace("Manali", "India", "Adventure and snow destination", 30000.0, 4.7,
                "Day 1: Solang Valley\nDay 2: Rohtang Pass\nDay 3: River Rafting"));
            placeRepo.save(createPlace("Goa", "India", "Famous for beaches and nightlife", 28000.0, 4.5,
                "Day 1: Beaches\nDay 2: Water Sports\nDay 3: Nightlife"));
            placeRepo.save(createPlace("Jaipur", "India", "Royal heritage city of Rajasthan", 22000.0, 4.4,
                "Day 1: Amber Fort\nDay 2: Hawa Mahal\nDay 3: City Palace"));

            // US
            placeRepo.save(createPlace("New York", "US", "Famous US city life", 52000.0, 4.9,
                "Day 1: Times Square\nDay 2: Statue of Liberty"));
            placeRepo.save(createPlace("Los Angeles", "US", "Famous US city life", 90000.0, 4.7,
                "Day 1: Hollywood\nDay 2: Santa Monica"));
            placeRepo.save(createPlace("Miami", "US", "Beach city with vibrant life", 75000.0, 4.5,
                "Day 1: South Beach\nDay 2: Everglades\nDay 3: Little Havana"));
            placeRepo.save(createPlace("Las Vegas", "US", "Famous for casinos and nightlife", 88000.0, 4.6,
                "Day 1: Casinos\nDay 2: Shows\nDay 3: Strip Tour"));

            // CANADA
            placeRepo.save(createPlace("Toronto", "Canada", "Peaceful Canadian cities", 34000.0, 4.6,
                "Day 1: CN Tower\nDay 2: City Tour"));
            placeRepo.save(createPlace("Vancouver", "Canada", "Peaceful Canadian cities", 82000.0, 4.8,
                "Day 1: Stanley Park\nDay 2: Bridge"));
            placeRepo.save(createPlace("Niagara Falls", "Canada", "World famous waterfalls destination", 40000.0, 4.9,
                "Day 1: Falls View\nDay 2: Boat Ride\nDay 3: Tower Visit"));
            placeRepo.save(createPlace("Montreal", "Canada", "French culture city of Canada", 78000.0, 4.5,
                "Day 1: Old City\nDay 2: Basilica\nDay 3: Mount Royal"));

            // UK
            placeRepo.save(createPlace("London", "UK", "Historic UK destinations", 48000.0, 4.8,
                "Day 1: Big Ben\nDay 2: London Eye"));
            placeRepo.save(createPlace("Edinburgh", "UK", "Historic UK destinations", 70000.0, 4.7,
                "Day 1: Edinburgh Castle\nDay 2: Royal Mile\nDay 3: Arthur's Seat"));
            placeRepo.save(createPlace("Liverpool", "UK", "City of Beatles and music history", 60000.0, 4.3,
                "Day 1: Beatles Tour\nDay 2: Dock Visit\nDay 3: Museum"));
            placeRepo.save(createPlace("Birmingham", "UK", "Industrial city with modern vibe", 62000.0, 4.2,
                "Day 1: City Center\nDay 2: Museum\nDay 3: Canal Walk"));

            System.out.println("✓ All 16 places loaded successfully!");
        };
    }

    private Place createPlace(String name, String country, String description, Double price, Double rating, String itinerary) {
        Place place = new Place();
        place.setName(name);
        place.setCountryName(country);
        place.setDescription(description);
        place.setPrice(price);
        place.setRating(rating);
        place.setItinerary(itinerary);
        return place;
    }
}
