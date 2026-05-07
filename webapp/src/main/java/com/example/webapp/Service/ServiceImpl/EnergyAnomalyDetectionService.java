package com.example.webapp.Service.ServiceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import com.example.webapp.Entity.BuildingDeviceId;
import com.example.webapp.Entity.Devices;
import com.example.webapp.Entity.customer;
import com.example.webapp.Entity.energyReadings;
import com.example.webapp.Mapper.BuildingMapper;
import com.example.webapp.Mapper.DeviceMapper;
import com.example.webapp.Mapper.energyMapper;
import com.example.webapp.Service.LoginService;
import com.example.webapp.Util.LoginCustomerHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 能耗异常检测服务
 * 用户登录后异步检测设备最新能耗数据是否超出阈值，并更新设备状态
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnergyAnomalyDetectionService {

    private final ThresholdRangeService thresholdRangeService;
    private final DeviceMapper deviceMapper;
    private final energyMapper energyReadingsMapper;
    @Autowired
    private LoginService loginService;
    @Autowired
    private BuildingMapper buildingMapper;

    // 需要检测的指标（与 threshold_range 表中的 metric_name 一致）
    private static final List<String> METRICS = List.of(
            "power_consumption",
            "water_consumption",
            "ac_power_consumption"
    );

    /**
     * 异步检查当前登录用户的设备最新能耗是否异常，并更新设备状态
     */
    @Async
    public void checkAndUpdateDeviceStatus() {
        // 1. 获取当前登录用户（获取 buildingId 和 deviceId 用于阈值查询）
        Long id = LoginCustomerHolder.getLoginCustomer().getId();
        customer user = loginService.getById(id);
        if (user == null) {
            log.warn("未获取到登录用户信息，跳过能耗检查");
            return;
        }
        BuildingDeviceId id1 = buildingMapper.getId(user.getDeviceCode(), user.getBuildingCode());
        int buildingId = id1.getBuildingId();
        int deviceId = id1.getDeviceId();

        if (buildingId == 0 || deviceId == 0) {
            log.warn("用户未绑定 buildingId 或 deviceId，跳过检查: buildingCode={}, deviceCode={}", buildingId, deviceId);
            return;
        }

        // 2. 获取设备最新一条能耗数据
        energyReadings latestReading = getLatestReading(deviceId, buildingId);
        if (latestReading == null) {
            log.info("设备 {} 无能耗数据，跳过检查", deviceId);
            return;
        }

        // 3. 提取各指标值
        BigDecimal power = BigDecimal.valueOf(latestReading.getPowerConsumption());
        BigDecimal water = BigDecimal.valueOf(latestReading.getWaterConsumption());
        BigDecimal acPower = BigDecimal.valueOf(latestReading.getAcPowerConsumption());
        LocalDateTime monitoringTime = latestReading.getMonitoringTime();

        log.debug("设备 {} 最新能耗数据: 时间={}, 用电={}, 用水={}, 空调用电={}",
                deviceId, monitoringTime, power, water, acPower);

        // 4. 逐指标判断是否超出阈值，确定新状态
        String newStatus = determineStatusByThresholds(buildingId, deviceId, power, water, acPower);

        // 5. 如果状态发生变化，更新设备表
        Devices device = getDeviceByCode(deviceId);
        if (device == null) {
            log.warn("设备不存在: deviceCode={}", deviceId);
            return;
        }

        if (!newStatus.equals(device.getDeviceStatus())) {
            updateDeviceStatus(deviceId, newStatus);
            log.info("设备 {} 能耗检查完成，状态从 {} 更新为 {} (最新数据时间: {})",
                    deviceId, device.getDeviceStatus(), newStatus, monitoringTime);
        } else {
            log.debug("设备 {} 状态未变化，仍为 {}", deviceId, newStatus);
        }
    }

    /**
     * 根据阈值判断设备应处于的状态
     * @param buildingId 建筑ID
     * @param deviceId   设备ID
     * @param power      用电量
     * @param water      用水量
     * @param acPower    空调用电量
     * @return normal / warning / critical
     */
    private String determineStatusByThresholds(Integer buildingId, Integer deviceId,
                                               BigDecimal power, BigDecimal water, BigDecimal acPower) {
        String status = "normal";
        LocalDateTime now = LocalDateTime.now();

        // 检查用电指标
        ThresholdRangeService.Range powerRange = thresholdRangeService.getRange(buildingId, deviceId, "power_consumption", now);
        if (powerRange != null) {
            if (powerRange.isAboveMax(power)) {
                return "critical";  // 一旦超出上限，直接严重异常
            } else if (powerRange.isBelowMin(power)) {
                status = "warning";
            }
        }

        // 检查用水指标
        ThresholdRangeService.Range waterRange = thresholdRangeService.getRange(buildingId, deviceId, "water_consumption", now);
        if (waterRange != null) {
            if (waterRange.isAboveMax(water)) {
                return "critical";
            } else if (waterRange.isBelowMin(water)) {
                status = "warning";
            }
        }

        // 检查空调用电指标
        ThresholdRangeService.Range acPowerRange = thresholdRangeService.getRange(buildingId, deviceId, "ac_power_consumption", now);
        if (acPowerRange != null) {
            if (acPowerRange.isAboveMax(acPower)) {
                return "critical";
            } else if (acPowerRange.isBelowMin(acPower)) {
                status = "warning";
            }
        }

        return status;
    }

    /**
     * 获取设备最新一条能耗记录
     */
    private energyReadings getLatestReading(int deviceId, int buildingId) {

        LambdaQueryWrapper<energyReadings> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(energyReadings::getDeviceId,deviceId )
                .eq(energyReadings::getBuildingId,buildingId )
                .orderByDesc(energyReadings::getMonitoringTime)
                .last("LIMIT 1");
        return energyReadingsMapper.selectOne(wrapper);
    }

    /**
     * 根据设备编号查询设备
     */
    private Devices getDeviceByCode(int deviceId) {
        LambdaQueryWrapper<Devices> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Devices::getDeviceCode, deviceId);
        return deviceMapper.selectOne(wrapper);
    }

    /**
     * 更新设备状态
     */
    @Transactional
    public void updateDeviceStatus(int deviceId, String newStatus) {
        LambdaUpdateWrapper<Devices> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Devices::getDeviceCode, deviceId)
                .set(Devices::getDeviceStatus, newStatus);
        deviceMapper.update(null, wrapper);
    }
}