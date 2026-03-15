package com.synex.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.synex.entity.Hotel;
import com.synex.service.HotelService;

@RestController
@RequestMapping("/api/hotels")
@CrossOrigin(origins = "*")
public class HotelController {
    
    @Autowired
    private HotelService hotelService;
    
    // Get all hotels
    @GetMapping
    public ResponseEntity<List<Hotel>> getAllHotels() {
        List<Hotel> hotels = hotelService.getAllHotels();
        return ResponseEntity.ok(hotels);
    }
    
    // Get hotel by ID
    @GetMapping("/{id}")
    public ResponseEntity<Hotel> getHotelById(@PathVariable int id) {
        Hotel hotel = hotelService.getHotelById(id);
        if (hotel != null) {
            return ResponseEntity.ok(hotel);
        }
        return ResponseEntity.notFound().build();
    }
    
    // Search hotels with optional filters
    @GetMapping("/search")
    public ResponseEntity<List<Hotel>> searchHotels(
            @RequestParam(required = false) String searchLocation,
            @RequestParam(required = false, defaultValue = "all") String searchType) {
        
        if (searchLocation == null || searchLocation.trim().isEmpty()) {
            return ResponseEntity.ok(hotelService.getAllHotels());
        }
        
        List<Hotel> hotels = hotelService.searchHotelsByType(searchLocation.trim(), searchType);
        return ResponseEntity.ok(hotels);
    }
    
    // Filter
    @GetMapping("/filter")
    public ResponseEntity<List<Hotel>> filterHotels(
            @RequestParam(required = false) String searchLocation,
            @RequestParam(required = false) Integer minStarRating,
            @RequestParam(required = false) Integer maxStarRating,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) List<String> amenities) {
        
        List<Hotel> hotels = hotelService.filterHotels(
            searchLocation, 
            minStarRating, 
            maxStarRating, 
            minPrice, 
            maxPrice, 
            amenities
        );
        
        return ResponseEntity.ok(hotels);
    }
            
    // Create new hotel
    @PostMapping
    public ResponseEntity<Hotel> createHotel(@RequestBody Hotel hotel) {
        Hotel savedHotel = hotelService.saveHotel(hotel);
        return ResponseEntity.ok(savedHotel);
    }
    
    // Update hotel
    @PutMapping("/{id}")
    public ResponseEntity<Hotel> updateHotel(@PathVariable int id, @RequestBody Hotel hotel) {
        hotel.setHotelId(id);
        Hotel updatedHotel = hotelService.saveHotel(hotel);
        return ResponseEntity.ok(updatedHotel);
    }
    
    // Delete hotel
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHotel(@PathVariable int id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.noContent().build();
    }
    
    // Increment times booked
    @PutMapping("/{id}/increment-bookings")
    public ResponseEntity<Void> incrementBookings(@PathVariable int id) {
        hotelService.incrementTimesBooked(id);
        return ResponseEntity.ok().build();
    }
}
