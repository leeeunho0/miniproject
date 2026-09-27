package com.yonsai.HotelReservation.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.yonsai.HotelReservation.entity.ExchangeEntity;
import com.yonsai.HotelReservation.repository.ExchangeRepository;

@Service
public class ExchangeService {

  //1. 환율데이터 가져오기!
  private String PUBLIC_URL = "https://oapi.koreaexim.go.kr/site/program/financial/exchangeJSON";
  private String PUBLIC_API_KEY ="oevS02SAC98Abv9ri8p3gGnONlbkBOOv";

  //2. 실제 일처리하는 함수
  public List<ExchangeEntity> ExchangeData(){
    System.out.println("ExchangeService - ExchangeData()");

    //3. 통신도구 생성
    RestClient 통신도구 = RestClient.create();

    // 4. 필터링 결과를 여기에 계속 모음 (하루씩 거슬러 올라가며 찾을 거라서 반복문 밖으로 뺌)
    List<ExchangeEntity> 필터링결과 = new ArrayList<>();

    // 5. 수출입은행 API는 주말/공휴일 날짜로 조회하면 데이터가 아예 없음.
    //    그래서 오늘부터 최대 7일 전까지 하루씩 뒤로 가면서, 데이터가 있는 가장 최근 영업일을 찾음
    for (int i = 0; i < 7 && 필터링결과.isEmpty(); i++) {

      String 조회날짜 = LocalDate.now().minusDays(i).format(DateTimeFormatter.ofPattern("yyyyMMdd"));

      String url = PUBLIC_URL
            + "?authkey=" + PUBLIC_API_KEY
            + "&searchdate=" + 조회날짜
            + "&data=AP01";

      // 6. 전송
      Map<String, Object>[] 결과 = 통신도구.get()
          .uri(url)
          .retrieve()
          .body(Map[].class);

      // 7. 확인
      System.out.println(조회날짜 + " 조회 결과: " + (결과 == null ? "null" : 결과.length + "건"));

      if (결과 == null) {
        continue; // 이 날짜는 데이터 없음 -> 하루 더 전으로
      }

      // 8. 원하는 통화만 필터링 + Entity로 변환
      for (Map<String, Object> 통화 : 결과) {

        if (!"1".equals(String.valueOf(통화.get("result")))) {
          continue;
        }

        String 통화코드 = String.valueOf(통화.get("cur_unit"));

        boolean 대상통화 = 통화코드.equals("USD")
            || 통화코드.equals("EUR")
            || 통화코드.equals("JPY(100)")
            || 통화코드.equals("CNH");

        if (대상통화) {
          ExchangeEntity 환율정보 = new ExchangeEntity();
          환율정보.set통화코드(통화코드);
          환율정보.set통화명((String) 통화.get("cur_nm"));
          환율정보.set매매기준율(Double.parseDouble(String.valueOf(통화.get("deal_bas_r")).replace(",", "")));
          환율정보.set살때(Double.parseDouble(String.valueOf(통화.get("tts")).replace(",", "")));
          환율정보.set팔때(Double.parseDouble(String.valueOf(통화.get("ttb")).replace(",", "")));
          환율정보.set조회일자(LocalDate.now()); // 실제 영업일이 며칠 전이어도, "오늘 조회한 값"이라는 의미로 오늘 날짜 저장

          //9.DB에 저장
          exchangeRepository.save(환율정보);

          필터링결과.add(환율정보);
        }
      }
    }

    // 10. 필터링결과는 API가 준 순서(알파벳순) 그대로라서, 원하는 순서(달러->엔화->유로->위안화)로 재배열
    String[] 통화순서 = {"USD", "JPY(100)", "EUR", "CNH"};
    List<ExchangeEntity> 정렬결과 = new ArrayList<>();
    for (String code : 통화순서) {
      for (ExchangeEntity e : 필터링결과) {
        if (e.get통화코드().equals(code)) {
          정렬결과.add(e);
          break;
        }
      }
    }

    System.out.println(정렬결과);

    return 정렬결과; // 필터링결과 대신 정렬결과 반환
  }


    private ExchangeRepository exchangeRepository;

    public ExchangeService(ExchangeRepository exchangeRepository) {
        this.exchangeRepository = exchangeRepository;
    }

    public List<ExchangeEntity> getAllExchangeData() {
        return exchangeRepository.findAll();
    }

    public ExchangeEntity saveExchangeData(ExchangeEntity exchangeEntity) {
        return exchangeRepository.save(exchangeEntity);
    }
}