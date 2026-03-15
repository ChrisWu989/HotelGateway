package com.synex.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.synex.entity.HotelRoom;
import com.synex.entity.RoomType;

@Repository
public interface HotelRoomRepository extends JpaRepository<HotelRoom, Integer> {
    
    // Find hotel rooms by room type
    List<HotelRoom> findByType(RoomType type);
    
    // Find hotel rooms with available rooms greater than or equal to
    List<HotelRoom> findByNoRoomsGreaterThanEqual(int noRooms);
    
    // Find hotel rooms by price range
    List<HotelRoom> findByPriceBetween(float minPrice, float maxPrice);
    
    // Find hotel rooms by amenity
    @Query("SELECT DISTINCT hr FROM HotelRoom hr JOIN hr.amenities a WHERE a.name = :amenityName")
    List<HotelRoom> findByAmenityName(@Param("amenityName") String amenityName);
    
    // FIXED: Custom query to get available rooms for a hotel
    // Solution 1: Using JOIN (Recommended)
    @Query("SELECT hr FROM Hotel h " +
           "JOIN h.hotelRooms hr " +
           "WHERE h.hotelId = :hotelId")
    List<HotelRoom> findByHotelId(@Param("hotelId") int hotelId);
}