package com.synex.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.synex.entity.Hotel;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Integer> {
    
    // Search by hotel name only
    @Query("SELECT h FROM Hotel h " +
           "WHERE LOWER(h.hotelName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Hotel> searchByHotelName(@Param("searchTerm") String searchTerm);
    
    // Search by city only
    @Query("SELECT h FROM Hotel h " +
           "WHERE LOWER(h.city) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Hotel> searchByCity(@Param("searchTerm") String searchTerm);
    
    // Search by state only (exact match for codes like "CA")
    @Query("SELECT h FROM Hotel h " +
           "WHERE UPPER(h.state) = UPPER(:searchTerm)")
    List<Hotel> searchByStateExact(@Param("searchTerm") String searchTerm);
    
    // Search by address only
    @Query("SELECT h FROM Hotel h " +
           "WHERE LOWER(h.address) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Hotel> searchByAddress(@Param("searchTerm") String searchTerm);
    
    // Search all fields (for "All" option)
    @Query("SELECT DISTINCT h FROM Hotel h " +
           "WHERE UPPER(h.state) = UPPER(:searchTerm) " +
           "OR LOWER(h.hotelName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(h.city) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(h.address) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Hotel> searchByLocation(@Param("searchTerm") String searchTerm);
    
    // Find hotels by city (exact match)
    @Query("SELECT h FROM Hotel h WHERE LOWER(h.city) = LOWER(:city)")
    List<Hotel> findByCity(@Param("city") String city);
    
    // Find hotels by state (exact match)
    @Query("SELECT h FROM Hotel h WHERE LOWER(h.state) = LOWER(:state)")
    List<Hotel> findByState(@Param("state") String state);
    
    // Find hotels by star rating
    @Query("SELECT h FROM Hotel h WHERE h.starRating = :rating")
    List<Hotel> findByStarRating(@Param("rating") int rating);
    
    // Find hotels by star rating range
    @Query("SELECT h FROM Hotel h WHERE h.starRating >= :min AND h.starRating <= :max")
    List<Hotel> findByStarRatingBetween(@Param("min") int min, @Param("max") int max);
    
    // Find hotels by price range
    @Query("SELECT h FROM Hotel h WHERE h.averagePrice >= :minPrice AND h.averagePrice <= :maxPrice")
    List<Hotel> findByPriceRange(@Param("minPrice") double minPrice, @Param("maxPrice") double maxPrice);
    
    // Find hotels with specific amenities
    @Query("SELECT DISTINCT h FROM Hotel h " +
           "JOIN h.amenities a " +
           "WHERE UPPER(a.name) IN :amenityNames " +
           "GROUP BY h " +
           "HAVING COUNT(DISTINCT a) = :amenityCount")
    List<Hotel> findByAllAmenities(@Param("amenityNames") List<String> amenityNames, 
                                     @Param("amenityCount") long amenityCount);
    
    // Find hotels with at least one of the specified amenities
    @Query("SELECT DISTINCT h FROM Hotel h " +
           "JOIN h.amenities a " +
           "WHERE UPPER(a.name) IN :amenityNames")
    List<Hotel> findByAnyAmenity(@Param("amenityNames") List<String> amenityNames);
    
    // Complex filter query that works with OR without search term
    @Query("SELECT DISTINCT h FROM Hotel h " +
           "LEFT JOIN h.amenities a " +
           "WHERE (:searchTerm IS NULL OR :searchTerm = '' OR " +
           "       UPPER(h.state) = UPPER(:searchTerm) OR " +
           "       LOWER(h.hotelName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "       LOWER(h.city) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "       LOWER(h.address) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND (:minRating IS NULL OR h.starRating >= :minRating) " +
           "AND (:maxRating IS NULL OR h.starRating <= :maxRating) " +
           "AND (:minPrice IS NULL OR h.averagePrice >= :minPrice) " +
           "AND (:maxPrice IS NULL OR h.averagePrice <= :maxPrice)")
    List<Hotel> filterHotels(
        @Param("searchTerm") String searchTerm,
        @Param("minRating") Integer minRating,
        @Param("maxRating") Integer maxRating,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice
    );
}
