package com.yonsai.HotelReservation.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Hotel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long hotelId;

  private String name;
  private String decription;
  private String location;
  private Double price;

  public Hotel() {}

  public Hotel(String name, String decription, String location, Double price) {
    this.name = name;
    this.decription = decription;
    this.location = location;
    this.price = price;
  }

  public Long getHotelId() {
    return hotelId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDecription() {
    return decription;
  }

  public void setDecription(String decription) {
    this.decription = decription;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public Double getPrice() {
    return price;
  }

  public void setPrice(Double price) {
    this.price = price;
  }
}