package com.yonsai.HotelReservation.dto;

// record: 값만 담는 DTO를 짧게 작성 (생성자, getter 자동)
public record PaymentPrepareResponse(
    String orderId, // 주문번호
    String orderName, // 결제창에 보일 이름 (숙소명)
    Double amount // 결제 금액
) {
}