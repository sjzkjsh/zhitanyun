package com.example.webapp.Tool;

import com.example.webapp.Entity.Weather.WeatherVO;

import com.example.webapp.Service.WeatherService.WeatherService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class WeatherTool {

    private final WeatherService weatherService;

    public WeatherTool(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @Tool(name = "get_weather_forecast"
            ,description = "查询指定城市的天气预报，默认查询郑州")
    public String getWeatherForecast(
            @ToolParam(description = "城市名称，例如：北京、上海、郑州。不传则默认郑州") String city) {
        // 如果参数为空或未提供，使用默认值 "郑州"
        if (city == null || city.isBlank()) {
            city = "郑州";
        }
        WeatherVO weatherVO = weatherService.getWeather(city);
        return formatWeatherResult(weatherVO);
    }

    private String formatWeatherResult(WeatherVO vo) {
        if (vo == null || vo.getForecast().isEmpty()) {
            return "暂时无法获取天气数据，请稍后重试。";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("城市：").append(vo.getCity()).append("\n");
        sb.append("更新时间：").append(vo.getUpdateTime()).append("\n");
        sb.append("天气概况：").append(vo.getSummary()).append("\n\n");
        sb.append("未来7天预报：\n");
        for (var day : vo.getForecast()) {
            sb.append(day.getDate()).append(" ").append(day.getWeekday())
              .append("：").append(day.getWeather())
              .append("，").append(day.getTempLow()).append("°~").append(day.getTempHigh()).append("°")
              .append("，").append(day.getWind()).append("，湿度").append(day.getHumidity()).append("\n");
        }
        return sb.toString();
    }
}