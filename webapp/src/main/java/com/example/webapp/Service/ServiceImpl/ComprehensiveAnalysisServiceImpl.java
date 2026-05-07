package com.example.webapp.Service.ServiceImpl;
import com.example.webapp.Entity.*;
import com.example.webapp.Mapper.energyMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComprehensiveAnalysisServiceImpl {

    private final energyMapper energyMapper;
    private final CopServiceImpl copService;
    private final CopHealthAnalysisService copHealthService;

    public ComprehensiveAnalysisResult analyzeComprehensive(int buildingId, int deviceId) {
        ComprehensiveAnalysisResult result = new ComprehensiveAnalysisResult();

        // 1. 获取最新基础能耗数据
        LatestEnergyDTO latest = energyMapper.getLatestEnergyFields(buildingId, deviceId);
        if (latest == null) {
            result.setDataComplete(false);
            result.setOverallAssessment("未获取到设备最新运行数据");
            result.setWarningLevel("UNKNOWN");
            return result;
        }

        // 2. 计算瞬时 COP
        InstantCopResult copResult = copService.calculateInstantCop();
        
        // 3. COP 健康评估（复用您已有的服务）
        CopHealthResult copHealth = copHealthService.analyzeCopHealth(copResult);
        result.setCopHealth(copHealth);

        // 4. 整理能耗指标
        Map<String, Object> energyMetrics = new LinkedHashMap<>();
        energyMetrics.put("totalPower", latest.getPowerConsumption() != null ? latest.getPowerConsumption() : 0.0);
        energyMetrics.put("acPower", latest.getAcPowerConsumption() != null ? latest.getAcPowerConsumption() : 0.0);
        energyMetrics.put("waterConsumption", latest.getWaterConsumption() != null ? latest.getWaterConsumption() : 0.0);
        energyMetrics.put("coolingCapacity", copResult.getCoolingCapacity() != null ? copResult.getCoolingCapacity() : 0.0);
        result.setEnergyMetrics(energyMetrics);

        // 5. 获取环境参数
        Double envTemp = energyMapper.getEnvTempLatestByDevice(buildingId, deviceId);
        Double humidity = energyMapper.getHumidityLatestByDevice(buildingId, deviceId);
        Double occupancy = energyMapper.getOccupancyDensityLatestByDevice(buildingId, deviceId);

        Map<String, Object> envMetrics = new LinkedHashMap<>();
        envMetrics.put("envTemp", envTemp != null ? envTemp : "无数据");
        envMetrics.put("humidity", humidity != null ? humidity : "无数据");
        envMetrics.put("occupancyDensity", occupancy != null ? occupancy : "无数据");
        result.setEnvMetrics(envMetrics);

        // 6. 计算扩展效率指标
        Map<String, Object> efficiencyIndicators = new LinkedHashMap<>();
        efficiencyIndicators.put("cop", copResult.getCop());
        // EER 近似转换 (1 COP ≈ 3.412 EER)
        efficiencyIndicators.put("eer", copResult.getCop() * 3.412);
        // 单位制冷量功耗 (kW/RT) ，1 冷吨 ≈ 3.517 kW
        if (copResult.getCoolingCapacity() != null && copResult.getCoolingCapacity() > 0) {
            double kwPerTon = copResult.getPowerConsumption() / (copResult.getCoolingCapacity() / 3.517);
            efficiencyIndicators.put("kwPerTon", kwPerTon);
        }
        result.setEfficiencyIndicators(efficiencyIndicators);

        // 7. 生成综合评价与建议
        generateOverallAssessment(result, copHealth, envTemp, humidity, occupancy);

        result.setDataComplete(true);
        return result;
    }

    private void generateOverallAssessment(ComprehensiveAnalysisResult result,
                                           CopHealthResult copHealth,
                                           Double envTemp,
                                           Double humidity,
                                           Double occupancy) {
        StringBuilder assessment = new StringBuilder();
        List<String> actions = new ArrayList<>();
        String warningLevel = "NORMAL";

        // COP 部分
        if (!copHealth.isDataValid()) {
            assessment.append("COP数据无效，无法评估能效。");
            warningLevel = "UNKNOWN";
        } else {
            assessment.append(String.format("空调瞬时COP为%.2f，", copHealth.getCop()));
            switch (copHealth.getHealthLevel()) {
                case "优秀":
                case "良好":
                    assessment.append("能效表现良好。");
                    break;
                case "一般":
                    assessment.append("能效一般，有提升空间。");
                    warningLevel = "WARNING";
                    break;
                case "较差":
                case "异常":
                    assessment.append("能效异常，需立即检查。");
                    warningLevel = "CRITICAL";
                    break;
            }
            // 继承 COP 分析中的建议
            if (copHealth.getSuggestions() != null) {
                actions.addAll(copHealth.getSuggestions());
            }
        }

        // 环境温度影响
        if (envTemp != null) {
            if (envTemp > 30) {
                assessment.append("当前环境温度较高（").append(String.format("%.1f℃", envTemp)).append("），");
                actions.add("建议适当提高空调设定温度，每提高1℃可节省约6%能耗");
                if (warningLevel.equals("NORMAL")) warningLevel = "WARNING";
            } else if (envTemp < 10) {
                assessment.append("当前环境温度较低（").append(String.format("%.1f℃", envTemp)).append("），");
                actions.add("低温环境下可减少新风量，利用自然冷源");
            }
        }

        // 湿度影响
        if (humidity != null) {
            if (humidity > 70) {
                assessment.append("湿度偏高（").append(String.format("%.1f%%", humidity)).append("），");
                actions.add("开启除湿模式或提高送风温度以降低湿度");
            } else if (humidity < 30) {
                assessment.append("湿度偏低（").append(String.format("%.1f%%", humidity)).append("），");
                actions.add("可适当加湿，避免静电和不适感");
            }
        }

        // 人员密度影响
        if (occupancy != null && occupancy > 0.8) {
            assessment.append("人员密度较高（").append(String.format("%.2f人/m²", occupancy)).append("），");
            actions.add("高密度时段需保证新风量，可提前预冷/预热");
        }

        // 若没有任何异常，给出通用建议
        if (actions.isEmpty()) {
            actions.add("系统运行平稳，建议定期清洗过滤网和冷凝器以维持高效运行");
        }

        result.setOverallAssessment(assessment.toString());
        result.setPriorityActions(actions);
        result.setWarningLevel(warningLevel);
    }
}