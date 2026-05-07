package com.example.McpServices;


import com.example.Entity.CopEntity.CopDiagnosisResult;
import com.example.Service.CopServiceImpl.CopAnalysisService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service("mcpCopService")
public class McpCopService {

    @Autowired
    private CopAnalysisService copAnalysisService;

    /**
     * 【空调COP能效专项诊断工具】
     */
    @Tool(name = "diagnose_cop_efficiency",
        description = """
        【COP能效专项诊断工具，计算完cop自动调用】
        
        功能：深度诊断能效比（Coefficient of Performance）健康状况。
        
        COP计算逻辑：通过CopService.CopCompute计算
        
        诊断流程：
        1. 计算当前时段COP（基于ac_power_consumption）
        2. 查询上月同期COP进行环比对比
        3. 健康状态判定：
           - CRITICAL: COP<2.0（严重偏低，效率<50%）
           - WARNING: 环比下降>10%
           - NORMAL: 运行正常
        4. 知识库联动：CRITICAL状态时自动查询原因
        5. 环境关联：高温时段(>35℃)且COP<3.0时建议优化运行时段
        
        效率等级：优秀(≥4.0)/良好(≥3.0)/合格(≥2.5)/偏低(≥2.0)/严重低效(<2.0)
        
        参数：
        - buildingId: 建筑ID
        - deviceId: 设备ID
        - days: 分析天数（可选，默认7天，范围1-30）
        
        返回：当前COP、上月COP、健康状态、诊断结论、效率等级、修复建议列表
        """)
    public String diagnoseCopEfficiency(
            @ToolParam(description = "建筑ID，必填") Integer buildingId,
            @ToolParam(description = "设备ID，必填") Integer deviceId,
            @ToolParam(description = "分析天数，默认7天，范围1-30", required = false) Integer days) {

        // 参数校验
        if (buildingId == null || deviceId == null) {
            return "{\"error\": \"buildingId和deviceId不能为空\"}";
        }

        // 默认7天，限制1-30天
        int analysisDays = (days == null) ? 7 : Math.max(1, Math.min(30, days));

        // 计算时间范围
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(analysisDays);

        try {
            // 调用现有COP诊断服务
            CopDiagnosisResult result = copAnalysisService.diagnoseCOP(
                    buildingId, deviceId, startTime, endTime);

            if (result == null) {
                return String.format("{\"error\": \"设备%d在指定时段无COP计算数据\"}", deviceId);
            }

            return convertCopResultToText(result, deviceId, analysisDays);

        } catch (Exception e) {
            return String.format("{\"error\": \"COP诊断失败: %s\"}", e.getMessage());
        }
    }

    private String convertCopResultToText(CopDiagnosisResult result, Integer deviceId, int days) {
        StringBuilder sb = new StringBuilder();

        sb.append("【COP能效诊断报告】\n\n");
        sb.append("设备：").append(deviceId).append(" | 分析周期：").append(days).append("天\n\n");

        // 核心指标
        sb.append("=== 核心指标 ===\n");
        sb.append(String.format("当前COP：%.2f\n", result.getCurrentCop()));
        sb.append(String.format("上月COP：%.2f\n", result.getLastMonthCop()));

        // 环比变化
        if (result.getLastMonthCop() > 0) {
            double change = (result.getCurrentCop() - result.getLastMonthCop())
                    / result.getLastMonthCop() * 100;
            sb.append(String.format("环比变化：%.1f%% (%s)\n",
                    Math.abs(change), change >= 0 ? "↑" : "↓"));
        }
        sb.append("\n");

        // 健康状态
        sb.append("=== 健康状态 ===\n");
        sb.append("评级：").append(result.getStatus()).append("\n");
        sb.append("效率等级：").append(result.getEfficiencyLevel()).append("\n");
        sb.append("诊断结论：").append(result.getDiagnosis()).append("\n\n");

        // 修复建议
        sb.append("=== 修复建议 ===\n");
        if (result.getSuggestions() == null || result.getSuggestions().isEmpty()) {
            sb.append("暂无具体建议\n");
        } else {
            for (int i = 0; i < result.getSuggestions().size(); i++) {
                sb.append(String.format("%d. %s\n", i + 1, result.getSuggestions().get(i)));
            }
        }

        return sb.toString();
    }

}