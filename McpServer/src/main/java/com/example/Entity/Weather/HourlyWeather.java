package com.example.Entity.Weather;

import lombok.Data;
import java.io.Serializable;

/**
 * 逐小时天气预报 - 用于负荷预测
 */
@Data
public class HourlyWeather implements Serializable {
    private String fxTime;          // 预报时间，如"2026-04-02 14:00"
    private Integer temp;           // 温度(℃)
    private Integer humidity;       // 湿度(%)
    private Double precip;          // 降水量(mm)
    private Integer pressure;       // 气压(hPa)
    private Integer windSpeed;      // 风速(km/h)
    private String text;            // 天气状况，如"晴"、"多云"
    private Double solarRadiation;  // 太阳辐射(W/m²)，估算值
    private String windDir;         // 风向
}