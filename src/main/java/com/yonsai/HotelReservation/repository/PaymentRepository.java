package com.yonsai.HotelReservation.repository;

import com.yonsai.HotelReservation.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

  // Query Method: 메서드 이름만으로 "orderId로 찾기" 쿼리가 만들어짐
  // 결과가 없을 수도 있으니 Optional로 감쌈
  Optional<Payment> findByOrderId(String orderId);
}