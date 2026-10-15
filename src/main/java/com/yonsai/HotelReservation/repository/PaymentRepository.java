package com.yonsai.HotelReservation.repository;

import com.yonsai.HotelReservation.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

  // Query Method: 메서드 이름만으로 "orderId로 찾기" 쿼리가 만들어짐
  // 결과가 없을 수도 있으니 Optional로 감쌈
  Optional<Payment> findByOrderId(String orderId);

  // WHERE status = ? AND created_at < ?  → 특정 시간 이전에 만들어진 특정 상태의 주문 목록
  List<Payment> findByStatusAndCreatedAtBefore(String status, LocalDateTime time);

  // WHERE status = ?  → 결과 확인이 필요한(UNKNOWN) 주문 목록
  List<Payment> findByStatus(String status);
}