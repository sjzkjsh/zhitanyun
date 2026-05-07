package com.example.webapp.Tool;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.webapp.Entity.Weather.DailyWeather;
import com.example.webapp.Entity.Weather.WeatherVO;
import com.example.webapp.Entity.customer;
import com.example.webapp.Mapper.energyMapper;
import com.example.webapp.Service.LoginService;
import com.example.webapp.Service.WeatherService.WeatherService;
import com.example.webapp.Util.LoginCustomerHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP工具：建筑能源优化策略生成器
 * 供大模型调用，综合天气预测和历史数据生成优化建议
 */
@Service
@Slf4j

public class BuildingOptimizationMcpTool {

    @Autowired
    private WeatherService weatherService;
    
    @Autowired
    private energyMapper energyReadingsMapper;
    @Autowired
    private LoginService loginService;


    @Tool(name = "generateOptimizationStrategy",
            description = """
              生成建筑能源优化策略和预警报告。
              当用户询问以下问题时调用此工具：
              - 帮我生成优化建议 / 节能方案
              - 未来几天需要注意什么 / 有什么风险预警
              - 结合天气给点运行建议
              """)
    public Map<String, Object> generateOptimizationStrategy() {
        String name = LoginCustomerHolder.getLoginCustomer().getName();
        LambdaQueryWrapper<customer> wrapper = new LambdaQueryWrapper<customer>();
        LambdaQueryWrapper<customer> eq = wrapper.eq(customer::getName, name);
        customer one = loginService.getOne(eq);
        String buildingCode=one.getBuildingCode();
        String deviceCode = one.getDeviceCode();
        log.info("生成建筑优化策略，buildingId={}, deviceId={}", buildingCode, deviceCode);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 获取郑州未来7天天气
            WeatherVO weatherData = weatherService.getWeather("郑州");
            result.put("weatherForecast", weatherData);
            
            // 2. 获取历史环境数据
            Double avgEnvTemp = energyReadingsMapper.getEnvTempLatest(buildingCode, deviceCode);
            Double avgOccupancy = energyReadingsMapper.gethumidityLatest(buildingCode, deviceCode);
            
            Map<String, Object> historicalData = new HashMap<>();
            historicalData.put("avgEnvTemp", avgEnvTemp != null ? String.format("%.2f°C", avgEnvTemp) : "暂无数据");
            historicalData.put("avgOccupancyDensity", avgOccupancy != null ? String.format("%.2f人/m²", avgOccupancy) : "暂无数据");
            historicalData.put("dataSource", buildingCode != null ? "建筑ID: " + buildingCode : "全局数据");
            result.put("historicalData", historicalData);
            
            // 3. 生成关键指标分析
            result.put("keyInsights", analyzeKeyInsights(weatherData, avgEnvTemp, avgOccupancy));
            
            // 4. 生成优化建议模板
            result.put("optimizationPrompt", buildOptimizationPrompt(weatherData, historicalData));
            
            return result;
            
        } catch (Exception e) {
            log.error("生成优化策略失败", e);
            return createErrorResponse(e.getMessage());
        }
    }

    /**
     * 分析关键洞察
     */
    private Map<String, Object> analyzeKeyInsights(WeatherVO weatherData, Double avgTemp, Double avgDensity) {
        Map<String, Object> insights = new HashMap<>();
        
        // 天气分析
        List<DailyWeather> forecast = weatherData.getForecast();
        long rainyDays = forecast.stream()
                .filter(d -> d.getWeather().contains("雨"))
                .count();
        long hotDays = forecast.stream()
                .filter(d -> d.getTempHigh() > 30)
                .count();
        long coldDays = forecast.stream()
                .filter(d -> d.getTempLow() < 5)
                .count();
        
        insights.put("rainyDaysCount", rainyDays);
        insights.put("hotDaysCount", hotDays);
        insights.put("coldDaysCount", coldDays);
        insights.put("weatherSummary", weatherData.getSummary());
        
        // 环境参数对比
        if (avgTemp != null) {
            String tempTrend = avgTemp > 25 ? "偏高" : avgTemp < 18 ? "偏低" : "适中";
            insights.put("envTempTrend", tempTrend);
        }
        
        if (avgDensity != null) {
            String densityLevel = avgDensity > 0.5 ? "高密度" : avgDensity < 0.2 ? "低密度" : "中密度";
            insights.put("occupancyLevel", densityLevel);
        }
        
        return insights;
    }

    /**
     * 构建给大模型的优化提示
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
        
        prompt.append("\n### 2. 历史环境参数\n");
        prompt.append("- 平均环境温度: ").append(historicalData.get("avgEnvTemp")).append("\n");
        prompt.append("- 平均人员密度: ").append(historicalData.get("avgOccupancyDensity")).append("\n");
        
        prompt.append("\n### 3. 优化目标\n");
        prompt.append("请基于以上数据，为建筑能源系统提供以下优化建议：\n");
        prompt.append("1. **空调系统**：根据温度预测和人员密度，建议制冷/制热策略\n");
        prompt.append("2. **通风系统**：结合雨天湿度和人员密度，建议新风量调节\n");
        prompt.append("3. **照明系统**：根据天气阴晴和人员密度，建议照明策略\n");
        prompt.append("4. **预警提示**：识别潜在的高负荷或舒适度风险日\n");
        prompt.append("5. **节能建议**：综合天气和人员密度，提出分时段运行策略\n");
        
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
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String errorMessage) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", true);
        errorResponse.put("message", errorMessage);
        
        // 提供降级数据
        WeatherVO fallbackWeather = new WeatherVO();
        fallbackWeather.setCity("郑州");
        fallbackWeather.setSummary("天气数据暂时不可用");
        fallbackWeather.setUpdateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        errorResponse.put("weatherForecast", fallbackWeather);
        
        Map<String, Object> historicalData = new HashMap<>();
        historicalData.put("avgEnvTemp", "数据不可用");
        historicalData.put("avgOccupancyDensity", "数据不可用");
        historicalData.put("dataSource", "无");
        errorResponse.put("historicalData", historicalData);
        
        return errorResponse;
    }
}
