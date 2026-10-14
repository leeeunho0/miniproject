package com.yonsai.HotelReservation.controller;

import com.yonsai.HotelReservation.dto.PaymentPrepareResponse;
import com.yonsai.HotelReservation.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentService paymentService;

  // POST /payments/prepare/3 → 3번 숙소의 주문 정보(READY) 저장 후 orderId, 금액 반환
  @PostMapping("/payments/prepare/{hotelId}")
  public ResponseEntity<PaymentPrepareResponse> prepare(@PathVariable Long hotelId) {
    PaymentPrepareResponse response = paymentService.prepare(hotelId);
    return ResponseEntity.status(200).body(response);
  }
}