package com.synex.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.synex.entity.Review;
import com.synex.service.ReviewService;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {
    
    @Autowired
    private ReviewService reviewService;
    
    // Get all reviews
    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        List<Review> reviews = reviewService.getAllReviews();
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }
    
    // Get review by ID
    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable("id") int reviewId) {
        Review review = reviewService.getReviewById(reviewId);
        if (review != null) {
            return new ResponseEntity<>(review, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    
    // Get reviews by hotel ID
    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<Review>> getReviewsByHotelId(@PathVariable("hotelId") int hotelId) {
        List<Review> reviews = reviewService.getReviewsByHotelId(hotelId);
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }
    
    // Get reviews by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Review>> getReviewsByUserId(@PathVariable("userId") String userId) {
        List<Review> reviews = reviewService.getReviewsByUserId(userId);
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }
    
    // Get average rating for a hotel
    @GetMapping("/hotel/{hotelId}/average-rating")
    public ResponseEntity<Double> getAverageRatingForHotel(@PathVariable("hotelId") int hotelId) {
        Double avgRating = reviewService.getAverageRatingForHotel(hotelId);
        return new ResponseEntity<>(avgRating, HttpStatus.OK);
    }
    
    // Create new review
    @PostMapping
    public ResponseEntity<Review> createReview(
            @RequestParam("hotelId") int hotelId,
            @RequestParam("userId") String userId,
            @RequestParam("rating") int rating,
            @RequestParam("reviewDesc") String reviewDesc) {
        Review review = reviewService.createReview(hotelId, userId, rating, reviewDesc);
        if (review != null) {
            return new ResponseEntity<>(review, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    
    // Update review
    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(
            @PathVariable("id") int reviewId,
            @RequestBody Review review) {
        Review existingReview = reviewService.getReviewById(reviewId);
        if (existingReview != null) {
            review.setId(reviewId);
            Review updatedReview = reviewService.updateReview(review);
            return new ResponseEntity<>(updatedReview, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    
    // Delete review
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable("id") int reviewId) {
        Review review = reviewService.getReviewById(reviewId);
        if (review != null) {
            reviewService.deleteReview(reviewId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
