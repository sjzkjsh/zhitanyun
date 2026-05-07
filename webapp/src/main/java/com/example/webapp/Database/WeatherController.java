package com.example.webapp.Database;


import com.example.webapp.Entity.Result;
import com.example.webapp.Entity.Weather.WeatherVO;
import com.example.webapp.Service.WeatherService.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

        @GetMapping("/forecast")
    public Result<WeatherVO> forecast(@RequestParam String city) {
        if (!StringUtils.hasText(city)) {
            return Result.error("城市名称不能为空");
        }

        WeatherVO weather = weatherService.getWeather(city);
        return Result.success(weather);
    }
    /**
     * 获取默认城市天气（郑州）
     */
    @GetMapping("/default")
    public Result<WeatherVO> defaultCity() {
        return forecast("郑州");
    }
}