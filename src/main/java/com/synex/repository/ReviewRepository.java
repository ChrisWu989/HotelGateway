package com.synex.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.synex.entity.Hotel;
import com.synex.entity.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    
    // Find reviews by hotel
    List<Review> findByHotel(Hotel hotel);
    
    // Find reviews by hotel ID
    @Query("SELECT r FROM Review r WHERE r.hotel.hotelId = :hotelId")
    List<Review> findByHotelId(@Param("hotelId") int hotelId);
    
    // Find reviews by user ID
    List<Review> findByUserId(String userId);
    
    // Find reviews by rating
    List<Review> findByRating(int rating);
    
    // Get average rating for a hotel
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.hotel.hotelId = :hotelId")
    Double getAverageRatingByHotelId(@Param("hotelId") int hotelId);
}