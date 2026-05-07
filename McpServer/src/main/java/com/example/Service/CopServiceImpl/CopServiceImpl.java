package com.example.Service.CopServiceImpl;

import com.example.Entity.CopEntity.CopResult;
import com.example.Entity.energyReadings;
import com.example.Service.CopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class CopServiceImpl implements CopService {

    // 水的比热容：1.163 Wh/(kg·℃) = 0.001163 kWh/(kg·℃)
    private static final double WATER_SPECIFIC_HEAT = 1.163 / 1000.0;

    // 采样间隔（小时），您之前生成的数据为 15 分钟 → 0.25 小时
    private static final double INTERVAL_HOURS = 0.25;

    /**
     * 支持单个 energyReadings 对象计算 COP（便于测试）
     */
    public CopResult CopCompute(energyReadings reading) {
        if (reading == null) {
            log.warn("传入的 reading 为 null");
            return emptyResult();
        }
        return CopCompute(Collections.singletonList(reading));
    }

    /**
     * 支持列表计算 COP
     */
    @Override
    public CopResult CopCompute(List<energyReadings> readings) {
        if (readings == null || readings.isEmpty()) {
            log.warn("传入的 readings 列表为空");
            return emptyResult();
        }

        double totalCooling = 0.0;
        double totalPower = 0.0;
        int validPoints = 0;

        for (energyReadings r : readings) {
            if (!isValidReading(r)) {
                log.debug("无效记录被过滤：power={}, flow={}, inlet={}, outlet={}",
                        r.getAcPower(), r.getWaterFlowRate(), r.getAcInletTemp(), r.getAcOutletTemp());
                continue;
            }

            double deltaT = r.getAcInletTemp() - r.getAcOutletTemp();
            double flowRate = r.getWaterFlowRate();   // 假设单位：m³/h
            double power = r.getAcPower();            // 单位：kW

            // 单条记录耗电量 = 功率 × 时间间隔
            totalPower += power * INTERVAL_HOURS;

            // 单条记录制冷量 = c × 水流量 × 温差 × 时间间隔
            // 注意：若 flowRate 单位为 m³/h，需乘以 1000 转换为 kg/h
            totalCooling += WATER_SPECIFIC_HEAT * flowRate * 1000 * deltaT * INTERVAL_HOURS;

            validPoints++;
        }

        log.info("COP计算完成：有效记录数={}, 总制冷量={} kWh, 总耗电量={} kWh",
                validPoints, totalCooling, totalPower);

        if (totalPower == 0 || validPoints == 0) {
            return emptyResult();
        }

        CopResult result = new CopResult();
        result.setAverageCop(totalCooling / totalPower);
        result.setTotalCooling(totalCooling);
        result.setTotalPower(totalPower);
        result.setValidPoints(validPoints);
        return result;
    }

    /**
     * 修正后的有效性校验：只检查实际参与计算的字段
     */
    private boolean isValidReading(energyReadings r) {
        // Double 类型可能为 null，必须先判空再比较
        return r.getAcInletTemp() != 0 && r.getAcInletTemp() > 0 &&
                r.getAcOutletTemp() != 0 && r.getAcOutletTemp() > 0 &&
                r.getAcPower() != 0 && r.getAcPower() > 0 &&
                r.getWaterFlowRate() != 0 && r.getWaterFlowRate() > 0;
    }

    private CopResult emptyResult() {
        CopResult result = new CopResult();
        result.setAverageCop(0.0);
        result.setTotalCooling(0.0);
        result.setTotalPower(0.0);
        result.setValidPoints(0);
        return result;
    }
}