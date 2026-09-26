package com.yonsai.HotelReservation.service;

import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yonsai.HotelReservation.entity.Hotel;
import com.yonsai.HotelReservation.repository.HotelRepository;

@Service
public class HotelService {

  @Autowired 
  private VectorStore 벡터저장소;

  @Autowired 
  private HotelRepository 호텔디비;

  // 가짜 데이터를 추가하는 함수!
  public void add(String name, String description, String location, double price) {

    //1) hotel테이블에 저장
    //   hotelId발급
    Hotel hotel = 호텔디비.save(new Hotel(name, description, location, price));

    //2) vector_store테이블에 저장
    //  임베딩 대상 -> 내용(description)
    //  나머지 부가 정보 -> hotelId를 이용해서 실질적인 호텔의 정보를
    // 가져올려고!
    Document document = new Document(
      description, // ★★★★★ 이 텍스트가 임베딩됨 ★★★★★
      Map.of("hotelId", hotel.getHotelId()));
    벡터저장소.add(List.of(document));
  }

  // index 화면에서 전체 호텔 목록을 보여주기 위해 추가
  public List<Hotel> getAll() {
    return 호텔디비.findAll();
  }
}