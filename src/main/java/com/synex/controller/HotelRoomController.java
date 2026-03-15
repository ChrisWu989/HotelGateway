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

import com.synex.entity.HotelRoom;
import com.synex.service.HotelRoomService;

@RestController
@RequestMapping("/api/hotel-rooms")
@CrossOrigin(origins = "*")
public class HotelRoomController {
    
    @Autowired
    private HotelRoomService hotelRoomService;
    
    // Get all hotel rooms
    @GetMapping
    public ResponseEntity<List<HotelRoom>> getAllHotelRooms() {
        List<HotelRoom> rooms = hotelRoomService.getAllHotelRooms();
        return new ResponseEntity<>(rooms, HttpStatus.OK);
    }
    
    // Get hotel room by ID
    @GetMapping("/{id}")
    public ResponseEntity<HotelRoom> getHotelRoomById(@PathVariable("id") int hotelRoomId) {
        HotelRoom room = hotelRoomService.getHotelRoomById(hotelRoomId);
        if (room != null) {
            return new ResponseEntity<>(room, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    
    // Get hotel rooms by hotel ID
    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<List<HotelRoom>> getHotelRoomsByHotelId(@PathVariable("hotelId") int hotelId) {
        List<HotelRoom> rooms = hotelRoomService.getHotelRoomsByHotelId(hotelId);
        return new ResponseEntity<>(rooms, HttpStatus.OK);
    }
    
    // Get hotel rooms by minimum available rooms
    @GetMapping("/available")
    public ResponseEntity<List<HotelRoom>> getHotelRoomsByMinimumRooms(@RequestParam("minRooms") int minRooms) {
        List<HotelRoom> rooms = hotelRoomService.getHotelRoomsByMinimumRooms(minRooms);
        return new ResponseEntity<>(rooms, HttpStatus.OK);
    }
    
    // Get hotel rooms by price range
    @GetMapping("/price-range")
    public ResponseEntity<List<HotelRoom>> getHotelRoomsByPriceRange(
            @RequestParam("minPrice") float minPrice,
            @RequestParam("maxPrice") float maxPrice) {
        List<HotelRoom> rooms = hotelRoomService.getHotelRoomsByPriceRange(minPrice, maxPrice);
        return new ResponseEntity<>(rooms, HttpStatus.OK);
    }
    
    // Create new hotel room
    @PostMapping
    public ResponseEntity<HotelRoom> createHotelRoom(@RequestBody HotelRoom hotelRoom) {
        HotelRoom savedRoom = hotelRoomService.saveHotelRoom(hotelRoom);
        return new ResponseEntity<>(savedRoom, HttpStatus.CREATED);
    }
    
    // Update hotel room
    @PutMapping("/{id}")
    public ResponseEntity<HotelRoom> updateHotelRoom(
            @PathVariable("id") int hotelRoomId,
            @RequestBody HotelRoom hotelRoom) {
        HotelRoom existingRoom = hotelRoomService.getHotelRoomById(hotelRoomId);
        if (existingRoom != null) {
            hotelRoom.setHotelRoomId(hotelRoomId);
            HotelRoom updatedRoom = hotelRoomService.saveHotelRoom(hotelRoom);
            return new ResponseEntity<>(updatedRoom, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    
    // Delete hotel room
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHotelRoom(@PathVariable("id") int hotelRoomId) {
        HotelRoom room = hotelRoomService.getHotelRoomById(hotelRoomId);
        if (room != null) {
            hotelRoomService.deleteHotelRoom(hotelRoomId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    
    // Update room availability
    @PutMapping("/{id}/update-availability")
    public ResponseEntity<String> updateRoomAvailability(
            @PathVariable("id") int hotelRoomId,
            @RequestParam("roomsBooked") int roomsBooked) {
        boolean updated = hotelRoomService.updateRoomAvailability(hotelRoomId, roomsBooked);
        if (updated) {
            return new ResponseEntity<>("Room availability updated successfully", HttpStatus.OK);
        }
        return new ResponseEntity<>("Not enough rooms available", HttpStatus.BAD_REQUEST);
    }
}
