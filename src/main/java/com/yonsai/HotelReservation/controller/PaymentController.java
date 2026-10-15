package com.yonsai.HotelReservation.controller;

import com.yonsai.HotelReservation.dto.ErrorResponse;
import com.yonsai.HotelReservation.dto.PaymentPrepareResponse;
import com.yonsai.HotelReservation.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentService paymentService;

  // POST /payments/prepare/3?nights=2 → 3번 숙소 2박의 주문 정보(READY) 저장 후 orderId, 금액 반환
  @PostMapping("/payments/prepare/{hotelId}")
  public ResponseEntity<PaymentPrepareResponse> prepare(@PathVariable Long hotelId,
                                                        @RequestParam int nights) {
    PaymentPrepareResponse response = paymentService.prepare(hotelId, nights);
    return ResponseEntity.status(200).body(response);
  }

  // 이 Controller 안에서만 동작하는 예외 처리 (다른 Controller에는 영향 없음)

  // 숙소가 없을 때 → 404 + 메시지 JSON
  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(NoSuchElementException e) {
    return ResponseEntity.status(404).body(new ErrorResponse(404, e.getMessage()));
  }

  // /payments/prepare/abc?nights=x 처럼 숫자 자리에 문자가 들어왔을 때 → 400
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
    return ResponseEntity.status(400).body(new ErrorResponse(400, "잘못된 요청입니다. 숙소와 숙박일을 다시 선택해주세요."));
  }

  // 그 밖의 모든 예외 (DB 오류 등) → 500
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    System.out.println("결제 준비 오류: " + e.getMessage());
    return ResponseEntity.status(500).body(new ErrorResponse(500, "결제 준비 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
  }
}