package com.yonsai.HotelReservation.dto;

import lombok.Getter;

// 에러가 났을 때 화면(JS)에 JSON으로 돌려줄 형식 → { "status": 404, "message": "..." }
@Getter
public class ErrorResponse {

    private final int status;
    private final String message;

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }
}