package com.atguigu.RagFlowService;
import com.atguigu.Result.DailyWeather;
import com.atguigu.Result.WeatherVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriUtils;
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
         * 获取城市7天天气预报
         */
        public WeatherVO getWeather(String city) {
            try {
                log.info("查询城市天气: {}", city);
                // 步骤1：城市搜索获取 locationId
                String locationId = searchCity(city);
                if (locationId == null) {
                    log.error("找不到城市: {}", city);
                    return getFallbackWeather(city);
                }
                log.info("获取到 locationId: {}", locationId);
                // 步骤2：查询7天预报
                return getForecast(locationId, city);
            } catch (Exception e) {
                log.error("查询天气失败: {}", e.getMessage(), e);
                return getFallbackWeather(city);
            }
        }
        /**
         * 城市搜索：获取 locationId
         */
        private String searchCity(String city) {
            try {
                // 修改点：使用 bodyToMono(byte[].class) 获取原始字节
                byte[] bytes = qweatherClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/geo/v2/city/lookup")
                                .queryParam("location", city)
                                .queryParam("key", apiKey)
                                .build())
                        .retrieve()
                        .onStatus(status -> !status.is2xxSuccessful(), clientResponse -> {
                            log.error("城市搜索请求失败，状态码: {}", clientResponse.statusCode());
                            // 注意：错误响应体通常较小且未压缩，这里保留 String.class 即可
                            return clientResponse.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        log.error("错误响应内容: {}", errorBody);
                                        return Mono.error(new RuntimeException("API 返回错误: " + clientResponse.statusCode()));
                                    });
                        })
                        .bodyToMono(byte[].class)
                        .block();
                if (bytes == null) {
                    return null;
                }
                // 修改点：调用工具方法处理可能的 Gzip 压缩
                String response = handleResponse(bytes);
                JsonNode json = new ObjectMapper().readTree(response);
                String code = json.get("code").asText();
                if (!"200".equals(code)) {
                    log.error("城市搜索业务错误，code={}, 响应={}", code, response);
                    return null;
                }
                JsonNode locations = json.get("location");
                if (locations != null && locations.isArray() && locations.size() > 0) {
                    return locations.get(0).get("id").asText();
                }
                return null;
            } catch (Exception e) {
                log.error("城市搜索请求异常, city={}", city, e);
                return null;
            }
        }
        /**
         * 查询7天天气预报
         */
        private WeatherVO getForecast(String locationId, String city) {
            try {
                // 修改点：使用 bodyToMono(byte[].class)
                byte[] bytes = qweatherClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/v7/weather/7d")
                                .queryParam("location", locationId)
                                .queryParam("key", apiKey)
                                .build())
                        .retrieve()
                        .bodyToMono(byte[].class)
                        .block();
                // 修改点：调用工具方法处理可能的 Gzip 压缩
                String response = handleResponse(bytes);
                JsonNode json = new ObjectMapper().readTree(response);
                String code = json.get("code").asText();
                if (!"200".equals(code)) {
                    throw new RuntimeException("天气查询失败，code=" + code);
                }
                return parseWeather(json, city);
            } catch (Exception e) {
                log.error("天气预报查询异常, locationId={}", locationId, e);
                throw new RuntimeException("天气查询失败: " + e.getMessage(), e);
            }
        }
        /**
         * 【核心修复方法】处理响应字节流
         * 判断是否为 Gzip 压缩，如果是则解压，否则直接按 UTF-8 转字符串
         */
        private String handleResponse(byte[] bytes) throws IOException {
            if (bytes == null || bytes.length == 0) {
                return "";
            }
            // Gzip 的 Magic Number 是 0x1F8B (十进制 31, 139)
            // 通过检查前两个字节判断是否为 Gzip 格式
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
                // 如果不是 Gzip，直接转字符串
                return new String(bytes, StandardCharsets.UTF_8);
            }
        }
        /**
         * 解析天气数据
         */
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
                // 日期
                String dateStr = getTextOrDefault(day, "fxDate", "");
                dw.setDate(dateStr);
                dw.setWeekday(getWeekdayFromDate(dateStr));
                // 天气
                dw.setWeather(getTextOrDefault(day, "textDay", "--"));
                // 温度
                dw.setTempHigh(getIntOrDefault(day, "tempMax", 0));
                dw.setTempLow(getIntOrDefault(day, "tempMin", 0));
                // 风向风力
                String windDir = getTextOrDefault(day, "windDirDay", "");
                String windScale = getTextOrDefault(day, "windScaleDay", "");
                dw.setWind(windDir + " " + windScale + "级");
                // 湿度
                dw.setHumidity(getTextOrDefault(day, "humidity", "--") + "%");
                forecast.add(dw);
            }
            vo.setForecast(forecast);
            vo.setSummary(generateSummary(forecast));
            return vo;
        }
        /**
         * 辅助方法：安全获取文本
         */
        private String getTextOrDefault(JsonNode node, String field, String defaultValue) {
            JsonNode value = node.get(field);
            return value != null ? value.asText() : defaultValue;
        }
        /**
         * 辅助方法：安全获取整数
         */
        private int getIntOrDefault(JsonNode node, String field, int defaultValue) {
            JsonNode value = node.get(field);
            return value != null ? value.asInt() : defaultValue;
        }
        /**
         * 根据日期获取星期几
         */
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
        /**
         * 生成天气总结
         */
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
        /**
         * 降级方案
         */
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