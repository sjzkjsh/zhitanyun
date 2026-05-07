package com.example.Entity.Weather;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class WeatherVO implements Serializable {
    private String city;                    // 城市名
    private String updateTime;              // 更新时间
    private List<DailyWeather> forecast;    // 7天预报
    private String summary;                 // 一句话总结
    private String source;                  // 数据来源
    

}