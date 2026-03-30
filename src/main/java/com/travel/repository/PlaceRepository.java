package com.travel.repository;

import com.travel.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT p FROM Place p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.countryName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Place> searchPlaces(@Param("query") String query);

    Optional<Place> findByNameIgnoreCase(String name);
}