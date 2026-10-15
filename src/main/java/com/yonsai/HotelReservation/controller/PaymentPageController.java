package com.yonsai.HotelReservation.controller;

import com.yonsai.HotelReservation.entity.Payment;
import com.yonsai.HotelReservation.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.NoSuchElementException;

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

        // 승인까지 끝나면 완료 화면, 그 외 상태는 상태별 안내 문구와 함께 실패 화면
        if ("DONE".equals(payment.getStatus())) {
            return "payment-success";
        }
        model.addAttribute("message", failMessage(payment.getStatus()));
        return "payment-fail";
    }

    // 결제창에서 취소하거나 실패했을 때 토스가 이동시키는 주소 (code, message, orderId가 함께 옴)
    @GetMapping("/payment/fail")
    public String fail(@RequestParam String message,
                       @RequestParam String orderId,
                       Model model) {

        // READY로 남아 있는 주문을 CANCELED로 정리
        paymentService.cancelByUser(orderId);

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

        if ("DONE".equals(payment.getStatus())) {
            return "payment-success";
        }
        model.addAttribute("message", failMessage(payment.getStatus()));
        return "payment-fail";
    }

    // 주문 상태별로 실패 화면에 보여줄 문구
    private String failMessage(String status) {
        if ("FAILED".equals(status)) {
            return "결제 승인에 실패했습니다. 결제 금액이 다르거나 승인이 거절되었습니다.";
        }
        if ("CANCELED".equals(status)) {
            return "취소된 주문입니다. 결제된 금액이 있다면 자동으로 취소됩니다.";
        }
        if ("EXPIRED".equals(status)) {
            return "결제 시간이 만료된 주문입니다. 다시 결제해주세요.";
        }
        if ("UNKNOWN".equals(status)) {
            return "결제 결과를 확인하고 있습니다. 잠시 후 다시 확인해주세요.";
        }
        return "결제를 완료할 수 없습니다.";
    }

    // 이 Controller 안에서 난 예외는 Whitelabel 에러 페이지 대신 실패 화면으로 보여줌

    // 주문 정보가 없을 때 (잘못된 orderId)
    @ExceptionHandler(NoSuchElementException.class)
    public String handleNotFound(NoSuchElementException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "payment-fail";
    }

    // 파라미터 누락, 숫자 자리에 문자 입력 등 그 밖의 모든 예외
    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        System.out.println("결제 페이지 오류: " + e.getMessage());
        model.addAttribute("message", "잘못된 접근이거나 결제 처리 중 오류가 발생했습니다.");
        return "payment-fail";
    }
}