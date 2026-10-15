package com.yonsai.HotelReservation.service;

import com.yonsai.HotelReservation.dto.PaymentPrepareResponse;
import com.yonsai.HotelReservation.entity.Hotel;
import com.yonsai.HotelReservation.entity.Payment;
import com.yonsai.HotelReservation.repository.HotelRepository;
import com.yonsai.HotelReservation.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor // final 필드 생성자 자동 → 생성자 주입
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final HotelRepository hotelRepository;

    // 승인 API 호출에 쓰는 시크릿 키 (환경변수에서 읽음, 서버에서만 사용)
    @Value("${toss.secret-key}")
    private String secretKey;

    // 결제 준비: 1박 가격은 DB에서 가져오고, 총액은 서버가 박수를 곱해서 계산한다
    public PaymentPrepareResponse prepare(Long hotelId, int nights) {

        // 숙소가 없으면 예외 발생
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new NoSuchElementException("숙소 없음: " + hotelId));

        // 박수가 1보다 작게 들어오면 1박으로 처리
        if (nights < 1) {
            nights = 1;
        }

        // 결제 금액 = DB의 1박 가격 × 박수
        Double amount = hotel.getPrice() * nights;

        // save(): DB에 저장하고, id가 채워진 객체를 돌려줌
        Payment payment = paymentRepository.save(new Payment(hotel, amount));

        // 결제창에 보일 이름: "해운대 오션뷰 (2박)"
        String orderName = hotel.getName() + " (" + nights + "박)";

        return new PaymentPrepareResponse(payment.getOrderId(), orderName, payment.getAmount());
    }

    // 결제 승인: 금액 검증 → 토스 승인 API 호출 → 상태 저장
    public Payment confirm(String paymentKey, String orderId, long amount) {

        // ① 결제 준비 때 저장한 주문 정보 조회
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoSuchElementException("주문 없음: " + orderId));

        // 이미 승인된 주문이면 다시 승인하지 않음 (새로고침 대비)
        if ("DONE".equals(payment.getStatus())) {
            return payment;
        }

        // ② 금액 검증: 결제창에서 돌아온 금액을 Double로 바꿔 DB 금액과 비교
        Double returnedAmount = Double.parseDouble(String.valueOf(amount));
        if (!payment.getAmount().equals(returnedAmount)) {
            payment.markFailed();
            return paymentRepository.save(payment);
        }

        // 토스 인증값: "시크릿키:"(끝에 콜론)를 Base64로 변환해서 Basic 인증에 사용
        String encodedSecretKey = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes());

        // ③ 토스 승인 API 호출 (Map을 body에 넣으면 JSON으로 자동 변환되어 전송)
        try {
            String result = RestClient.create()
                    .post().uri("https://api.tosspayments.com/v1/payments/confirm")
                    .header("Authorization", "Basic " + encodedSecretKey)
                    .body(Map.of(
                            "paymentKey", paymentKey,
                            "orderId", orderId,
                            "amount", amount))
                    .retrieve()
                    .body(String.class);

            System.out.println("토스 승인 응답: " + result);

            // ④ 승인 성공 → DONE으로 변경 (아래 save()에서 UPDATE)
            payment.markDone(paymentKey);

        } catch (Exception e) {
            // 토스가 승인을 거절하거나 통신 오류가 나면 실패 처리
            System.out.println("토스 승인 실패: " + e.getMessage());
            payment.markFailed();
        }

        return paymentRepository.save(payment);
    }

    // 테스트 결제: 토스 승인 API 없이 금액 검증 후 바로 DONE 처리 (인증 없이 흐름 확인용)
    public Payment mockConfirm(String orderId, long amount) {

        // 결제 준비 때 저장한 주문 정보 조회
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoSuchElementException("주문 없음: " + orderId));

        // 이미 완료된 주문이면 그대로 반환
        if ("DONE".equals(payment.getStatus())) {
            return payment;
        }

        // 금액 검증은 실제 결제와 똑같이 진행
        Double returnedAmount = Double.parseDouble(String.valueOf(amount));
        if (!payment.getAmount().equals(returnedAmount)) {
            payment.markFailed();
            return paymentRepository.save(payment);
        }

        // 토스 paymentKey 대신 테스트용 값을 넣어 DONE 처리
        payment.markDone("TEST-" + orderId);
        return paymentRepository.save(payment);
    }
}