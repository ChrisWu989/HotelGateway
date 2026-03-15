package com.synex.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.synex.entity.Amenities;
import com.synex.entity.HotelRoom;
import com.synex.repository.HotelRoomRepository;

@Service
@Transactional
public class HotelRoomService {
    
    @Autowired
    private HotelRoomRepository hotelRoomRepository;
    
    // Get all hotel rooms
    public List<HotelRoom> getAllHotelRooms() {
        List<HotelRoom> rooms = hotelRoomRepository.findAll();
        rooms.forEach(this::populateRoomDetails);
        return rooms;
    }
    
    // Get hotel room by ID
    public HotelRoom getHotelRoomById(int hotelRoomId) {
        Optional<HotelRoom> room = hotelRoomRepository.findById(hotelRoomId);
        if (room.isPresent()) {
            HotelRoom hr = room.get();
            populateRoomDetails(hr);
            return hr;
        }
        return null;
    }
    
    // Get hotel rooms by hotel ID
    public List<HotelRoom> getHotelRoomsByHotelId(int hotelId) {
        List<HotelRoom> rooms = hotelRoomRepository.findByHotelId(hotelId);
        rooms.forEach(this::populateRoomDetails);
        return rooms;
    }
    
    // Get hotel rooms by minimum available rooms
    public List<HotelRoom> getHotelRoomsByMinimumRooms(int minRooms) {
        List<HotelRoom> rooms = hotelRoomRepository.findByNoRoomsGreaterThanEqual(minRooms);
        rooms.forEach(this::populateRoomDetails);
        return rooms;
    }
    
    // Get hotel rooms by price range
    public List<HotelRoom> getHotelRoomsByPriceRange(float minPrice, float maxPrice) {
        List<HotelRoom> rooms = hotelRoomRepository.findByPriceBetween(minPrice, maxPrice);
        rooms.forEach(this::populateRoomDetails);
        return rooms;
    }
    
    // Save or update hotel room
    public HotelRoom saveHotelRoom(HotelRoom hotelRoom) {
        return hotelRoomRepository.save(hotelRoom);
    }
    
    // Delete hotel room
    public void deleteHotelRoom(int hotelRoomId) {
        hotelRoomRepository.deleteById(hotelRoomId);
    }
    
    // Update room availability after booking
    public boolean updateRoomAvailability(int hotelRoomId, int roomsBooked) {
        Optional<HotelRoom> roomOpt = hotelRoomRepository.findById(hotelRoomId);
        if (roomOpt.isPresent()) {
            HotelRoom room = roomOpt.get();
            if (room.getNoRooms() >= roomsBooked) {
                room.setNoRooms(room.getNoRooms() - roomsBooked);
                hotelRoomRepository.save(room);
                return true;
            }
        }
        return false;
    }
    
    // Helper method to populate room details
    private void populateRoomDetails(HotelRoom room) {
        if (room.getType() != null) {
            room.setRoomType(room.getType().getName());
        }
        
        Set<String> amenityNames = new HashSet<>();
        if (room.getAmenities() != null) {
            for (Amenities amenity : room.getAmenities()) {
                amenityNames.add(amenity.getName());
            }
        }
        room.setHotelRoomAmenityNames(amenityNames);
    }
}
