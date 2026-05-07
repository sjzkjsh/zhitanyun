package com.example.Repository;

import com.example.Entity.DeviceEnergyBuildingVO;
import com.example.Entity.AnalysisEntity.errorEntity;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AnomalyFeatureExtractor {

    public String extractFeatures(List<errorEntity> abnormalPoints,
                                  List<DeviceEnergyBuildingVO> dataList) {
        StringBuilder sb = new StringBuilder();

        // 异常统计
        if (!abnormalPoints.isEmpty()) {
            Map<String, Long> countByMetric = abnormalPoints.stream()
                    .collect(Collectors.groupingBy(errorEntity::getMetric, Collectors.counting()));
            sb.append("异常指标统计：");
            countByMetric.forEach((metric, cnt) -> sb.append(metric).append("异常").append(cnt).append("次，"));

            // 可添加最大偏离值等
            abnormalPoints.stream()
                    .filter(p -> "power_consumption".equals(p.getMetric()))
                    .max(Comparator.comparing(p -> p.getValue().subtract(p.getMax()).abs()))
                    .ifPresent(maxPoint -> sb.append("电力能耗最大偏离：")
                            .append(maxPoint.getValue()).append(maxPoint.getUnit()).append("，"));
        }

        // 环境因素摘要（从 dataList 中提取）
        if (dataList != null && !dataList.isEmpty()) {
            double avgEnvTemp = dataList.stream()
                    .mapToDouble(d -> d.getEnvTemp())
                    .average().orElse(0);
            double avgHumidity = dataList.stream()
                    .mapToDouble(d -> d.getHumidity())
                    .average().orElse(0);
            sb.append(String.format("平均环境温度%.1f℃，平均湿度%.1f%%。", avgEnvTemp, avgHumidity));
        }

        return sb.length() > 0 ? sb.toString() : "无明显异常特征";
    }
}