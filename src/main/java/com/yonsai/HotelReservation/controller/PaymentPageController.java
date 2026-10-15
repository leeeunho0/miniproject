package com.yonsai.HotelReservation.controller;

import com.yonsai.HotelReservation.entity.Payment;
import com.yonsai.HotelReservation.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PaymentPageController {

    private final PaymentService paymentService;

    // 클라이언트 키는 화면에 노출돼도 되는 키 (시크릿 키는 절대 화면으로 보내지 않음)
    @Value("${toss.client-key}")
    private String clientKey;

    // GET /payment/1?nights=2 → 1번 숙소 2박 결제 페이지
    @GetMapping("/payment/{hotelId}")
    public String paymentPage(@PathVariable Long hotelId,
                              @RequestParam int nights,
                              Model model) {
        model.addAttribute("hotelId", hotelId);     // 결제 준비 API 호출에 사용
        model.addAttribute("nights", nights);       // 결제 준비 API에 박수 전달
        model.addAttribute("clientKey", clientKey); // 토스 결제창 초기화에 사용
        return "payment";
    }

    // 결제 성공 시 토스가 이동시키는 주소 (paymentKey, orderId, amount가 함께 옴)
    @GetMapping("/payment/success")
    public String success(@RequestParam String paymentKey,
                          @RequestParam String orderId,
                          @RequestParam long amount,
                          Model model) {

        Payment payment = paymentService.confirm(paymentKey, orderId, amount);
        model.addAttribute("payment", payment);

        // 승인까지 끝나면 완료 화면, 금액 불일치나 승인 실패면 실패 화면
        if ("DONE".equals(payment.getStatus())) {
            return "payment-success";
        }
        model.addAttribute("message", "결제 승인에 실패했습니다.");
        return "payment-fail";
    }

    // 결제창에서 취소하거나 실패했을 때 토스가 이동시키는 주소 (실패 사유 message가 함께 옴)
    @GetMapping("/payment/fail")
    public String fail(@RequestParam String message, Model model) {
        model.addAttribute("message", message);
        return "payment-fail";
    }

    // 테스트 결제 완료 주소 (orderId, amount가 함께 옴)
    @GetMapping("/payment/test-success")
    public String testSuccess(@RequestParam String orderId,
                              @RequestParam long amount,
                              Model model) {

        Payment payment = paymentService.mockConfirm(orderId, amount);
        model.addAttribute("payment", payment);

        // 금액 검증까지 통과하면 완료 화면, 아니면 실패 화면
        if ("DONE".equals(payment.getStatus())) {
            return "payment-success";
        }
        model.addAttribute("message", "결제 금액이 일치하지 않습니다.");
        return "payment-fail";
    }
}