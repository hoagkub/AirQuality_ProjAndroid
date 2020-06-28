package com.example.airquality_projandroid;

public class CityData {
    private String cityName;
    private int cityAQI;
    private String cityTimeStamp;

    public CityData() {
        this.cityName = "";
        this.cityAQI = 0;
        this.cityTimeStamp = "2020-06-27T13:00:00.000Z";
    }

    public CityData(String cityName, int cityAQI, String cityTimeStamp) {
        this.cityName = cityName;
        this.cityAQI = cityAQI;
        this.cityTimeStamp = cityTimeStamp;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getCityAQI() {
        return cityAQI;
    }

    public void setCityAQI(int cityAQI) {
        this.cityAQI = cityAQI;
    }

    public String getCityTimeStamp() {
        return cityTimeStamp;
    }

    public void setCityTimeStamp(String cityTimeStamp) {
        this.cityTimeStamp = cityTimeStamp;
    }
}
