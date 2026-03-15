package com.synex.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.synex.entity.Hotel;
import com.synex.entity.Review;
import com.synex.repository.HotelRepository;
import com.synex.repository.ReviewRepository;

@Service
@Transactional
public class ReviewService {
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private HotelRepository hotelRepository;
    
    // Get all reviews
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }
    
    // Get review by ID
    public Review getReviewById(int reviewId) {
        Optional<Review> review = reviewRepository.findById(reviewId);
        return review.orElse(null);
    }
    
    // Get reviews by hotel ID
    public List<Review> getReviewsByHotelId(int hotelId) {
        return reviewRepository.findByHotelId(hotelId);
    }
    
    // Get reviews by user ID
    public List<Review> getReviewsByUserId(String userId) {
        return reviewRepository.findByUserId(userId);
    }
    
    // Get average rating for a hotel
    public Double getAverageRatingForHotel(int hotelId) {
        Double avgRating = reviewRepository.getAverageRatingByHotelId(hotelId);
        return avgRating != null ? avgRating : 0.0;
    }
    
    // Create a new review
    public Review createReview(int hotelId, String userId, int rating, String reviewDesc) {
        Optional<Hotel> hotelOpt = hotelRepository.findById(hotelId);
        if (hotelOpt.isPresent()) {
            Review review = new Review();
            review.setHotel(hotelOpt.get());
            review.setUserId(userId);
            review.setRating(rating);
            review.setReviewDesc(reviewDesc);
            review.setReviewDate(LocalDate.now());
            return reviewRepository.save(review);
        }
        return null;
    }
    
    // Update review
    public Review updateReview(Review review) {
        return reviewRepository.save(review);
    }
    
    // Delete review
    public void deleteReview(int reviewId) {
        reviewRepository.deleteById(reviewId);
    }
}
