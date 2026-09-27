package com.yonsai.HotelReservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yonsai.HotelReservation.entity.ExchangeEntity;

@Repository
public interface ExchangeRepository extends JpaRepository<ExchangeEntity, Long> {

  // 통화별 히스토리 조회
  List<ExchangeEntity> findBy통화코드(String 통화코드);

  // 같은 통화 + 같은 조회일자 데이터가 이미 있는지 확인
  Optional<ExchangeEntity> findBy통화코드And조회일자(String 통화코드, LocalDate 조회일자);

  // 오늘자 데이터가 이미 다 있으면 API 재호출 없이 바로 반환하기 위함
  List<ExchangeEntity> findBy조회일자(LocalDate 조회일자);
}