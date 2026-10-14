package com.yonsai.HotelReservation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor // 기본 생성자 (JPA가 객체를 만들 때 필요)
public class Payment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY) // PK 자동 증가
  private Long id;

  // 토스에 넘길 주문번호. 결제 전에 서버가 UUID로 만든다
  @Column(nullable = false)
  private String orderId;

  @ManyToOne // 결제(N) → 숙소(1)
  @JoinColumn(name = "hotel_id") // 외래키 컬럼 이름
  private Hotel hotel;

  // 서버가 저장한 기준 금액 → 결제 후 돌아온 금액과 비교
  private Double amount;

  private String status; // READY → DONE / FAILED
  private String paymentKey; // 승인 후 토스가 주는 값

  private LocalDateTime createdAt;
  private LocalDateTime paidAt;

  // 결제 준비 단계에서 사용하는 생성자
  public Payment(Hotel hotel, Double amount) {
    this.orderId = UUID.randomUUID().toString(); // 겹치지 않는 고유 주문번호
    this.hotel = hotel;
    this.amount = amount;
    this.status = "READY";
    this.createdAt = LocalDateTime.now();
  }

  // 승인 성공 시 상태 변경 (변경 후 Repository의 save()로 DB에 반영)
  public void markDone(String paymentKey) {
    this.paymentKey = paymentKey;
    this.status = "DONE";
    this.paidAt = LocalDateTime.now();
  }

  public void markFailed() {
    this.status = "FAILED";
  }
}