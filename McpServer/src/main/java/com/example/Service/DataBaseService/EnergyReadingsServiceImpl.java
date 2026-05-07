package com.example.Service.DataBaseService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.Entity.*;
import com.example.Entity.EnergyEntity.AcPowerTrendVO;
import com.example.Entity.EnergyEntity.EnergyTrendVO;
import com.example.Entity.EnergyEntity.WaterFlowRateVo;
import com.example.Entity.EnergyEntity.WaterTrendVO;
import com.example.Mapper.EnergyReadingsMapper;
import com.example.Repository.BloomFilterHelper;
import com.example.Service.EnergyReadingsService;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.example.Repository.BloomFilterHelper.*;

@Service
public class EnergyReadingsServiceImpl extends ServiceImpl<EnergyReadingsMapper, energyReadings> implements EnergyReadingsService {

    @Autowired
    EnergyReadingsMapper energyReadingsMapper;

    @Autowired
    private BloomFilterHelper bloomFilterHelper;

    //根据建筑id和指定时间段查询能耗和环境变量

    @Override
    public List<energyReadings> queryEnergyconsumptionbybuilding(int buildingId,
                                                                 LocalDateTime singleTime,
                                                                 LocalDateTime startTime,
                                                                 LocalDateTime endTime) {
        return energyReadingsMapper.queryEnergyconsumptionbybuildingId(buildingId,singleTime,startTime,endTime);
    }


    //根据建筑类型查询能耗和环境变量
    @Override
    public List<energyReadings> queryEnergyconsumptionbybuilding(String buildingType,
                                                                 LocalDateTime singleTime,
                                                                 LocalDateTime startTime,
                                                                 LocalDateTime endTime) {
        return energyReadingsMapper.queryEnergyconsumptionbybuildingType(buildingType,singleTime,startTime,endTime);
    }

    //根据建筑编号查询能耗和环境变量
    @Override
    public List<energyReadings> queryEnergyconsumptionbyBuildingcode(String buildingcode,
                                                                    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime singleTime,
                                                                    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
                                                                     @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")LocalDateTime endTime) {
        return energyReadingsMapper.queryEnergyconsumptionbyBuildingcode(buildingcode,singleTime,startTime,endTime);
    }

    //根据建筑名称查询能耗和环境变量
    @Override
    public List<energyReadings> queryEnergyconsumptionbyBuildingname(String buildingname,
                                                                    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime singleTime,
                                                                     @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")LocalDateTime startTime,
                                                                    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return energyReadingsMapper.queryEnergyconsumptionbyBuildingname(buildingname,singleTime,startTime,endTime);
    }

    //查询设备能耗和设备名字以及设备运行状态
    @Override
    public List<DeviceEnergyBuildingVO> getqueryEnergyconsumption(int devicesId,
                                                                 @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime singleTime,
                                                                 @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
                                                                 @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,Integer randomCount) {
        return energyReadingsMapper.queryEnergyconsumption(devicesId,singleTime,startTime,endTime,randomCount);
    }



    //根据设备id查询设备能耗和环境变量
    @Override
    public List<energyReadings> queryConsumptionByDevicesId(int devicesId,
                                                            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")LocalDateTime startTime,
                                                           @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return energyReadingsMapper.queryEnergyconsumptionbyDeviceId(devicesId,startTime,endTime);
    }

    @Override
    public Double getEnvTempByTime(@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")LocalDateTime time) {
        return energyReadingsMapper.getEnvTempByTime(time);
    }

    @Override
    public Double getOccupancyByTime(@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")LocalDateTime time) {
        return energyReadingsMapper.getOccupancyByTime(time);
    }


    @Cacheable(value = "energyStats",
            key = "'power:year:' + (#Year != null ? #Year : 'current')",
            unless = "#result == null")
    @Override
    public BigDecimal sumPowerByYear(Integer Year) {
        if(Year==null){
            Year=LocalDate.now().getYear();
        }
        return energyReadingsMapper.sumPowerByYear(Year);
    }

    @Cacheable(value = "energyStats", key = "'power:total'", unless = "#result == null")
    @Override
    public Double sumPower() {
        return energyReadingsMapper.sumPower();
    }



    @Cacheable(value = "energyStats", key = "'water:total'", unless = "#result == null")
    @Override
    public BigDecimal sumWaterByYear(String startTime, String endTime) {

        return energyReadingsMapper.sumWaterConsumption(startTime, endTime);
    }


    @Cacheable(value = "energyStats", key = "'water:year'", unless = "#result == null")
    @Override
    public Double sumWater() {
        return energyReadingsMapper.sumWater();
    }

    @Override
    public List<energyReadings> selectByTime(int devicesId, LocalDateTime startTime, LocalDateTime endTime) {
        return energyReadingsMapper.selectByTime(devicesId,startTime,endTime);
    }

    @Override
    public List<energyReadings> selectByTime(int devicesId, LocalDateTime monthTime) {
        return energyReadingsMapper.selectByOnlyTime(devicesId,monthTime);
    }

