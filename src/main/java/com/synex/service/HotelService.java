package com.synex.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.synex.entity.Hotel;
import com.synex.repository.HotelRepository;

@Service
@Transactional
public class HotelService {
    
    @Autowired
    private HotelRepository hotelRepository;
    
    // Get all hotels
    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }
    
    // Get hotel by ID
    public Hotel getHotelById(int id) {
        return hotelRepository.findById(id).orElse(null);
    }
    
    // Improved hotel search (hotel, state, city, state, address)
    public List<Hotel> searchHotelsByType(String searchTerm, String searchType) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllHotels();
        }
        
        switch (searchType.toLowerCase()) {
            case "hotel":
                return hotelRepository.searchByHotelName(searchTerm);
            case "city":
                return hotelRepository.searchByCity(searchTerm);
            case "state":
                return hotelRepository.searchByStateExact(searchTerm);
            case "address":
                return hotelRepository.searchByAddress(searchTerm);
            case "all":
            default:
                return hotelRepository.searchByLocation(searchTerm);
        }
    }
    
    // Hotel Filtering
    public List<Hotel> filterHotels(
            String searchLocation,
            Integer minStarRating,
            Integer maxStarRating,
            Double minPrice,
            Double maxPrice,
            List<String> amenities) {
        
        // Start with basic filters
        List<Hotel> hotels = hotelRepository.filterHotels(
            searchLocation,
            minStarRating,
            maxStarRating,
            minPrice,
            maxPrice
        );
        
        // Apply amenities filter if specified
        if (amenities != null && !amenities.isEmpty()) {
            // Convert amenities to uppercase for case-insensitive matching
            List<String> upperAmenities = amenities.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());
            
            hotels = hotels.stream()
                .filter(hotel -> hotelHasAmenities(hotel, upperAmenities))
                .collect(Collectors.toList());
        }
        
        return hotels;
    }
    
    // Check if hotel has all specified amenities
    private boolean hotelHasAmenities(Hotel hotel, List<String> requiredAmenities) {
        if (hotel.getAmenities() == null || hotel.getAmenities().isEmpty()) {
            return false;
        }
        
        List<String> hotelAmenityNames = hotel.getAmenities().stream()
            .map(amenity -> amenity.getName().toUpperCase())
            .collect(Collectors.toList());
        
        // Hotel must have all required amenities
        return hotelAmenityNames.containsAll(requiredAmenities);
    }
    
    // Save or update hotel
    public Hotel saveHotel(Hotel hotel) {
        return hotelRepository.save(hotel);
    }
    
    // Delete hotel
    public void deleteHotel(int id) {
        hotelRepository.deleteById(id);
    }
    
    // Increment times booked
    public void incrementTimesBooked(int hotelId) {
        Hotel hotel = getHotelById(hotelId);
        if (hotel != null) {
            int currentCount = hotel.getTimesBooked() != null ? hotel.getTimesBooked() : 0;
            hotel.setTimesBooked(currentCount + 1);
            saveHotel(hotel);
        }
    }
    
    // Get hotels by city
    public List<Hotel> getHotelsByCity(String city) {
        return hotelRepository.findByCity(city);
    }
    
    // Get hotels by state
    public List<Hotel> getHotelsByState(String state) {
        return hotelRepository.findByState(state);
    }
    
    // Get hotels by star rating
    public List<Hotel> getHotelsByStarRating(int rating) {
        return hotelRepository.findByStarRating(rating);
    }
    
    //Get hotels by price range
    public List<Hotel> getHotelsByPriceRange(double minPrice, double maxPrice) {
        return hotelRepository.findByPriceRange(minPrice, maxPrice);
    }
}
