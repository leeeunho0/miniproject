package com.yonsai.HotelReservation.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.yonsai.HotelReservation.entity.ExchangeEntity;
import com.yonsai.HotelReservation.service.ExchangeService;

@Controller
public class ExchangeController {

    private ExchangeService exchangeService;

    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @GetMapping("/exchange")
    public String exchange(Model model) {
        List<ExchangeEntity> exchangeList = exchangeService.ExchangeData();
        model.addAttribute("exchangeList", exchangeList);
        return "exchange";
    }
}