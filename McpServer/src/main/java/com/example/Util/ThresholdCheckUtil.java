package com.example.Util;

import com.example.Entity.AnalysisEntity.ThresholdRange;
import com.example.Entity.energyReadings;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 阈值检测公共工具类
 * 从 DeviceMonitorTask 和 DeviceAnomalyDetector 中提取的重复逻辑
 */
@Slf4j
public final class ThresholdCheckUtil {

    private ThresholdCheckUtil() {}

    /** 缓存反射获取的 Field 对象，避免每次重新解析 */
    private static final Map<String, Field> FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * 通过反射获取 energyReadings 对象中指定字段的值
     * @param obj       能耗读数对象
     * @param fieldName 下划线命名的字段名（如 ac_power）
     * @return 字段值，获取失败返回 null
     */
    public static Double getFieldValueByName(energyReadings obj, String fieldName) {
        try {
            String camelCaseName = underscoreToCamel(fieldName);
            Field field = FIELD_CACHE.computeIfAbsent(camelCaseName, name -> {
                try {
                    Field f = obj.getClass().getDeclaredField(name);
                    f.setAccessible(true);
                    return f;
                } catch (NoSuchFieldException e) {
                    log.debug("反射获取字段 {} 失败: {}", name, e.getMessage());
                    return null;
                }
            });
            if (field == null) return null;

            Object value = field.get(obj);
            if (value instanceof Double) return (Double) value;
            if (value instanceof BigDecimal) return ((BigDecimal) value).doubleValue();
            if (value instanceof Number) return ((Number) value).doubleValue();
            return null;
        } catch (IllegalAccessException e) {
            log.debug("反射访问字段 {} 失败: {}", fieldName, e.getMessage());
            return null;
        }
    }

    /**
     * 检查指标是否连续越界
     * @param readings           读数列表
     * @param range              阈值范围
     * @param requiredConsecutive 需要连续越界的次数
     * @return true 表示触发告警
     */
    public static boolean checkMetricConsecutiveOutOfRange(List<energyReadings> readings,
                                                           ThresholdRange range,
                                                           int requiredConsecutive) {
        int consecutiveCount = 0;
        for (energyReadings reading : readings) {
            Double actualValue = getFieldValueByName(reading, range.getMetricName());
            if (actualValue == null) {
                consecutiveCount = 0;
                continue;
            }

            boolean outOfRange = false;
            if (range.getMinValue() != null && actualValue < range.getMinValue().doubleValue()) {
                outOfRange = true;
            }
            if (range.getMaxValue() != null && actualValue > range.getMaxValue().doubleValue()) {
                outOfRange = true;
            }

            if (outOfRange) {
                consecutiveCount++;
                if (consecutiveCount >= requiredConsecutive) {
                    return true;
                }
            } else {
                consecutiveCount = 0;
            }
        }
        return false;
    }

    /**
     * 判断告警类型（低于下限 / 超出上限）
     */
    public static String determineAlertType(Double actualValue, ThresholdRange range) {
        if (range.getMinValue() != null && actualValue < range.getMinValue().doubleValue()) {
            return "BELOW_MIN";
        }
        if (range.getMaxValue() != null && actualValue > range.getMaxValue().doubleValue()) {
            return "ABOVE_MAX";
        }
        return "UNKNOWN";
    }

    /**
     * 计算告警等级（1-3）
     */
    public static Integer calculateAlertLevel(Double actualValue, ThresholdRange range) {
        if (range.getMaxValue() != null && actualValue > range.getMaxValue().doubleValue()) {
            double exceedPercent = (actualValue - range.getMaxValue().doubleValue()) / range.getMaxValue().doubleValue();
            if (exceedPercent >= 0.2) return 3;
            if (exceedPercent >= 0.1) return 2;
            return 1;
        }
        if (range.getMinValue() != null && actualValue < range.getMinValue().doubleValue()) {
            double deficitPercent = (range.getMinValue().doubleValue() - actualValue) / range.getMinValue().doubleValue();
            if (deficitPercent >= 0.2) return 3;
            if (deficitPercent >= 0.1) return 2;
            return 1;
        }
        return 1;
    }

    /**
     * 匹配阈值规则（设备级 > 建筑级 > 全局）
     */
    public static List<ThresholdRange> findThresholds(Map<String, List<ThresholdRange>> thresholdMap,
                                                       Integer deviceId, Integer buildingId) {
        List<ThresholdRange> thresholds = thresholdMap.get(deviceId + "_" + buildingId);
        if (thresholds == null || thresholds.isEmpty()) {
            thresholds = thresholdMap.get("null_" + buildingId);
        }
        if (thresholds == null || thresholds.isEmpty()) {
            thresholds = thresholdMap.get("null_null");
        }
        return thresholds;
    }

    /**
     * 下划线转驼峰
     */
    public static String underscoreToCamel(String underscore) {
        if (underscore == null || underscore.isEmpty()) return underscore;
        StringBuilder result = new StringBuilder();
        boolean nextUpperCase = false;
        for (char c : underscore.toCharArray()) {
            if (c == '_') {
                nextUpperCase = true;
            } else {
                result.append(nextUpperCase ? Character.toUpperCase(c) : c);
                nextUpperCase = false;
            }
        }
        return result.toString();
    }
}
