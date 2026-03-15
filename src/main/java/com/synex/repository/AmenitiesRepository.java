package com.synex.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.synex.entity.Amenities;

@Repository
public interface AmenitiesRepository extends JpaRepository<Amenities, Integer> {
    Amenities findByName(String name);
}