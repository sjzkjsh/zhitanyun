package com.example.McpServices;


import com.example.Entity.Weather.DailyWeather;
import com.example.Entity.Weather.WeatherVO;
import com.example.Service.WeatherService.WeatherService;
import reactor.core.scheduler.Schedulers;
import com.example.Mapper.EnergyReadingsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class BuildingOptimizationMcpTool {

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private EnergyReadingsMapper energyReadingsMapper;

    @Tool(
            name = "building_optimization_strategy",
            description = "基于郑州未来7天天气预报和历史环境/能耗数据，生成建筑能源系统优化策略。" +
                    "若不需要指定具体建筑或设备，可不提供 buildingId 和 deviceId，工具将使用全局综合数据进行分析。"
    )
    public Map<String, Object> generateOptimizationStrategy(@ToolParam(required = false) Integer buildingId,
                                                            @ToolParam(required = false) Integer deviceId) {
        log.info("生成建筑优化策略，buildingId={}, deviceId={}", buildingId, deviceId);

        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 获取郑州未来7天天气（使用 subscribeOn 避免阻塞 Netty 事件循环线程）
            WeatherVO weatherData = weatherService.getWeather("郑州")
                    .subscribeOn(Schedulers.boundedElastic())
                    .block();
            result.put("weatherForecast", weatherData);

            // 2. 获取历史环境及能耗数据（通过Mapper查询）
            Double avgEnvTemp = energyReadingsMapper.getEnvTempLatest(buildingId, deviceId);
            Double avgOccupancy = energyReadingsMapper.gethumidityLatest(buildingId, deviceId); // 映射人员密度
            Double avgPowerLoad = energyReadingsMapper.getPowerLoad(buildingId, deviceId); // 新增：平均电力负载(kW)
            Double avgHumidity = energyReadingsMapper.getPersonLatest(buildingId, deviceId);   // 新增：平均湿度(%RH)

            Map<String, Object> historicalData = new HashMap<>();
            historicalData.put("avgEnvTemp", avgEnvTemp != null ? String.format("%.2f°C", avgEnvTemp) : "暂无数据");
            historicalData.put("avgOccupancyDensity", avgOccupancy != null ? String.format("%.2f人/m²", avgOccupancy) : "暂无数据");
            historicalData.put("avgPowerLoad", avgPowerLoad != null ? String.format("%.2f kW", avgPowerLoad) : "暂无数据");
            historicalData.put("avgHumidity", avgHumidity != null ? String.format("%.1f%%", avgHumidity) : "暂无数据");
            historicalData.put("dataSource", buildingId != null ? "建筑ID: " + buildingId : (deviceId != null ? "设备ID: " + deviceId : "全局数据"));
            result.put("historicalData", historicalData);

            // 3. 生成关键指标分析（加入电力负载和湿度）
            result.put("keyInsights", analyzeKeyInsights(weatherData, avgEnvTemp, avgOccupancy, avgPowerLoad, avgHumidity));

            // 4. 生成优化建议模板
            result.put("optimizationPrompt", buildOptimizationPrompt(weatherData, historicalData));

            return result;

        } catch (Exception e) {
            log.error("生成优化策略失败", e);
            return createErrorResponse(e.getMessage());
        }
    }

    /**
     * 分析关键洞察（扩展电力负载和湿度）
     */
    private Map<String, Object> analyzeKeyInsights(WeatherVO weatherData, Double avgTemp, Double avgDensity,
                                                   Double avgPowerLoad, Double avgHumidity) {
        Map<String, Object> insights = new HashMap<>();

        // 天气统计
        List<DailyWeather> forecast = weatherData.getForecast();
        long rainyDays = forecast.stream().filter(d -> d.getWeather().contains("雨")).count();
        long hotDays = forecast.stream().filter(d -> d.getTempHigh() > 30).count();
        long coldDays = forecast.stream().filter(d -> d.getTempLow() < 5).count();

        insights.put("rainyDaysCount", rainyDays);
        insights.put("hotDaysCount", hotDays);
        insights.put("coldDaysCount", coldDays);
        insights.put("weatherSummary", weatherData.getSummary());

        // 温度趋势
        if (avgTemp != null) {
            String tempTrend = avgTemp > 25 ? "偏高" : avgTemp < 18 ? "偏低" : "适中";
            insights.put("envTempTrend", tempTrend);
        }

        // 人员密度等级
        if (avgDensity != null) {
            String densityLevel = avgDensity > 0.5 ? "高密度" : avgDensity < 0.2 ? "低密度" : "中密度";
            insights.put("occupancyLevel", densityLevel);
        }

        // 新增：电力负载等级
        if (avgPowerLoad != null) {
            String loadLevel = avgPowerLoad > 500 ? "高负荷" : avgPowerLoad < 200 ? "低负荷" : "正常负荷";
            insights.put("powerLoadLevel", loadLevel);
            insights.put("avgPowerLoad", String.format("%.2f kW", avgPowerLoad));
        }

        // 新增：湿度等级
        if (avgHumidity != null) {
            String humidityLevel = avgHumidity > 70 ? "潮湿" : avgHumidity < 30 ? "干燥" : "适中";
            insights.put("humidityLevel", humidityLevel);
            insights.put("avgHumidity", String.format("%.1f%%", avgHumidity));
        }

        // 综合预警提示
        if (avgTemp != null && avgHumidity != null && avgTemp > 28 && avgHumidity > 65) {
            insights.put("comfortAlert", "高温高湿环境，需加强空调除湿与通风");
        }
        if (avgPowerLoad != null && hotDays > 3 && avgPowerLoad > 400) {
            insights.put("loadAlert", "未来多日高温，且当前电力负荷较高，建议提前优化制冷策略避免峰值");
        }

        return insights;
    }

    /**
     * 构建给大模型的优化提示（包含新增数据）
     */
    private String buildOptimizationPrompt(WeatherVO weatherData, Map<String, Object> historicalData) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("## 建筑能源优化分析任务\n\n");

        prompt.append("### 1. 未来天气预测（郑州）\n");
        prompt.append(weatherData.getSummary()).append("\n");
        prompt.append("详细预报：\n");
        for (DailyWeather day : weatherData.getForecast()) {
            prompt.append(String.format("- %s(%s): %s, %d°C/%d°C, 湿度%s, 风力%s\n",
                    day.getDate(),
                    day.getWeekday(),
                    day.getWeather(),
                    day.getTempHigh(),
                    day.getTempLow(),
                    day.getHumidity(),
                    day.getWind()
            ));
        }

        prompt.append("\n### 2. 历史环境与能耗参数\n");
        prompt.append("- 平均环境温度: ").append(historicalData.get("avgEnvTemp")).append("\n");
        prompt.append("- 平均人员密度: ").append(historicalData.get("avgOccupancyDensity")).append("\n");
        prompt.append("- 平均电力负载: ").append(historicalData.get("avgPowerLoad")).append("\n");
        prompt.append("- 平均湿度: ").append(historicalData.get("avgHumidity")).append("\n");
        prompt.append("- 数据来源: ").append(historicalData.get("dataSource")).append("\n");

        prompt.append("\n### 3. 优化目标\n");
        prompt.append("请基于以上数据，为建筑能源系统提供以下优化建议：\n");
        prompt.append("1. **空调系统**：根据温度预测、历史湿度和人员密度，建议制冷/制热及除湿策略\n");
        prompt.append("2. **通风系统**：结合雨天湿度、人员密度和电力负荷，建议新风量调节与夜间通风策略\n");
        prompt.append("3. **照明系统**：根据天气阴晴、人员密度，建议分时段照明控制\n");
        prompt.append("4. **电力负荷管理**：参考历史电力负载和高温预测，识别潜在峰值时段，提出削峰填谷建议\n");
        prompt.append("5. **预警提示**：识别高负荷、高湿、高温叠加的风险日，并给出应对措施\n");
        prompt.append("6. **节能建议**：综合所有数据，提出分时段运行策略和预期节能效果\n");

        prompt.append("\n### 4. 输出格式\n");
        prompt.append("请以结构化JSON格式返回：\n");
        prompt.append("```json\n");
        prompt.append("{\n");
        prompt.append("  \"strategy_summary\": \"总体策略概述\",\n");
        prompt.append("  \"daily_plans\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"date\": \"YYYY-MM-DD\",\n");
        prompt.append("      \"focus\": \"当日重点\",\n");
        prompt.append("      \"hvac_strategy\": \"空调策略\",\n");
        prompt.append("      \"ventilation_strategy\": \"通风策略\",\n");
        prompt.append("      \"lighting_strategy\": \"照明策略\",\n");
        prompt.append("      \"power_management\": \"电力负荷管理建议\",\n");
        prompt.append("      \"energy_saving_tips\": \"节能建议\",\n");
        prompt.append("      \"comfort_alert\": \"舒适度提醒\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"risk_alerts\": [\"风险预警1\", \"风险预警2\"],\n");
        prompt.append("  \"overall_saving_potential\": \"预计节能潜力\",\n");
        prompt.append("  \"priority_actions\": [\"优先行动1\", \"优先行动2\"]\n");
        prompt.append("}\n");
        prompt.append("```\n");

        return prompt.toString();
    }

    /**
     * 创建错误响应（降级数据）
     */
    private Map<String, Object> createErrorResponse(String errorMessage) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", true);
        errorResponse.put("message", errorMessage);

        // 提供降级天气数据
        WeatherVO fallbackWeather = new WeatherVO();
        fallbackWeather.setCity("郑州");
        fallbackWeather.setSummary("天气数据暂时不可用");
        fallbackWeather.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        errorResponse.put("weatherForecast", fallbackWeather);

        // 提供降级历史数据
        Map<String, Object> historicalData = new HashMap<>();
        historicalData.put("avgEnvTemp", "数据不可用");
        historicalData.put("avgOccupancyDensity", "数据不可用");
        historicalData.put("avgPowerLoad", "数据不可用");
        historicalData.put("avgHumidity", "数据不可用");
        historicalData.put("dataSource", "无");
        errorResponse.put("historicalData", historicalData);

        return errorResponse;
    }
}