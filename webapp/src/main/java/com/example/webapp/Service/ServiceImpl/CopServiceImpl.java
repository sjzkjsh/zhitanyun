package com.example.webapp.Service.ServiceImpl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.webapp.Entity.BuildingDeviceId;
import com.example.webapp.Entity.InstantCopResult;
import com.example.webapp.Entity.LatestEnergyDTO;
import com.example.webapp.Entity.customer;
import com.example.webapp.Mapper.BuildingMapper;
import com.example.webapp.Mapper.CustomerMapper;
import com.example.webapp.Mapper.energyMapper;
import com.example.webapp.Util.LoginCustomerHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CopServiceImpl {

    private final energyMapper energyMapper;
    @Autowired
    private BuildingMapper buildingMapper;

    @Autowired
    private CustomerMapper customerMapper;
    // 水的比热容 (kWh/(kg·℃))
    private static final double SPECIFIC_HEAT_KWH_PER_KG_C = 0.001163;
    // 水的密度 (kg/m³)，当流量单位是 m³/h 时使用
    private static final double WATER_DENSITY_KG_PER_M3 = 1000.0;


    public InstantCopResult calculateInstantCop() {
        Long id = LoginCustomerHolder.getLoginCustomer().getId();
        LambdaQueryWrapper<customer> eq = new LambdaQueryWrapper<customer>().eq(customer::getId, id);
        customer customer = customerMapper.selectOne(eq);
        BuildingDeviceId id1 = buildingMapper.getId(customer.getDeviceCode(), customer.getBuildingCode());
        int buildingId = id1.getBuildingId();
        int deviceId = id1.getDeviceId();
        // 1. 获取最新一条数据
        LatestEnergyDTO latest = energyMapper.getLatestEnergyFields(buildingId, deviceId);

        InstantCopResult result = new InstantCopResult();
        if (latest == null) {
            result.setValid(false);
            result.setMessage("未找到该设备的最新能耗数据");
            return result;
        }

        // 2. 校验有效性
        if (!isDataValid(latest)) {
            result.setValid(false);
            result.setMessage("数据不完整或无效，无法计算 COP");
            fillBasicInfo(result, latest);
            return result;
        }

        // 3. 计算
        double deltaT = latest.getAcInletTemp() - latest.getAcOutletTemp();
        // 假设 waterFlowRate 单位是 m³/h，转为 kg/h
        double massFlowRate = latest.getWaterFlowRate() * WATER_DENSITY_KG_PER_M3;
        double coolingCapacity = SPECIFIC_HEAT_KWH_PER_KG_C * massFlowRate * deltaT;
        double power = latest.getAcPower();
        double cop = coolingCapacity / power;

        // 4. 填充结果
        result.setCop(cop);
        result.setCoolingCapacity(coolingCapacity);
        result.setPowerConsumption(power);
        result.setDeltaT(deltaT);
        result.setWaterFlowRate(latest.getWaterFlowRate());
        result.setValid(true);
        result.setMessage("计算成功");

        log.info("瞬时COP计算: buildingId={}, deviceId={}, COP={}", buildingId, deviceId, cop);
        return result;
    }

    private boolean isDataValid(LatestEnergyDTO dto) {
        if (dto.getAcInletTemp() == null || dto.getAcOutletTemp() == null) return false;
        if (dto.getWaterFlowRate() == null || dto.getWaterFlowRate() <= 0) return false;
        if (dto.getAcPowerConsumption() == null || dto.getAcPowerConsumption() <= 0) return false;
        double deltaT = dto.getAcInletTemp() - dto.getAcOutletTemp();
        return deltaT > 0;
    }

    private void fillBasicInfo(InstantCopResult result, LatestEnergyDTO latest) {
        if (latest.getAcInletTemp() != null && latest.getAcOutletTemp() != null) {
            result.setDeltaT(latest.getAcInletTemp() - latest.getAcOutletTemp());
        }
        result.setWaterFlowRate(latest.getWaterFlowRate());
        result.setPowerConsumption(latest.getAcPowerConsumption());
    }
}