package com.atguigu.Result;

import lombok.Data;

import java.io.Serializable;

@Data
public  class DailyWeather implements Serializable {
        private String date;
        private String weekday;
        private String weather;
        private String weatherIcon;
        private Integer tempHigh;
        private Integer tempLow;
        private String wind;
        private String humidity;
        private String uv;
        private String airQuality;
    }