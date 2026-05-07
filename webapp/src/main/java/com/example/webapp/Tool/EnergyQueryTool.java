package com.example.webapp.Tool;


import com.example.webapp.Entity.energyReadings;
import com.example.webapp.Service.energyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EnergyQueryTool {

    private final energyService energyReadingService;

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    @Tool(name = "queryDeviceEnergy",description = """
        查询设备的能耗历史数据或实时数据。
        当用户询问以下问题时调用此工具：
        - 查一下能耗数据 / 最近用电量多少
        - 昨天/上周/上个月的能耗是多少
        - 实时功率是多少
        """)
    public List<energyReadings> queryEnergyReadings(
            @ToolParam(description = "查询的开始时间，格式：yyyy-MM-dd HH:mm:ss，可选")String star,
            @ToolParam(description = "查询的结束时间，格式：yyyy-MM-dd HH:mm:ss，可选") String end
    ) {
        LocalDateTime startTime = parseTime(star);
        LocalDateTime endTime = parseTime(end);
        log.info("工具调用：queryEnergyReadings, startTime={}, endTime={}", startTime, endTime);
        // 直接调用已有的 Service 方法（Service 内部已处理客户登录信息）
        return energyReadingService.queryEnergyReadings(startTime, endTime);
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