    //获取设备运行趋势
    @Cacheable(value = "energyTrend",
            key = "'ac:' + (#buildingId != null ? #buildingId : 'all') + ':' + (#deviceId != null ? #deviceId : 'none')",
            unless = "#result == null ")
    @Override
    public List<AcPowerTrendVO> getAcPowerTrend(Integer buildingId, Integer deviceId) {
        if (buildingId != null && !bloomFilterHelper.mightContain(BLOOM_FILTER_BUILDING, buildingId)) {
            return null;
        }
        // 仅当 deviceId 不为空时，检查其是否存在
        if (deviceId != null && !bloomFilterHelper.mightContain(BLOOM_FILTER_DEVICE, deviceId)) {
            return null;
        }
        return energyReadingsMapper.getAcPowerTrend(buildingId,deviceId);
    }
    @Cacheable(value = "energyTrend",
            key = "'water:' + (#buildingId != null ? #buildingId : 'all') + ':' + (#deviceId != null ? #deviceId : 'none')",
            unless = "#result == null ")
    @Override
    public List<WaterTrendVO> getWaterTrend(Integer buildingId, Integer deviceId) {
        if (buildingId != null && !bloomFilterHelper.mightContain(BLOOM_FILTER_BUILDING, buildingId)) {
            return null;
        }
        // 仅当 deviceId 不为空时，检查其是否存在
        if (deviceId != null && !bloomFilterHelper.mightContain(BLOOM_FILTER_DEVICE, deviceId)) {
            return null;
        }
        return energyReadingsMapper.getWaterTrend(buildingId,deviceId);
    }
    @Cacheable(value = "energyTrend",
            key = "'energy:' + (#buildingId != null ? #buildingId : 'all') + ':' + (#deviceId != null ? #deviceId : 'none')",
            unless = "#result == null ")
    @Override
    public List<EnergyTrendVO> getEnergyTrend(Integer buildingId, Integer deviceId) {
        if (buildingId != null && !bloomFilterHelper.mightContain(BLOOM_FILTER_BUILDING, buildingId)) {
            return null;
        }
        // 仅当 deviceId 不为空时，检查其是否存在
        if (deviceId != null && !bloomFilterHelper.mightContain(BLOOM_FILTER_DEVICE, deviceId)) {
            return null;
        }
        return energyReadingsMapper.getEnergyTrend(buildingId,deviceId);
    }

    @Cacheable(value = "energyTrend",
            key = "'waterflow:' + (#buildingId != null ? #buildingId : 'all') + ':' + (#deviceId != null ? #deviceId : 'none')",
            unless = "#result == null ")
    @Override
    public List<WaterFlowRateVo> getWaterFlowRate(Integer buildingId, Integer deviceId) {
        if (buildingId != null && !bloomFilterHelper.mightContain(BLOOM_FILTER_BUILDING, buildingId)) {
            return null;
        }
        // 仅当 deviceId 不为空时，检查其是否存在
        if (deviceId != null && !bloomFilterHelper.mightContain(BLOOM_FILTER_DEVICE, deviceId)) {
            return null;
        }
        return energyReadingsMapper.getWaterFlow(buildingId,deviceId);
    }

    @Cacheable(value = "energyTrend",
            key = "'env:' + (#buildingId != null ? #buildingId : 'all') + ':' + (#deviceId != null ? #deviceId : 'none')",
            unless = "#result == null")
    @Override
    public Double AvgEnv(Integer buildingId, Integer deviceId) {
        return energyReadingsMapper.getEnvTempLatest(buildingId,deviceId);
    }

    @Cacheable(value = "energyTrend",
            key = "'humidity:' + (#buildingId != null ? #buildingId : 'all') + ':' + (#deviceId != null ? #deviceId : 'none')",
            unless = "#result == null ")
    @Override
    public Double AvgHumidity(Integer buildingId, Integer deviceId) {
        return energyReadingsMapper.gethumidityLatest(buildingId,deviceId);
    }

    //获取超出阈值设备
    @Override
    public Page<AlertVo> QueryErrorEnergy(int current, int size,   @RequestParam(required = false) Integer buildingId,
                                           @RequestParam(required = false) String metricName,
                                           @RequestParam(required = false) Integer deviceId,  @RequestParam(required = false) Integer status,
                                           @RequestParam(required = false) AlertRecord.AlertType alertType,@RequestParam(required = false) LocalDateTime startTime,
                                           @RequestParam(required = false) LocalDateTime endTime) {
        Page<AlertVo> page = new Page(current, size);
        return energyReadingsMapper.selectAbnormalBuildingDeviceByPage(page, buildingId,metricName, deviceId, status, alertType, startTime, endTime);
    }

    @Override
    public List<BuildingEnergyVo> QueryBuildingEnergy() {
        return energyReadingsMapper.getBuildingEnergyStatistics();
    }

    @Override
    public List<DeviceLatestCompareVO> QueryDeviceLatestCompare() {
        return energyReadingsMapper.getLatestWithPreviousComparison();
    }
}
