package com.example.McpServices;





import com.example.Entity.CopEntity.CopDiagnosisResult;
import com.example.Entity.CopEntity.CopResult;
import com.example.Entity.energyReadings;
import com.example.Mapper.EnergyReadingsMapper;
import com.example.Service.CopService;
import com.example.Service.CopServiceImpl.CopAnalysisService;
import com.example.Service.EnergyReadingsService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service("mcpCopComputer")
public class McpCopCompute {

    @Autowired
    private EnergyReadingsService energyReadingsService;

    @Autowired
    private CopService copService;

    @Autowired
    private CopAnalysisService copAnalysisService;
    @Autowired
    private EnergyReadingsMapper energyReadingsMapper;

    @Tool(name = "compute_cop",
            description = """
【计算设备COP（性能系数/能效比）】

功能：计算空调系统的COP值。支持三种模式：
1. 指定时间点：输入设备ID和具体时间点，自动查询该时间点所在小时的运行数据计算COP。
2. 默认最新：如果不提供时间，自动获取该设备数据库中最新的一条记录计算COP。
3. 如果用户直接说计算一下cop之类的那么就直接计算刚刚查询到的数据的cop值
参数：
- deviceId: 设备ID（整数，必填）
- monitorTime: 时间点，格式yyyy-MM-dd HH:mm:ss（可选，不提供则使用最新数据）

返回：COP计算结果、能效评级、诊断建议
""")
    public String CopCompute(
            @ToolParam(description = "设备ID") int deviceId,
            @ToolParam(description = "时间点，格式yyyy-MM-dd HH:mm:ss，可选", required = false) String monitorTime) {

        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║  MCP工具: compute_cop_basic        ║");
        System.out.println("║  参数: deviceId=" + deviceId);
        System.out.println("║        time=" + (monitorTime != null ? monitorTime : "最新"));
        System.out.println("╚════════════════════════════════════╝\n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime targetTime;
        List<energyReadings> energy;

        if (monitorTime == null) {
            // 未提供时间，查询最新数据
            LocalDateTime latestTime = energyReadingsMapper.selectLastTime(deviceId);
            if (latestTime == null) {
                return String.format("{\"error\": \"设备 %d 无任何数据\"}", deviceId);
            }
            targetTime = latestTime;
            // 查询该最新时间点所在小时的数据
            LocalDateTime start = targetTime.withMinute(0).withSecond(0).withNano(0);
            LocalDateTime end = start.plusHours(1);
            energy = energyReadingsService.queryConsumptionByDevicesId(deviceId, start, end);
            if (energy == null || energy.isEmpty()) {
                return String.format("{\"error\": \"设备 %d 最新时间点 %s 所在小时无数据\"}",
                        deviceId, latestTime.format(formatter));
            }
        } else {
            // 提供了时间点，按该时间点所在小时查询
            targetTime = LocalDateTime.parse(monitorTime, formatter);
            LocalDateTime start = targetTime.withMinute(0).withSecond(0).withNano(0);
            LocalDateTime end = start.plusHours(1);
            energy = energyReadingsService.queryConsumptionByDevicesId(deviceId, start, end);
            if (energy == null || energy.isEmpty()) {
                return String.format("{\"error\": \"设备 %d 在 %s 至 %s 时段无数据\"}",
                        deviceId, start.format(formatter), end.format(formatter));
            }
        }

        CopResult cop = copService.CopCompute(energy);
        if (cop == null) {
            return "{\"error\": \"COP计算失败，请检查水流量参数\"}";
        }

        return cop.toString();
    }

    @Tool(name = "diagnose_cop",
            description = "COP 智能诊断 - 分析趋势、健康评级、生成建议。参数 buildingId:建筑 ID(整数),devicesId:设备 ID(整数),startTime:开始时间 (yyyy-MM-dd HH:mm:ss),endTime:结束时间。调用 CopAnalysisService.diagnoseCOP() 进行深度分析：1) 计算当前 COP;2) 对比上月同期数据;3) 判断健康状态 (NORMAL/WARNING/CRITICAL);4) 联动 RAG 知识库生成优化建议。返回 CopDiagnosisResult:currentCop(当前 COP)、lastMonthCop(上月 COP)、status(健康状态)、diagnosis(诊断结论)、suggestions(建议列表)、efficiencyLevel(能效等级：优秀/良好/合格/偏低/严重低效)。适用于系统预测性维护。")
    public String diagnoseCop(int buildingId, int devicesId, String startTime, String endTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime start = LocalDateTime.parse(startTime, formatter);
        LocalDateTime end = LocalDateTime.parse(endTime, formatter);

        CopDiagnosisResult result = copAnalysisService.diagnoseCOP(buildingId, devicesId, start, end);

        if (result == null) {
            return "{\"error\": \"诊断失败，无足够历史数据\"}";
        }

        return formatDiagnosisResult(result);
    }

    // ============== 私有方法 ==============

    private String formatDiagnosisResult(CopDiagnosisResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("【COP 智能诊断报告】\n\n");
        sb.append("========================\n\n");
        sb.append(String.format("当前 COP：%.2f(%s)\n",
                result.getCurrentCop(), result.getEfficiencyLevel()));
        sb.append(String.format("上月对比：%.2f\n\n", result.getLastMonthCop()));

        double change = result.getLastMonthCop() > 0 ?
                (result.getCurrentCop() - result.getLastMonthCop()) / result.getLastMonthCop() * 100 : 0;
        sb.append(String.format("环比变化：%.1f%%\n\n", change));

        sb.append("健康状态：").append(result.getStatus()).append("\n\n");
        sb.append("诊断结论：").append(result.getDiagnosis()).append("\n\n");

        sb.append("【优化建议】\n\n");
        if (result.getSuggestions() != null) {
            for (int i = 0; i < result.getSuggestions().size(); i++) {
                sb.append(String.format("%d. %s\n", i + 1, result.getSuggestions().get(i)));
            }
        }

        return sb.toString();
    }
}