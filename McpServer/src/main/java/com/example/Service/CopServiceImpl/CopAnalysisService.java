package com.example.Service.CopServiceImpl;


import com.example.Entity.CopEntity.CopDiagnosisResult;
import com.example.Entity.CopEntity.CopResult;
import com.example.Enum.CopHealthStatus;

import com.example.Entity.energyReadings;
import com.example.Mapper.EnergyReadingsMapper;
import com.example.Service.CopService;
import com.example.Service.EnergyReadingsService;
import com.example.Service.ServiceImpl.RagflowService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CopAnalysisService {

    @Autowired
    private CopService copService;

    @Autowired
    private EnergyReadingsService readingsService;
    @Autowired
    private EnergyReadingsMapper mapper;

    @Autowired
    private RagflowService ragflowService;

    @Autowired
    private ObjectMapper objectMapper;  // 用于 JSON 解析

    /**
     * 智能 COP 分析 - 不只是计算，还要诊断和预测
     */
    public CopDiagnosisResult diagnoseCOP(Integer buildingId, Integer deviceId, LocalDateTime startTime, LocalDateTime endTime) {
        // 1. 当前时段 COP（用户指定的时间段）
        List<energyReadings> currentData = readingsService.selectByTime(deviceId, startTime, endTime);
        CopResult current = copService.CopCompute(currentData);

        // 2. 上月同期 - 查相同长度的时段，不是单小时！
        long daysBetween = java.time.Duration.between(startTime, endTime).toDays();
        if (daysBetween == 0) daysBetween = 1;  // 至少1天

        LocalDateTime lastMonthStart = startTime.minusMonths(1);
        LocalDateTime lastMonthEnd = endTime.minusMonths(1);  // 保持相同长度！

        List<energyReadings> lastMonthData = readingsService.selectByTime(
                deviceId, lastMonthStart, lastMonthEnd);
        CopResult lastMonth = copService.CopCompute(lastMonthData);


        // 3. 判断健康状态
        CopHealthStatus status;
        String diagnosis;
        List<String> suggestions = new ArrayList<>();

        if (current.getAverageCop() < 2.0) {
            status = CopHealthStatus.CRITICAL; // 严重偏低
            diagnosis = "COP严重偏低，系统效率不足50%，需立即检修";

            // 4. 联动 RAG 查询具体原因
            String ragQuery = String.format("COP%.1f 空调效率低 冷凝器 制冷剂", current.getAverageCop());
            String ragResult = ragflowService.searchKnowledgeBase(ragQuery);
            suggestions.addAll(parseSuggestions(ragResult));

        } else if (current.getAverageCop() < lastMonth.getAverageCop() * 0.9) {
            status = CopHealthStatus.WARNING; // 环比下降10%
            diagnosis = String.format("COP较昨天下降%.1f%%，建议关注设备状态",
                    (1 - current.getAverageCop()/lastMonth.getAverageCop()) * 100);
            suggestions.add("建议清洗冷凝器翅片");
            suggestions.add("检查冷却水流量是否正常");

        } else {
            status = CopHealthStatus.NORMAL;
            diagnosis = "COP运行正常，能效比良好";
            suggestions.add("保持当前维护节奏");
        }

        // 5. 生成优化建议（结合环境温度）
        double avgEnvTemp = currentData.stream()
                .map(energyReadings::getEnvTemp)
                .filter(java.util.Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average().orElse(0);

        if (avgEnvTemp > 35 && current.getAverageCop() < 3.0) {
            suggestions.add(String.format("当前室外温度%.1f℃超过设计工况，建议优化运行时段避开高温", avgEnvTemp));
        }

        // 6. 封装完整诊断报告
        CopDiagnosisResult result = new CopDiagnosisResult();
        result.setCurrentCop(current.getAverageCop());
        result.setLastMonthCop(lastMonth.getAverageCop());
        result.setStatus(status);
        result.setDiagnosis(diagnosis);
        result.setSuggestions(suggestions);
        result.setEfficiencyLevel(calculateEfficiencyLevel(current.getAverageCop()));

        return result;
    }

    /**
     * 解析 RAG 返回的知识文本，提取建议列表
     * 把非结构化的知识文本 → 结构化的建议条目
     */
    private List<String> parseSuggestions(String ragResult) {
        if (ragResult == null || ragResult.isEmpty()) {
            return List.of("建议联系专业维护人员检查设备");
        }

        List<String> suggestions = new ArrayList<>();

        // 方案1：如果 RAG 返回的是结构化 JSON
        try {
            JsonNode root = objectMapper.readTree(ragResult);
            JsonNode suggestionsNode = root.get("suggestions");
            if (suggestionsNode != null && suggestionsNode.isArray()) {
                for (JsonNode node : suggestionsNode) {
                    suggestions.add(node.asText());
                }
                return suggestions;
            }
        } catch (Exception e) {
            // 不是 JSON，继续用文本解析
        }

        // 方案2：文本解析（按行分割，提取带序号的建议）
        String[] lines = ragResult.split("\n");
        for (String line : lines) {
            line = line.trim();
            // 匹配 "1. xxx" 或 "建议1：xxx" 或 "- xxx" 等格式
            if (line.matches("^\\d+[.．、]\\s*.+") ||           // 1. xxx 或 1、xxx
                    line.matches("^[建议步骤措施]\\d*[：:]\\s*.+") ||  // 建议1：xxx
                    line.matches("^[-•*]\\s*.+")) {                  // - xxx 或 • xxx
                // 去掉序号前缀，保留纯文本
                String clean = line.replaceAll("^[\\d\\s.．、:-•*建议步骤措施]+", "").trim();
                if (!clean.isEmpty() && clean.length() > 3) {
                    suggestions.add(clean);
                }
            }
        }

        // 如果没提取到，整段作为一条建议
        if (suggestions.isEmpty()) {
            suggestions.add(ragResult.length() > 100
                    ? ragResult.substring(0, 100) + "..."
                    : ragResult);
        }

        return suggestions;
    }

    private String calculateEfficiencyLevel(double cop) {
        if (cop >= 4.0) return "优秀";
        if (cop >= 3.0) return "良好";
        if (cop >= 2.5) return "合格";
        if (cop >= 2.0) return "偏低";
        return "严重低效";
    }
}