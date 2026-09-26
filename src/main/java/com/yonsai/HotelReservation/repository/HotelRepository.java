package com.yonsai.HotelReservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yonsai.HotelReservation.entity.Hotel;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
}
