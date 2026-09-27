package com.yonsai.HotelReservation.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "exchange_entity",
    // 같은 통화 + 같은 조회일자 조합은 중복 저장 안 되게 제약 추가 (새로고침마다 row 쌓이던 문제 방지)
    uniqueConstraints = @UniqueConstraint(columnNames = {"통화코드", "조회일자"})
)
public class ExchangeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String 통화코드;    // cur_unit (예: USD, JPY(100))
  private String 통화명;      // cur_nm (예: 미국 달러)
  private Double 매매기준율;   // deal_bas_r -> 콤마 제거 후 숫자로 저장 (문자열이면 나중에 그래프/계산할 때 불편함)
  private Double 살때;        // tts
  private Double 팔때;        // ttb
  private LocalDate 조회일자;  // 이 환율이 조회/수집된 날짜 (API 응답엔 없고, 저장 시점에 직접 채워줌)

  public Long getId() { return id; }

  public String get통화코드() { return 통화코드; }
  public void set통화코드(String 통화코드) { this.통화코드 = 통화코드; }

  public String get통화명() { return 통화명; }
  public void set통화명(String 통화명) { this.통화명 = 통화명; }

  public Double get매매기준율() { return 매매기준율; }
  public void set매매기준율(Double 매매기준율) { this.매매기준율 = 매매기준율; }

  public Double get살때() { return 살때; }
  public void set살때(Double 살때) { this.살때 = 살때; }

  public Double get팔때() { return 팔때; }
  public void set팔때(Double 팔때) { this.팔때 = 팔때; }

  public LocalDate get조회일자() { return 조회일자; }
  public void set조회일자(LocalDate 조회일자) { this.조회일자 = 조회일자; }
}