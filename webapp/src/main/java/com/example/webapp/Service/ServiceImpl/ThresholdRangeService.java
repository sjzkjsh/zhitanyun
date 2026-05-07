package com.example.webapp.Service.ServiceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.webapp.Entity.ThresholdRange;
import com.example.webapp.Mapper.ThresholdRangeMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThresholdRangeService extends ServiceImpl<ThresholdRangeMapper, ThresholdRange> {

    /**
     * 获取阈值范围（带缓存）
     * 优先级：设备级 > 建筑级 > 全局默认
     */
    @Cacheable(value = "thresholdRange", key = "#buildingId + ':' + #deviceId + ':' + #metricName + ':' + #time.toLocalDate()")
    public Range getRange(Integer buildingId, Integer deviceId, String metricName, LocalDateTime time) {
        // 参数校验
        if (metricName == null || time == null) {
            log.warn("获取阈值失败：metricName或time为空");
            return null;
        }

        // 按优先级分层查询：先查最精确的
        ThresholdRange range = queryByPriority(buildingId, deviceId, metricName, time);

        if (range == null) {
            log.debug("未找到阈值配置: buildingId={}, deviceId={}, metric={}, time={}",
                    buildingId, deviceId, metricName, time);
            return null;
        }

        return new Range(range.getMinValue(), range.getMaxValue(), range.getUnit(),
                range.getId(), range.getEffectiveFrom(), range.getEffectiveTo());
    }

    /**
     * 批量获取多个指标的阈值（提升性能）
     */
    public List<Range> getRanges(Integer buildingId, Integer deviceId,
                                 List<String> metricNames, LocalDateTime time) {
        return metricNames.stream()
                .map(metric -> getRange(buildingId, deviceId, metric, time))
                .filter(Objects::nonNull)
                .toList();
    }

    // ==================== 核心查询逻辑 ====================

    /**
     * 优先级查询：设备级 > 建筑级 > 全局默认
     */
    private ThresholdRange queryByPriority(Integer buildingId, Integer deviceId,
                                           String metricName, LocalDateTime time) {
        // 1. 设备级（最精确）
        if (deviceId != null) {
            ThresholdRange deviceRange = queryExactRange(deviceId, buildingId, metricName, time);
            if (deviceRange != null) {
                log.debug("命中设备级阈值: deviceId={}, metric={}", deviceId, metricName);
                return deviceRange;
            }
        }

        // 2. 建筑级
        if (buildingId != null) {
            ThresholdRange buildingRange = queryExactRange(null, buildingId, metricName, time);
            if (buildingRange != null) {
                log.debug("命中建筑级阈值: buildingId={}, metric={}", buildingId, metricName);
                return buildingRange;
            }
        }

        // 3. 全局默认
        ThresholdRange globalRange = queryExactRange(null, null, metricName, time);
        if (globalRange != null) {
            log.debug("命中全局阈值: metric={}", metricName);
        }
        return globalRange;
    }

    /**
     * 精确查询：必须完全匹配 deviceId 和 buildingId（包括null）
     */
    private ThresholdRange queryExactRange(Integer deviceId, Integer buildingId,
                                           String metricName, LocalDateTime time) {
        LambdaQueryWrapper<ThresholdRange> wrapper = new LambdaQueryWrapper<>();

        // 必须完全匹配这些条件（包括null）
        wrapper.eq(deviceId != null, ThresholdRange::getDeviceId, deviceId)
                .isNull(deviceId == null, ThresholdRange::getDeviceId)
                .eq(buildingId != null, ThresholdRange::getBuildingId, buildingId)
                .isNull(buildingId == null, ThresholdRange::getBuildingId)
                .eq(ThresholdRange::getMetricName, metricName)
                // 时间有效性：生效时间 <= 当前时间 <= 失效时间
                .le(ThresholdRange::getEffectiveFrom, time)
                .and(w -> w.ge(ThresholdRange::getEffectiveTo, time)
                        .or().isNull(ThresholdRange::getEffectiveTo))
                // 按更新时间倒序，取最新配置的
                .orderByDesc(ThresholdRange::getUpdatedAt)
                .last("LIMIT 1");

        return getOne(wrapper);
    }

    // ==================== 内部类：Range ====================

    @Getter
    public static class Range {
        private final BigDecimal min;
        private final BigDecimal max;
        private final String unit;
        private final Long configId;        // 配置ID，用于追溯
        private final LocalDateTime effectiveFrom;
        private final LocalDateTime effectiveTo;

        public Range(BigDecimal min, BigDecimal max, String unit) {
            this(min, max, unit, null, null, null);
        }

        public Range(BigDecimal min, BigDecimal max, String unit,
                     Long configId, LocalDateTime effectiveFrom, LocalDateTime effectiveTo) {
            this.min = min;
            this.max = max;
            this.unit = unit;
            this.configId = configId;
            this.effectiveFrom = effectiveFrom;
            this.effectiveTo = effectiveTo;
        }

        /**
         * 检查值是否在范围内（包含边界）
         */
        public boolean contains(BigDecimal value) {
            if (value == null || min == null || max == null) {
                return false;
            }
            return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
        }

        /**
         * 检查值是否超出上限
         */
        public boolean isAboveMax(BigDecimal value) {
            return value != null && max != null && value.compareTo(max) > 0;
        }

        /**
         * 检查值是否低于下限
         */
        public boolean isBelowMin(BigDecimal value) {
            return value != null && min != null && value.compareTo(min) < 0;
        }

        /**
         * 获取偏差程度（用于异常严重程度评估）
         * @return 偏差百分比，如 1.2 表示超出20%
         */
        public double getDeviationRatio(BigDecimal value) {
            if (value == null || max == null || min == null || max.equals(min)) {
                return 0;
            }
            BigDecimal mid = min.add(max).divide(BigDecimal.valueOf(2), 4, BigDecimal.ROUND_HALF_UP);
            BigDecimal range = max.subtract(min).divide(BigDecimal.valueOf(2), 4, BigDecimal.ROUND_HALF_UP);
            return value.subtract(mid).abs().divide(range, 4, BigDecimal.ROUND_HALF_UP).doubleValue();
        }

        @Override
        public String toString() {
            return String.format("Range[%s, %s %s]", min, max, unit);
        }
    }
}