package com.yonsai.HotelReservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yonsai.HotelReservation.entity.Hotel;

@Repository 
public interface HotelRepository extends JpaRepository<Hotel, Long> {
}
