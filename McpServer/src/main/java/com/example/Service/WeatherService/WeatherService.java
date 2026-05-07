package com.example.Service.WeatherService;

import com.example.Entity.Weather.DailyWeather;
import com.example.Entity.Weather.WeatherVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

@Service
@Slf4j
public class WeatherService {

    @Value("${weather.qweather.key}")
    private String apiKey;
    @Value("${weather.qweather.base-url}")
    private String baseUrl;
    private WebClient qweatherClient;

    @PostConstruct
    public void init() {
        this.qweatherClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * 【核心改动】返回 Mono<WeatherVO> 而非同步 WeatherVO
     * 整个调用链全部响应式化，无任何阻塞
     */
    public Mono<WeatherVO> getWeather(String city) {
        log.info("查询城市天气: {}", city);
        // 步骤1：城市搜索获取 locationId，返回 Mono<String>
        return searchCity(city)
                .flatMap(locationId -> {
                    log.info("获取到 locationId: {}", locationId);
                    // 步骤2：查询7天预报，返回 Mono<WeatherVO>
                    return getForecast(locationId, city);
                })
                .onErrorResume(e -> {
                    log.error("查询天气失败: {}", e.getMessage(), e);
                    return Mono.just(getFallbackWeather(city));
                });
    }

    /**
     * 【核心改动】城市搜索：返回 Mono<String>，完全非阻塞
     */
    private Mono<String> searchCity(String city) {
        return qweatherClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/geo/v2/city/lookup")
                        .queryParam("location", city)
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), clientResponse -> {
                    log.error("城市搜索请求失败，状态码: {}", clientResponse.statusCode());
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(errorBody -> {
                                log.error("错误响应内容: {}", errorBody);
                                return Mono.error(new RuntimeException("API 返回错误: " + clientResponse.statusCode()));
                            });
                })
                // 获取原始字节数组（可能被 Gzip 压缩）
                .bodyToMono(byte[].class)
                // 使用 handleResponse 方法处理 Gzip，返回 Mono<String>
                .map(this::handleResponseUnchecked)
                .flatMap(response -> {
                    try {
                        JsonNode json = new ObjectMapper().readTree(response);
                        String code = json.get("code").asText();
                        if (!"200".equals(code)) {
                            log.error("城市搜索业务错误，code={}, 响应={}", code, response);
                            return Mono.empty(); // 返回空，触发 switchIfEmpty 或后续处理
                        }
                        JsonNode locations = json.get("location");
                        if (locations != null && locations.isArray() && locations.size() > 0) {
                            return Mono.just(locations.get(0).get("id").asText());
                        }
                        return Mono.empty();
                    } catch (Exception e) {
                        log.error("解析城市搜索结果异常, city={}", city, e);
                        return Mono.error(e);
                    }
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("找不到城市: {}", city);
                    return Mono.empty(); // 触发外层 onErrorResume 降级
                }));
    }

    /**
     * 【核心改动】查询7天预报：返回 Mono<WeatherVO>
     */
    private Mono<WeatherVO> getForecast(String locationId, String city) {
        return qweatherClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v7/weather/7d")
                        .queryParam("location", locationId)
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(byte[].class)
                .map(this::handleResponseUnchecked)
                .map(response -> {
                    try {
                        JsonNode json = new ObjectMapper().readTree(response);
                        String code = json.get("code").asText();
                        if (!"200".equals(code)) {
                            throw new RuntimeException("天气查询失败，code=" + code);
                        }
                        return parseWeather(json, city);
                    } catch (Exception e) {
                        log.error("解析天气预报数据异常, locationId={}", locationId, e);
                        throw new RuntimeException("天气查询失败: " + e.getMessage(), e);
                    }
                });
    }

    /**
     * 将可能抛出 IOException 的 handleResponse 包装为无受检异常的版本，以便在 map 中使用
     */
    private String handleResponseUnchecked(byte[] bytes) {
        try {
            return handleResponse(bytes);
        } catch (IOException e) {
            throw new RuntimeException("处理响应内容失败", e);
        }
    }

    /**
     * 处理响应字节流：判断 Gzip 并解压
     */
    private String handleResponse(byte[] bytes) throws IOException {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        // Gzip Magic Number: 0x1F 0x8B
        if ((bytes[0] & 0xff) == 0x1F && (bytes[1] & 0xff) == 0x8B) {
            log.debug("检测到 Gzip 压缩响应，开始手动解压...");
            try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                 GZIPInputStream gis = new GZIPInputStream(bis);
                 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = gis.read(buffer)) > 0) {
                    bos.write(buffer, 0, len);
                }
                return bos.toString(StandardCharsets.UTF_8.name());
            }
        } else {
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    // 以下解析方法、辅助方法与原来保持一致，无需改动
    private WeatherVO parseWeather(JsonNode json, String city) {
        WeatherVO vo = new WeatherVO();
        vo.setCity(city);
        vo.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        List<DailyWeather> forecast = new ArrayList<>();
        JsonNode dailyArray = json.get("daily");
        if (dailyArray == null || !dailyArray.isArray()) {
            log.error("天气响应中没有 daily 数组");
            vo.setForecast(forecast);
            vo.setSummary("暂无预报数据");
            return vo;
        }
        for (JsonNode day : dailyArray) {
            DailyWeather dw = new DailyWeather();
            String dateStr = getTextOrDefault(day, "fxDate", "");
            dw.setDate(dateStr);
            dw.setWeekday(getWeekdayFromDate(dateStr));
            dw.setWeather(getTextOrDefault(day, "textDay", "--"));
            dw.setTempHigh(getIntOrDefault(day, "tempMax", 0));
            dw.setTempLow(getIntOrDefault(day, "tempMin", 0));
            String windDir = getTextOrDefault(day, "windDirDay", "");
            String windScale = getTextOrDefault(day, "windScaleDay", "");
            dw.setWind(windDir + " " + windScale + "级");
            dw.setHumidity(getTextOrDefault(day, "humidity", "--") + "%");
            forecast.add(dw);
        }
        vo.setForecast(forecast);
        vo.setSummary(generateSummary(forecast));
        return vo;
    }

    private String getTextOrDefault(JsonNode node, String field, String defaultValue) {
        JsonNode value = node.get(field);
        return value != null ? value.asText() : defaultValue;
    }

    private int getIntOrDefault(JsonNode node, String field, int defaultValue) {
        JsonNode value = node.get(field);
        return value != null ? value.asInt() : defaultValue;
    }

    private String getWeekdayFromDate(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            String[] weekdays = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
            int idx = date.getDayOfWeek().getValue() - 1;
            return weekdays[idx];
        } catch (Exception e) {
            return "";
        }
    }

    private String generateSummary(List<DailyWeather> forecast) {
        if (forecast.isEmpty()) return "暂无数据";
        long rainyDays = forecast.stream()
                .filter(d -> d.getWeather().contains("雨"))
                .count();
        int avgHigh = forecast.stream().mapToInt(DailyWeather::getTempHigh).sum() / forecast.size();
        int avgLow = forecast.stream().mapToInt(DailyWeather::getTempLow).sum() / forecast.size();
        if (rainyDays > 3) {
            return String.format("本周多雨，%d天有降雨，平均气温%d°/%d°", rainyDays, avgHigh, avgLow);
        } else if (rainyDays > 0) {
            return String.format("本周%d天有雨，平均气温%d°/%d°", rainyDays, avgHigh, avgLow);
        } else {
            return String.format("本周晴好，平均气温%d°/%d°", avgHigh, avgLow);
        }
    }

    private WeatherVO getFallbackWeather(String city) {
        WeatherVO vo = new WeatherVO();
        vo.setCity(city);
        vo.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        vo.setSummary("暂时无法获取天气数据，请稍后重试");
        List<DailyWeather> list = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            DailyWeather dw = new DailyWeather();
            dw.setDate(LocalDate.now().plusDays(i).toString());
            dw.setWeather("--");
            dw.setTempHigh(0);
            dw.setTempLow(0);
            dw.setWind("--");
            dw.setHumidity("--");
            list.add(dw);
        }
        vo.setForecast(list);
        return vo;
    }
}