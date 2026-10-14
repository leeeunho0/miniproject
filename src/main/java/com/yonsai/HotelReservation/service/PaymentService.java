package com.yonsai.HotelReservation.service;

import com.yonsai.HotelReservation.dto.PaymentPrepareResponse;
import com.yonsai.HotelReservation.entity.Hotel;
import com.yonsai.HotelReservation.entity.Payment;
import com.yonsai.HotelReservation.repository.HotelRepository;
import com.yonsai.HotelReservation.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor // final 필드 생성자 자동 → 생성자 주입
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final HotelRepository hotelRepository;

  // 결제 준비: 금액은 화면이 아니라 "서버가 DB 가격으로" 정한다
  public PaymentPrepareResponse prepare(Long hotelId) {

    // 숙소가 없으면 예외 발생
    Hotel hotel = hotelRepository.findById(hotelId)
        .orElseThrow(() -> new NoSuchElementException("숙소 없음: " + hotelId));

    // 숙소 가격을 결제 금액으로 사용 (지금은 1박 기준)
    Double amount = hotel.getPrice();

    // save(): DB에 저장하고, id가 채워진 객체를 돌려줌
    Payment payment = paymentRepository.save(new Payment(hotel, amount));

    return new PaymentPrepareResponse(payment.getOrderId(), hotel.getName(), payment.getAmount());
  }
}