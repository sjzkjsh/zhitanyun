package com.example.Service.PieService;

import com.example.Entity.PieEntity.BuildingEnergy;
import com.example.Entity.PieEntity.DeviceDetailVO;
import com.example.Entity.PieEntity.DeviceEnergy;
import com.example.Entity.PieEntity.EnergyPieResult;
import com.example.Mapper.EnergyReadingsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PieEnergyService {
    @Autowired
    private EnergyReadingsMapper energyReadingsMapper;


    public EnergyPieResult getEnergyPie(Integer buildingId, Integer deviceId) {
        // 第三层：设备三能耗占比（建筑ID + 设备ID）
        if (buildingId != null && deviceId != null) {
            return getDeviceDetailPie(buildingId, deviceId);
        }

        // 第二层：建筑内设备占比（只有建筑ID）
        if (buildingId != null) {
            return getDevicePieByBuilding(buildingId);
        }

        // 第一层：全部建筑占比（无参数）
        return getBuildingPie();
    }

    // ========== 第一层：全部建筑占比 ==========
    private EnergyPieResult getBuildingPie() {
        EnergyPieResult result = new EnergyPieResult();
        result.setLevel("building");
        result.setTitle("各建筑能耗占比");

        // 查询所有建筑能耗
        List<BuildingEnergy> list = energyReadingsMapper.queryBuildingEnergy();

        // 转换为扇形图数据
        List<EnergyPieResult.PieItem> pieData = list.stream()
                .filter(b -> b.getValue() != null && b.getValue().compareTo(BigDecimal.ZERO) > 0)
                .map(b -> {
                    EnergyPieResult.PieItem item = new EnergyPieResult.PieItem();
                    item.setId(b.getBuildingCode());
                    item.setName(b.getBuildingName());
                    item.setValue(b.getValue());
                    return item;
                })
                .collect(Collectors.toList());

        // 计算百分比
        calcPercent(pieData);
        result.setPieData(pieData);

        return result;
    }

    // ========== 第二层：建筑内设备占比 ==========
    private EnergyPieResult getDevicePieByBuilding(Integer buildingId) {
        EnergyPieResult result = new EnergyPieResult();
        result.setLevel("device");

        // 查询建筑名称
        String buildingName = energyReadingsMapper.selectBuildingName(buildingId);
        result.setTitle(buildingName + " - 设备能耗占比");

        // 查询该建筑下所有设备能耗
        List<DeviceEnergy> list = energyReadingsMapper.queryDeviceEnergy(buildingId);

        // 转换为扇形图数据
        List<EnergyPieResult.PieItem> pieData = list.stream()
                .filter(d -> d.getValue() != null && d.getValue().compareTo(BigDecimal.ZERO) > 0)
                .map(d -> {
                    EnergyPieResult.PieItem item = new EnergyPieResult.PieItem();
                    item.setId(d.getDeviceCode());
                    item.setName(d.getDeviceType());
                    item.setValue(d.getValue());
                    return item;
                })
                .collect(Collectors.toList());

        // 计算百分比
        calcPercent(pieData);
        result.setPieData(pieData);

        return result;
    }

    // ========== 第三层：设备三能耗占比 ==========
    private EnergyPieResult getDeviceDetailPie(Integer buildingId, Integer deviceId) {
        EnergyPieResult result = new EnergyPieResult();
        result.setLevel("detail");

        // 查询设备三能耗详情
        DeviceDetailVO detail = energyReadingsMapper.selectDeviceDetail(buildingId, deviceId);

        // 查询建筑名称
        String buildingName = energyReadingsMapper.selectBuildingName(buildingId);

        // 设置标题
        String deviceType = detail.getDeviceType() != null ? detail.getDeviceType() : "未知设备";
        result.setTitle(buildingName + " - " + deviceType + " 能耗构成");

        // 组装三能耗数据（水耗、电耗、空调系统能耗）
        List<EnergyPieResult.PieItem> pieData = Arrays.asList(
                createPieItem("水耗", detail.getWaterConsumption()),
                createPieItem("电耗", detail.getPowerConsumption()),
                createPieItem("空调系统能耗", detail.getAcPowerConsumption())
        );

        // 过滤掉值为null或0的项（可选，如果需要显示全部则去掉filter）
        pieData = pieData.stream()
                .filter(item -> item.getValue().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());

        // 计算百分比
        calcPercent(pieData);
        result.setPieData(pieData);

        return result;
    }

    // ========== 工具方法 ==========

    /**
     * 创建扇形图单项
     */
    private EnergyPieResult.PieItem createPieItem(String name, BigDecimal value) {
        EnergyPieResult.PieItem item = new EnergyPieResult.PieItem();
        item.setId(name);
        item.setName(name);
        item.setValue(value != null ? value : BigDecimal.ZERO);
        return item;
    }

    /**
     * 计算百分比
     */
    private void calcPercent(List<EnergyPieResult.PieItem> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        // 计算总和
        BigDecimal total = list.stream()
                .map(EnergyPieResult.PieItem::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 计算每项百分比
        for (EnergyPieResult.PieItem item : list) {
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                double percent = item.getValue()
                        .multiply(new BigDecimal("100"))
                        .divide(total, 2, RoundingMode.HALF_UP)
                        .doubleValue();
                item.setPercent(percent);
            } else {
                item.setPercent(0.0);
            }
        }
    }
}
