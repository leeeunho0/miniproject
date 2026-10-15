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

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor // final 필드 생성자 자동 → 생성자 주입
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final HotelRepository hotelRepository;

    // 승인 API 호출에 쓰는 시크릿 키 (환경변수에서 읽음, 서버에서만 사용)
    @Value("${toss.secret-key}")
    private String secretKey;

    // 토스 API 공통 주소
    private static final String TOSS_URL = "https://api.tosspayments.com/v1/payments";

    // 결제 준비: 1박 가격은 DB에서 가져오고, 총액은 서버가 박수를 곱해서 계산한다
    public PaymentPrepareResponse prepare(Long hotelId, int nights) {

        // 숙소가 없으면 예외 발생 → Controller의 @ExceptionHandler가 404 JSON으로 응답
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new NoSuchElementException("숙소 정보를 찾을 수 없습니다."));

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

    // 결제 승인: 상태 확인 → 금액 검증 → 토스 승인 API 호출 → 상태 저장
    public Payment confirm(String paymentKey, String orderId, long amount) {

        // ① 결제 준비 때 저장한 주문 정보 조회
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoSuchElementException("주문 정보를 찾을 수 없습니다."));

        // READY 상태일 때만 승인 진행
        // DONE(새로고침), FAILED·CANCELED·EXPIRED(이미 끝난 주문)는 다시 승인하지 않고 그대로 반환
        if (!"READY".equals(payment.getStatus())) {
            return payment;
        }

        // ② 금액 검증: 결제창에서 돌아온 금액을 Double로 바꿔 DB 금액과 비교
        Double returnedAmount = Double.parseDouble(String.valueOf(amount));
        if (!payment.getAmount().equals(returnedAmount)) {
            payment.markFailed();
            return paymentRepository.save(payment);
        }

        // ③ 토스 승인 API 호출 (Map을 body에 넣으면 JSON으로 자동 변환되어 전송)
        try {
            String result = RestClient.create()
                    .post().uri(TOSS_URL + "/confirm")
                    .header("Authorization", authHeader())
                    .body(Map.of(
                            "paymentKey", paymentKey,
                            "orderId", orderId,
                            "amount", amount))
                    .retrieve()
                    .body(String.class);

            System.out.println("토스 승인 응답: " + result);

            // 승인 성공 → DONE으로 변경 (아래 save()에서 UPDATE)
            payment.markDone(paymentKey);

        } catch (Exception e) {
            // 승인 거절인지, 통신만 끊긴 건지 모르니 토스에 실제 결제 상태를 다시 물어본다
            System.out.println("토스 승인 실패: " + e.getMessage());
            checkTossStatus(payment, paymentKey);
        }

        // ④ DB 저장 (승인은 됐는데 저장이 실패하면 결제를 취소해서 돈만 빠져나가는 상황을 막음)
        return saveOrCancel(payment);
    }

    // 테스트 결제: 토스 승인 API 없이 금액 검증 후 바로 DONE 처리 (인증 없이 흐름 확인용)
    public Payment mockConfirm(String orderId, long amount) {

        // 결제 준비 때 저장한 주문 정보 조회
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoSuchElementException("주문 정보를 찾을 수 없습니다."));

        // 실제 결제와 똑같이 READY 상태일 때만 진행
        if (!"READY".equals(payment.getStatus())) {
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

    // 결제창에서 사용자가 취소하거나 실패했을 때: READY 주문을 CANCELED로 변경
    public void cancelByUser(String orderId) {

        // 주문이 없을 수도 있으니 Optional로 받아서 isPresent()로 확인
        Optional<Payment> result = paymentRepository.findByOrderId(orderId);

        if (result.isPresent()) {
            Payment payment = result.get();

            // 아직 승인 전(READY)인 주문만 취소 처리 (이미 DONE인 주문은 건드리지 않음)
            if ("READY".equals(payment.getStatus())) {
                payment.markCanceled();
                paymentRepository.save(payment);
            }
        }
    }

    // 스케줄러에서 호출: 30분 넘게 READY로 남은 주문 → EXPIRED
    public int expireOldReady() {

        // 지금으로부터 30분 전 시각 (이 시각보다 먼저 만들어진 READY 주문이 대상)
        LocalDateTime limit = LocalDateTime.now().minusMinutes(30);

        List<Payment> payments = paymentRepository.findByStatusAndCreatedAtBefore("READY", limit);

        for (Payment payment : payments) {
            payment.markExpired();
            paymentRepository.save(payment);
        }
        return payments.size();
    }

    // 스케줄러에서 호출: UNKNOWN 주문을 토스에 다시 조회해서 DONE / FAILED로 확정
    public int recheckUnknown() {

        List<Payment> payments = paymentRepository.findByStatus("UNKNOWN");

        for (Payment payment : payments) {
            checkTossStatus(payment, payment.getPaymentKey());
            saveOrCancel(payment);
        }
        return payments.size();
    }

    // 토스 결제 조회 API: GET /v1/payments/{paymentKey} → 응답의 status로 실제 결제 결과 확인
    private void checkTossStatus(Payment payment, String paymentKey) {
        try {
            // 응답 JSON을 Map으로 받아서 "status" 값만 꺼내 씀
            Map result = RestClient.create()
                    .get().uri(TOSS_URL + "/" + paymentKey)
                    .header("Authorization", authHeader())
                    .retrieve()
                    .body(Map.class);

            System.out.println("토스 결제 조회 응답 status: " + result.get("status"));

            if ("DONE".equals(result.get("status"))) {
                payment.markDone(paymentKey);   // 실제로는 승인됨 (응답만 못 받은 경우)
            } else {
                payment.markFailed();           // 승인 거절, 만료 등
            }

        } catch (Exception e) {
            // 조회까지 실패하면 결과를 알 수 없으니 UNKNOWN → 스케줄러가 나중에 다시 조회
            System.out.println("토스 결제 조회 실패: " + e.getMessage());
            payment.markUnknown(paymentKey);
        }
    }

    // DB 저장, 실패하면 승인된 결제를 토스 취소 API로 되돌림 (보상 처리)
    private Payment saveOrCancel(Payment payment) {
        try {
            return paymentRepository.save(payment);

        } catch (Exception e) {
            System.out.println("결제 정보 저장 실패: " + e.getMessage());

            // 돈이 빠져나간 상태(DONE)인데 기록이 안 남았으면 결제 취소
            if ("DONE".equals(payment.getStatus())) {
                cancelToss(payment.getPaymentKey(), "주문 정보 저장 실패로 자동 취소");
                payment.markCanceled();
            }
            // 저장은 못 했지만 화면에 결과를 보여주기 위해 객체는 그대로 반환
            return payment;
        }
    }

    // 토스 결제 취소 API: POST /v1/payments/{paymentKey}/cancel (cancelReason 필수)
    private void cancelToss(String paymentKey, String cancelReason) {
        try {
            String result = RestClient.create()
                    .post().uri(TOSS_URL + "/" + paymentKey + "/cancel")
                    .header("Authorization", authHeader())
                    .body(Map.of("cancelReason", cancelReason))
                    .retrieve()
                    .body(String.class);

            System.out.println("토스 결제 취소 응답: " + result);

        } catch (Exception e) {
            // 취소까지 실패하면 토스 개발자센터에서 직접 취소해야 하므로 로그를 남김
            System.out.println("토스 결제 취소 실패 (paymentKey: " + paymentKey + "): " + e.getMessage());
        }
    }

    // 토스 인증값: "시크릿키:"(끝에 콜론)를 Base64로 변환해서 Basic 인증에 사용
    private String authHeader() {
        return "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
    }
}