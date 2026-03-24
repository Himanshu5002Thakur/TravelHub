package com.travel.repository;

import com.travel.entity.Booking;
import com.travel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Finds all bookings for the logged-in user
    List<Booking> findByUser(User user);
}