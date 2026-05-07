package com.example.McpServices;

import com.example.Entity.AnalysisEntity.AbnormalEnergyExportVO;
import com.example.Repository.ExcelExportUtil;
import com.example.Service.Opt.DeviceAnomalyDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class McpEnergyScanTool {

    private final DeviceAnomalyDetector anomalyDetector;
    private final ExcelExportUtil excelExportUtil;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Tool(name = "check_abnormal_devices",
            description = """
                    扫描所有设备，自动检测所有能耗指标（power_consumption, ac_power_consumption, 
                    water_consumption, ac_outlet_temp, ac_inlet_temp, env_temp, humidity, 
                    occupancy_density, water_flow_rate），统计异常设备和异常点数量。
                    此工具不会生成或导出任何文件，仅用于快速了解异常情况。
                    当用户询问"有没有异常设备"、"有多少设备异常"、"系统是否正常"等统计性问题时使用。
                    返回结果后，如果用户需要导出详细报告，再调用 export_abnormal_devices_excel。
                    """)
    public Map<String, Object> checkAbnormalDevices(
            @ToolParam(description = "是否深度分析原因（调用知识库），默认 false", required = false) Boolean deepReason) {

        log.info("执行异常设备统计（全指标检测），deepReason={}", deepReason);
        Map<String, Object> result = new HashMap<>();

        try {
            // 直接调用全指标检测（不传metrics）
            DeviceAnomalyDetector.ScanResult scanResult = anomalyDetector.scanAllDevices();

            result.put("success", true);
            result.put("message", "扫描完成");
            result.put("checkedMetrics", scanResult.getCheckedMetrics());  // 返回实际检测的指标列表
            result.put("abnormalDeviceCount", scanResult.getAbnormalDeviceCount());
            result.put("anomalyPointCount", scanResult.getAnomalyPointCount());

            // 按指标分组统计
            Map<String, Long> metricStats = scanResult.getAnomalyPoints().stream()
                    .collect(Collectors.groupingBy(
                            DeviceAnomalyDetector.AnomalyPoint::getMetricName,
                            Collectors.counting()
                    ));
            result.put("anomalyByMetric", metricStats);

            // 如果有点异常，补充前5条详情
            List<DeviceAnomalyDetector.AnomalyPoint> points = scanResult.getAnomalyPoints();
            if (!points.isEmpty()) {
                List<Map<String, Object>> top5 = points.stream()
                        .limit(5)
                        .map(p -> {
                            Map<String, Object> m = new LinkedHashMap<>();
                            m.put("deviceId", p.getDeviceId());
                            m.put("deviceCode", p.getDeviceCode());
                            m.put("buildingName", p.getBuildingName());
                            m.put("metric", p.getMetricName());
                            m.put("actualValue", String.format("%.2f %s", p.getActualValue(), p.getUnit()));
                            m.put("threshold", "[" + p.getMinValue() + ", " + p.getMaxValue() + "]");
                            m.put("deviation", calculateDeviation(p.getActualValue(), p.getMinValue(), p.getMaxValue()));
                            m.put("type", p.getAlertType());
                            m.put("time", p.getMonitoringTime().toString());
                            return m;
                        })
                        .collect(Collectors.toList());
                result.put("topAnomalies", top5);

                if (points.size() > 5) {
                    result.put("note", "共" + points.size() + "个异常点，显示前5条");
                }
            }

            // 建议状态变更提示
            if (!scanResult.getStatusChangeSuggestions().isEmpty()) {
                List<Map<String, Object>> suggestions = scanResult.getStatusChangeSuggestions().stream()
                        .limit(5)
                        .collect(Collectors.toList());
                result.put("statusChangeSuggestions", suggestions);
                if (scanResult.getStatusChangeSuggestions().size() > 5) {
                    result.put("statusChangeNote", "共" + scanResult.getStatusChangeSuggestions().size() + "条状态变更建议");
                }
            }

            return result;

        } catch (Exception e) {
            log.error("统计异常设备失败", e);
            result.put("success", false);
            result.put("message", "统计失败: " + e.getMessage());
            return result;
        }
    }

    @Tool(name = "export_abnormal_devices_excel",
            description = """
                    导出异常能耗设备明细到 Excel 文件。
                    此工具会执行完整扫描并生成 Excel 文件，返回下载链接。
                    """)
    public Map<String, Object> exportAbnormalDevicesExcel(
            @ToolParam(description = "是否深度分析原因，默认 false", required = false) Boolean deepReason) {

        log.info("执行异常设备导出（全指标检测），deepReason={}", deepReason);
        Map<String, Object> result = new HashMap<>();

        try {
            // 调用全指标检测获取异常数据
            List<AbnormalEnergyExportVO> dataList = anomalyDetector.scanForExport();

            if (dataList.isEmpty()) {
                result.put("success", true);
                result.put("message", "未发现异常设备，无需导出");
                result.put("excelDownloadUrl", null);
                return result;
            }

            // 生成 Excel
            String fileName = "异常能耗设备清单_" + LocalDateTime.now().format(DATE_TIME_FORMATTER);
            String filePath = excelExportUtil.exportToExcel(dataList, fileName);
            String downloadUrl = "/files/" + new File(filePath).getName();

            long deviceCount = dataList.stream()
                    .map(AbnormalEnergyExportVO::getDeviceId)
                    .distinct()
                    .count();

            result.put("success", true);
            result.put("message", "导出成功");
            result.put("checkedMetrics", DeviceAnomalyDetector.ALL_METRICS);
            result.put("abnormalDeviceCount", deviceCount);
            result.put("anomalyPointCount", dataList.size());
            result.put("excelDownloadUrl", downloadUrl);
            return result;

        } catch (Exception e) {
            log.error("导出异常设备失败", e);
            result.put("success", false);
            result.put("message", "导出失败: " + e.getMessage());
            return result;
        }
    }

    private String calculateDeviation(Double actual, BigDecimal min, BigDecimal max) {
        if (actual == null) return "N/A";
        if (max != null && actual > max.doubleValue()) {
            double rate = (actual - max.doubleValue()) / max.doubleValue() * 100;
            return "+" + String.format("%.1f%%", rate);
        } else if (min != null && actual < min.doubleValue()) {
            double rate = (min.doubleValue() - actual) / min.doubleValue() * 100;
            return "-" + String.format("%.1f%%", rate);
        }
        return "0%";
    }
}