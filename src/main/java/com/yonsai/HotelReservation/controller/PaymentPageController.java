package com.yonsai.HotelReservation.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PaymentPageController {

  // 클라이언트 키는 화면에 노출돼도 되는 키 (시크릿 키는 절대 화면으로 보내지 않음)
  @Value("${toss.client-key}")
  private String clientKey;

  // GET /payment/1 → 1번 숙소 결제 페이지
  @GetMapping("/payment/{hotelId}")
  public String paymentPage(@PathVariable Long hotelId, Model model) {
    model.addAttribute("hotelId", hotelId); // 결제 준비 API 호출에 사용
    model.addAttribute("clientKey", clientKey); // 토스 결제창 초기화에 사용
    return "payment";
  }
}