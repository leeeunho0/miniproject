package com.yonsai.HotelReservation.scheduler;

import com.yonsai.HotelReservation.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component // 스프링이 객체를 만들어 관리 → @Scheduled 메서드가 실행될 수 있음
@RequiredArgsConstructor
public class PaymentScheduler {

    private final PaymentService paymentService;

    // fixedRate = 600000 → 10분(600,000ms)마다 실행
    @Scheduled(fixedRate = 600000)
    public void cleanUpPayments() {
        // 결제창을 닫고 돌아오지 않은 READY 주문 → EXPIRED
        int expired = paymentService.expireOldReady();

        // 통신 오류로 결과를 모르는 UNKNOWN 주문 → 토스에 다시 조회해서 DONE / FAILED
        int checked = paymentService.recheckUnknown();

        System.out.println("결제 정리: 만료 " + expired + "건, 재조회 " + checked + "건");
    }
}