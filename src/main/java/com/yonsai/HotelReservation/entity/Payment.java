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

  // READY(준비) → DONE(완료) / FAILED(실패) / CANCELED(취소) / EXPIRED(만료) / UNKNOWN(결과 확인 필요)
  private String status;
  private String paymentKey; // 결제창에서 돌아올 때 토스가 주는 값 (조회·취소에 사용)

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

  // 금액 불일치, 토스 승인 거절
  public void markFailed() {
    this.status = "FAILED";
  }

  // 결제창에서 사용자가 취소 / 승인 후 DB 저장 실패로 결제를 되돌린 경우
  public void markCanceled() {
    this.status = "CANCELED";
  }

  // 결제창을 닫고 돌아오지 않아 READY로 30분 넘게 남은 주문
  public void markExpired() {
    this.status = "EXPIRED";
  }

  // 승인 요청 중 통신이 끊겨 결제 결과를 아직 모르는 경우 → 나중에 paymentKey로 다시 조회
  public void markUnknown(String paymentKey) {
    this.paymentKey = paymentKey;
    this.status = "UNKNOWN";
  }
}