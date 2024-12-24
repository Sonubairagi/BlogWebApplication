package com.blogapp.payload;

import jakarta.validation.constraints.*;

public class AddressDto {
    private Long id;

    @NotBlank(message = "Area name is required")
    @Size(min = 3, max = 100, message = "Area name must be between 3 and 100 characters")
    private String areaName;

    @NotBlank(message = "City name is required")
    @Size(min = 2, max = 50, message = "City name must be between 2 and 50 characters")
    private String cityName;

    @Positive(message = "Pin code must be a positive number")
    @Digits(integer = 6, fraction = 0, message = "Pin code must be a 6-digit number")
    private int pinCode;

    @NotBlank(message = "State name is required")
    @Size(min = 2, max = 50, message = "State name must be between 2 and 50 characters")
    private String stateName;

    @NotBlank(message = "Country name is required")
    @Size(min = 2, max = 50, message = "Country name must be between 2 and 50 characters")
    private String countryName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getPinCode() {
        return pinCode;
    }

    public void setPinCode(int pinCode) {
        this.pinCode = pinCode;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }
}
