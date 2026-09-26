package com.yonsai.HotelReservation.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.yonsai.HotelReservation.entity.Hotel;
import com.yonsai.HotelReservation.service.HotelService;
import com.yonsai.HotelReservation.test.HotelDummyService;

@Controller
public class HomeController {

  // HotelService 연결
  @Autowired 
  private HotelService hotelService;

  // HotelDummyService 연결
  @Autowired 
  private HotelDummyService hotelDummyService;

  @GetMapping("/")
  public String home(Model 상자){

    // DB에 저장된 전체 호텔 목록
    List<Hotel> hotelList = hotelService.getAll();

    // 필터 탭(전체/부산/서울...)에 쓸 지역 목록을 중복 제거해서 뽑아냄
    List<String> locationList = hotelList.stream()
          .map(Hotel::getLocation)
          .distinct()
          .sorted()
          .toList();

    상자.addAttribute("hotelList", hotelList);
    상자.addAttribute("locationList", locationList);

    return "index";
  }

  // 더미데이터 삽입용 임시 엔드포인트.
  @GetMapping ("/dummy-init")
  public String dummyInit(){
        hotelDummyService.insertDummyData();
        hotelDummyService.insertJapanDummyData();
        hotelDummyService.insertEuropeDummyData();
        return "redirect:/";
  }


}
