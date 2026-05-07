package com.example.McpServices;

import com.example.Entity.AnalysisEntity.AnomalyReportData;
import com.example.Entity.AnalysisEntity.EnhancedAnomalyReport;
import com.example.Service.AnalysisService.AnomalyAnalysisService;
import com.example.Service.AnalysisService.EnhancedAnomalyService;
import com.example.Service.AnalysisService.ReportExportService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;



@Service("mcpAnomalyAnalysisService")
public class McpAnomalyAnalysisService {

    @Autowired
    private AnomalyAnalysisService analysisService;

    @Autowired
    private EnhancedAnomalyService enhancedService;
    @Autowired
    private ReportExportService exportService;

    /**
     * 基础异常分析
     */
    @Tool(name = "smart_analyze_device",
            description = """
        【智能设备分析 - 自动深度诊断】
        
        自动检测设备异常状态，智能判断分析深度：
        - 无异常 → 返回基础状态报告
        - 有异常 → 自动触发深度根因分析
        
        适用场景：
        ✅ "设备有没有异常"、"看一下状态"、"检测一下"
        ✅ "分析一下设备"、"看看设备运行情况"
        ✅ 任何需要了解设备状态的场景
        
        参数：
        - deviceId: 设备ID（整数，必填）
        - buildingId: 建筑ID（整数，可选，提供后可获得COP能效分析）
        """)
    public String smartAnalyzeDevice(
            @ToolParam(description = "设备ID，必填", required = true) Integer deviceId,
            @ToolParam(description = "建筑ID，可选，提供后可获得完整COP能效分析") Integer buildingId) {

        // 1. 校验 deviceId
        if (deviceId == null) {
            return "{\"error\": \"deviceId 不能为空，请提供具体设备ID\"}";
        }

        // 2. 固定全指标检测
        List<String> metricList = Arrays.asList(
                "power_consumption", "ac_power_consumption", "water_consumption",
                "env_temp", "humidity", "occupancy_density"
        );

        try {
            // ========== 第一阶段：基础筛查 ==========
            AnomalyReportData basicData = analysisService.analyzeAuto(null, deviceId, metricList);

            // 空数据检查
            if (basicData == null || basicData.getTotalRecords() == 0) {
                return String.format("{\"anomalyCount\": 0, \"message\": \"设备%d无数据记录\"}", deviceId);
            }

            // 无异常点 → 直接返回基础报告
            if (basicData.getAbnormalPoints() == null || basicData.getAbnormalPoints().isEmpty()) {
                String start = basicData.getStartTime() != null ? basicData.getStartTime().toString() : "未知";
                String end = basicData.getEndTime() != null ? basicData.getEndTime().toString() : "未知";

                String result = String.format(
                        "【基础筛查结果】\n" +
                                "设备%d状态正常 ✅\n" +
                                "分析时段：%s 至 %s\n" +
                                "总记录数：%d\n" +
                                "异常点数：0",
                        deviceId, start, end, basicData.getTotalRecords()
                );

                // COP建议（预防性）
                if (metricList.contains("ac_power_consumption")) {
                    result += "\n\n💡 提示：虽然当前无异常，如需评估空调能效，可调用diagnose_cop_efficiency";
                }

                return result;
            }

            // ========== 第二阶段：自动深度分析（发现异常时） ==========
            StringBuilder result = new StringBuilder();

            // 2.1 输出基础异常信息
            result.append("【基础筛查结果】\n");
            result.append(convertToText(basicData));
            result.append(String.format("\n\n【分析时段】%s 至 %s",
                    basicData.getStartTime(), basicData.getEndTime()));

            result.append("\n\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            result.append("🔍 检测到异常，自动启动深度根因分析...\n");
            result.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

            // 2.2 调用深度分析
            EnhancedAnomalyReport deepReport = enhancedService.analyzeWithRootCauseAuto(
                    buildingId, deviceId, metricList);

            // 2.3 输出深度分析结果
            result.append("【深度根因分析】\n");
            result.append(convertEnhancedToText(deepReport));

            // 2.4 补充信息
            if (buildingId == null) {
                result.append("\n\n⚠️ 未提供buildingId，已跳过COP能效分析。如需完整诊断，建议提供buildingId重新分析");
            } else {
                result.append("\n\n✅ 已包含COP能效分析（提供buildingId）");
            }

            return result.toString();

        } catch (NullPointerException e) {
            return "{\"error\": \"分析失败：数据不完整，请检查设备是否有历史数据\"}";
        } catch (Exception e) {
            return "{\"error\": \"分析失败: " + e.getMessage() + "\"}";
        }
    }
    /**
     * 导出深度分析报告（HTML 格式）
     * 使用时机：当用户需要正式报告、下载文档或保存分析结果时调用。
     * 此工具会先进行深度分析，然后生成格式化的 HTML 报告并返回。
     *
     * @param buildingId 建筑ID（可选）
     * @param deviceId   设备ID（必填）
     * @return 包含 HTML 内容的字符串，可直接展示或保存为文件
     */
    @Tool(name = "export_anomaly_report",
            description = """
                导出能耗异常深度分析报告（HTML格式）。
                
                ⚠️ 使用前提：用户明确需要"导出报告"、"下载分析结果"或"保存文档"时才调用。
                如果用户只需要查看分析结果，请使用 smart_analyze_device。
                
                参数：
                - buildingId: 建筑ID（可选，但强烈建议提供以便获得更完整的 COP 诊断）
                - deviceId: 设备ID（必填）
                - metrics: 检测指标，如 power_consumption,ac_power_consumption（自动检测全部）
                
                返回：HTTP 下载链接，如 http://localhost:8080/api/reports/download/anomaly_report_101_20260419_121830.html
                用户点击链接即可下载或在浏览器中打开报告。
                """)
    public String exportAnomalyReport(
            @ToolParam(description = "建筑ID，可选") Integer buildingId,
            @ToolParam(description = "设备ID，必填") Integer deviceId) {

        List<String> metricList = Arrays.asList(
                "power_consumption", "ac_power_consumption", "water_consumption",
                "env_temp", "humidity", "occupancy_density"
        );

        try {
            // ✅ 直接返回 HTTP 下载链接
            String downloadUrl = exportService.generateHtmlReport(buildingId, deviceId, metricList);
            return String.format("[📄 点击下载能耗异常分析报告](%s)", downloadUrl);

        } catch (Exception e) {
            return "{\"error\": \"报告生成失败: " + e.getMessage().replace("\"", "\\\"") + "\"}";
        }
    }

    // ========== 私有转换方法（原有逻辑） ==========

    private String convertToText(AnomalyReportData data) {
        StringBuilder sb = new StringBuilder();

        if (data.getAnalysisStrategy() != null) {
            sb.append("【🤖 智能分析过程】\n\n");
            sb.append("选择策略：").append(data.getAnalysisStrategy()).append("\n");
            if (data.getExpectedAnomalyCount() != null) {
                sb.append("预估异常：").append(data.getExpectedAnomalyCount()).append("个\n");
            }
            sb.append("\n");
        }

        sb.append("【基础异常检测报告】\n\n");
        sb.append("设备：").append(data.getDeviceId())
                .append(" 时段：").append(data.getStartTime())
                .append(" 至 ").append(data.getEndTime()).append("\n\n");
        sb.append("异常数量：").append(data.getAnomalyCount()).append("\n");
        sb.append("数据记录：").append(data.getTotalRecords()).append("条\n");
        sb.append("异常率：")
                .append(String.format("%.1f%%", data.getAnomalyCount() * 100.0 / data.getTotalRecords()))
                .append("\n\n");
        sb.append("异常特征：").append(data.getAnomalyFeatures()).append("\n\n");

        if (data.getPossibleCauses() != null) {
            sb.append("可能原因：").append(data.getPossibleCauses()).append("\n");
        }
        return sb.toString();
    }

    private String convertEnhancedToText(EnhancedAnomalyReport report) {
        StringBuilder sb = new StringBuilder();

        AnomalyReportData basicInfo = report.getBasicInfo();
        if (basicInfo != null && basicInfo.getAnalysisStrategy() != null) {
            sb.append("【🤖 智能分析过程】\n\n");
            sb.append("选择策略：").append(basicInfo.getAnalysisStrategy()).append("\n");
            if (basicInfo.getExpectedAnomalyCount() != null) {
                sb.append("预估异常：").append(basicInfo.getExpectedAnomalyCount()).append("个\n");
            }
            sb.append("实际异常：").append(basicInfo.getAnomalyCount()).append("个\n");
            sb.append("分析时段：").append(basicInfo.getStartTime())
                    .append(" 至 ").append(basicInfo.getEndTime()).append("\n\n");
        }

        sb.append("【深度异常诊断报告】\n\n");
        sb.append("优先级：").append(report.getPriority()).append("\n\n");

        // 基础统计
        if (basicInfo != null) {
            sb.append("=== 基础统计 ===\n");
            sb.append(String.format("异常率：%.1f%% (%d/%d)\n\n",
                    basicInfo.getAnomalyCount() * 100.0 / basicInfo.getTotalRecords(),
                    basicInfo.getAnomalyCount(),
                    basicInfo.getTotalRecords()));
        }

        sb.append("=== 根因分析 ===\n\n");
        if (report.getRootCauses() == null || report.getRootCauses().isEmpty()) {
            sb.append("未发现明显根因关联\n\n");
        } else {
            report.getRootCauses().forEach(h -> {
                sb.append(String.format("[%s] %s (置信度%.0f%%)\n",
                        h.getCauseType(), h.getCauseName(), h.getConfidence() * 100));
                sb.append("  描述：").append(h.getDescription()).append("\n");
                sb.append("  建议：").append(h.getSuggestion()).append("\n\n");
            });
        }
        if (report.getPossibleCausesFromRag() != null && !report.getPossibleCausesFromRag().isEmpty()) {
            sb.append("=== 知识库参考 ===\n\n");
            sb.append(report.getPossibleCausesFromRag()).append("\n\n");
        }

        sb.append("=== 处理步骤 ===\n\n");
        if (report.getActionPlan() != null) {
            report.getActionPlan().forEach(step -> {
                sb.append(String.format("%d. [%s] %s (%s)\n",
                        step.getStepNumber(), step.getPriority(),
                        step.getTitle(), step.getEstimatedTime()));
                sb.append("   ").append(step.getDescription()).append("\n\n");
            });
        }

        return sb.toString();
    }
}