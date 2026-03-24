package com.travel.repository;

import com.travel.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import java.util.List;

public interface PlaceRepository extends JpaRepository<Place, Long> {
    
    // Finds all places for a specific country (e.g., all 4 places in India)
    List<Place> findByCountryName(String countryName);

    // FIX FOR DUPLICATES: Gets only UNIQUE country names for the Home page
    @Query("SELECT DISTINCT p.countryName FROM Place p")
    List<String> findAllDistinctCountries();

    // Search fix
    @Query("SELECT DISTINCT p.countryName FROM Place p WHERE p.countryName LIKE %:location%")
    List<String> findCountriesBySearch(String location);

    Optional<Place> findByNameIgnoreCase(String name);
}