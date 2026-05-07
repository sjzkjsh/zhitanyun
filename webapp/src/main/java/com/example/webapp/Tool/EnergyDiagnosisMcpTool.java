package com.example.webapp.Tool;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.example.webapp.Entity.Weather.WeatherVO;
import com.example.webapp.Entity.customer;
import com.example.webapp.Entity.energyReadings;
import com.example.webapp.Mapper.energyMapper;

import com.example.webapp.Service.LoginService;
import com.example.webapp.Service.WeatherService.WeatherService;
import com.example.webapp.Service.energyService;
import com.example.webapp.Util.LoginCustomerHolder;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 能耗智能诊断与优化建议工具
 * 综合实时/历史能耗数据、环境参数（温度、湿度、人员密度）、天气预测，
 * 并结合知识库内容，生成专业诊断报告和优化建议。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EnergyDiagnosisMcpTool {
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    private final energyService energyReadingService;
    private final WeatherService weatherService;
    private final energyMapper energyReadingsMapper;
    private final LoginService loginService;

    // 如果需要在工具内主动检索知识库，可注入知识库服务（可选）
    // private final McpBailianKnowledgeService knowledgeService;


    @Tool(name = "给出优化建议"
            ,description = """
        综合诊断能耗数据、环境参数和天气，给出深度分析和优化建议。
        当用户询问以下问题时调用此工具：
        - 帮我诊断一下能耗情况
        - 分析一下最近的能耗趋势
        - 空调是不是有问题 / 为什么湿度这么高
        - 结合天气给点节能建议
        """)
    public Map<String, Object> diagnoseEnergy(
            @ToolParam(description = "开始时间，格式：yyyy-MM-dd HH:mm:ss，可选") String star,
            @ToolParam(description = "结束时间，格式：yyyy-MM-dd HH:mm:ss，可选") String end
    ) {
        LocalDateTime startTime = parseTime(star);
        LocalDateTime endTime = parseTime(end);
        // 1. 获取当前登录客户信息
        String customerName = LoginCustomerHolder.getLoginCustomer().getName();
        LambdaQueryWrapper<customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(customer::getName, customerName);
        customer currentCustomer = loginService.getOne(wrapper);
        String buildingCode = currentCustomer.getBuildingCode();
        String deviceCode = currentCustomer.getDeviceCode();

        log.info("🔍 能耗诊断工具调用 | 建筑编码={} | 设备编码={} | 时间范围=[{} ~ {}]",
                buildingCode, deviceCode, startTime, endTime);

        Map<String, Object> result = new HashMap<>();

        try {
            // 2. 获取能耗数据（调用已有 Service 方法）
            List<energyReadings> energyData = energyReadingService.queryEnergyReadings(startTime, endTime);
            result.put("energyData", energyData);

            // 3. 获取最近7条记录的平均环境数据（使用你提供的 Mapper 方法）
            Double avgEnvTemp = energyReadingsMapper.getEnvTempLatest(buildingCode, deviceCode);
            Double avgHumidity = energyReadingsMapper.gethumidityLatest(buildingCode, deviceCode);
            Double avgOccupancy = energyReadingsMapper.getPersonnelDensityLatest(buildingCode, deviceCode);

            Map<String, Object> envData = new LinkedHashMap<>();
            envData.put("avgEnvTemp", avgEnvTemp != null ? String.format("%.2f°C", avgEnvTemp) : "暂无");
            envData.put("avgHumidity", avgHumidity != null ? String.format("%.2f%%", avgHumidity) : "暂无");
            envData.put("avgOccupancyDensity", avgOccupancy != null ? String.format("%.2f人/m²", avgOccupancy) : "暂无");
            result.put("environmentData", envData);

            // 4. 获取郑州天气预报（默认地点）
            WeatherVO weather = weatherService.getWeather("郑州");
            result.put("weatherForecast", weather);

            // 5. （可选）主动检索知识库内容
            // String knowledge = knowledgeService.search("建筑能耗诊断 空调故障 节能规范");
            // result.put("knowledgeContext", knowledge);

            // 6. 构建给大模型的分析提示词
            result.put("analysisPrompt", buildAnalysisPrompt(energyData, envData, weather, startTime, endTime));

            return result;

        } catch (Exception e) {
            log.error("❌ 能耗诊断工具执行失败", e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", true);
            errorResult.put("message", "数据获取失败：" + e.getMessage());
            return errorResult;
        }
    }

    /**
     * 构建结构化分析提示，引导大模型输出高质量诊断报告
     */
    private String buildAnalysisPrompt(List<energyReadings> energyData,
                                       Map<String, Object> envData,
                                       WeatherVO weather,
                                       LocalDateTime startTime,
                                       LocalDateTime endTime) {
        StringBuilder sb = new StringBuilder();
        sb.append("## 建筑能耗智能诊断任务\n\n");

        // 分析时间范围
        if (startTime != null && endTime != null) {
            sb.append("**分析时段**：")
              .append(startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
              .append(" 至 ")
              .append(endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
              .append("\n\n");
        } else {
            sb.append("**分析模式**：实时诊断（基于最新一条监测数据）\n\n");
        }

        // 当前环境参数（最近7条平均）
        sb.append("### 📊 当前环境参数（近7次采样平均）\n");
        sb.append("- 环境温度：").append(envData.getOrDefault("avgEnvTemp", "无")).append("\n");
        sb.append("- 环境湿度：").append(envData.getOrDefault("avgHumidity", "无")).append("\n");
        sb.append("- 人员密度：").append(envData.getOrDefault("avgOccupancyDensity", "无")).append("\n\n");

        // 天气预报摘要
        sb.append("### ☁️ 郑州未来天气趋势\n");
        sb.append(weather.getSummary()).append("\n");
        sb.append("详细预报：\n");
        weather.getForecast().forEach(day -> 
            sb.append(String.format("- %s(%s): %s, %d°/%d°, 湿度%s\n",
                    day.getDate(), day.getWeekday(), day.getWeather(),
                    day.getTempHigh(), day.getTempLow(), day.getHumidity()))
        );
        sb.append("\n");

        // 能耗数据（简化展示，避免Token过长）
        sb.append("### ⚡ 能耗监测数据\n");
        if (energyData.isEmpty()) {
            sb.append("无能耗数据。\n\n");
        } else {
            sb.append("共 ").append(energyData.size()).append(" 条记录，详细数据如下（JSON格式）：\n");
            sb.append("```json\n");
            sb.append(convertEnergyDataToJson(energyData));
            sb.append("\n```\n\n");
        }

        // 分析指令（核心部分）
        sb.append("### 🧠 分析要求\n");
        sb.append("请你扮演建筑能源管理专家，基于以上数据完成以下任务：\n\n");
        sb.append("1. **能耗趋势评估**：分析能耗变化是否正常？有无明显波峰/波谷？\n");
        sb.append("2. **异常检测与故障诊断**：结合环境温度、湿度、人员密度，判断空调、照明等系统是否存在运行异常（例如：高温高湿但空调功耗未升、低人员密度但能耗居高不下等），并推测可能原因。\n");
        sb.append("3. **天气影响预测**：根据未来天气预报，预估接下来几天能耗的变化趋势及潜在风险。\n");
        sb.append("4. **优化建议**：针对发现的问题或节能机会，**请主动调用知识库工具**检索相关节能规范、设备运维指南或故障案例，给出具体、可落地的优化措施。\n\n");

        sb.append("### 📝 回答格式要求\n");
        sb.append("- 使用清晰的小标题分点阐述。\n");
        sb.append("- 专业术语可适当解释，确保非技术人员也能理解。\n");
        sb.append("- 若发现明显故障可能，请用 ⚠️ 标记并给出排查优先级。\n");

        return sb.toString();
    }

    /**
     * 将能耗数据列表转换为紧凑的 JSON 字符串，便于大模型阅读
     */
    private String convertEnergyDataToJson(List<energyReadings> data) {
        if (data == null || data.isEmpty()) {
            return "[]";
        }
        List<Map<String, Object>> simplified = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (energyReadings er : data) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("time", er.getMonitoringTime() != null ? er.getMonitoringTime().format(formatter) : "");
            item.put("power_kWh", er.getPowerConsumption());
            item.put("water_m³", er.getWaterConsumption());
            item.put("ac_power_kWh", er.getAcPowerConsumption());
            item.put("ac_outlet_temp", er.getAcOutletTemp());
            item.put("ac_inlet_temp", er.getAcInletTemp());
            item.put("env_temp", er.getEnvTemp());
            item.put("humidity", er.getHumidity());
            item.put("occupancy", er.getOccupancyDensity());
            simplified.add(item);
        }

        // 手工拼接 JSON（避免引入额外依赖）
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < simplified.size(); i++) {
            Map<String, Object> map = simplified.get(i);
            json.append("  {");
            map.forEach((k, v) -> {
                json.append("\"").append(k).append("\":");
                if (v == null) {
                    json.append("null");
                } else if (v instanceof Number) {
                    json.append(v);
                } else {
                    json.append("\"").append(v).append("\"");
                }
                json.append(",");
            });
            if (!map.isEmpty()) json.deleteCharAt(json.length() - 1); // 删除最后一个逗号
            json.append("}");
            if (i < simplified.size() - 1) json.append(",");
            json.append("\n");
        }
        json.append("]");
        return json.toString();
    }
    private LocalDateTime parseTime(String time) {
        if (time == null || time.isEmpty()) return null;
        try {
            return LocalDateTime.parse(time, TIME_FORMATTER);
        } catch (Exception e) {
            log.warn("时间解析失败: {}", time);
            return null;
        }
    }
}