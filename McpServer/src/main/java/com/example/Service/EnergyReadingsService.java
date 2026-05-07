package com.example.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.Entity.*;
import com.example.Entity.EnergyEntity.AcPowerTrendVO;
import com.example.Entity.EnergyEntity.EnergyTrendVO;
import com.example.Entity.EnergyEntity.WaterFlowRateVo;
import com.example.Entity.EnergyEntity.WaterTrendVO;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface EnergyReadingsService extends IService<energyReadings> {


    //根据建筑id和指定时间段查询能耗和环境变量
    List<energyReadings> queryEnergyconsumptionbybuilding(int buildingId,@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")LocalDateTime singleTime, @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")LocalDateTime startTime, @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")LocalDateTime endTime);
@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    //根据设备type查询能耗
    List<energyReadings> queryEnergyconsumptionbybuilding(String buildingType,@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")LocalDateTime singleTime,@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime, @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")LocalDateTime endTime);

    ////根据建筑编号查询能耗和环境变量
    List<energyReadings> queryEnergyconsumptionbyBuildingcode(String buildingcode,@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")LocalDateTime singleTime, @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")LocalDateTime startTime,@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime);

    //根据建筑名称查询能耗和环境变量
    List<energyReadings> queryEnergyconsumptionbyBuildingname(String buildingname,@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")LocalDateTime singleTime, @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")LocalDateTime startTime,@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime);


    //查询设备能耗和设备名字以及设备运行状态
    List<DeviceEnergyBuildingVO> getqueryEnergyconsumption(int devicesId,@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")LocalDateTime singleTime,@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,Integer randomCount);


    //根据设备id查询能耗
    List<energyReadings> queryConsumptionByDevicesId(int devicesId,@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime, @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")LocalDateTime endTime);

    Double getEnvTempByTime(@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")LocalDateTime time);
    Double getOccupancyByTime(@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")LocalDateTime time);
    //查询电力能耗的和
    BigDecimal sumPowerByYear(Integer Year);

    Double sumPower();
    BigDecimal sumWaterByYear(String startTime, String endTime);

    Double sumWater();
    List<energyReadings> selectByTime(int devicesId, @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")LocalDateTime startTime,@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime);

    List<energyReadings> selectByTime(int devicesId, @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime monthTime);



    List<AcPowerTrendVO> getAcPowerTrend( Integer buildingId, Integer deviceId);

    List<WaterTrendVO> getWaterTrend( Integer buildingId, Integer deviceId);
    List<EnergyTrendVO> getEnergyTrend( Integer buildingId, Integer deviceId);
    List<WaterFlowRateVo> getWaterFlowRate(Integer buildingId, Integer deviceId);

    Double AvgEnv(Integer buildingId, Integer deviceId);
    Double AvgHumidity(Integer buildingId, Integer deviceId);

    Page<AlertVo> QueryErrorEnergy(int current,int size,    @RequestParam(required = false) Integer buildingId,
                                   @RequestParam(required = false) String metricName,
                                   @RequestParam(required = false) Integer deviceId,  @RequestParam(required = false) Integer status,
                                   @RequestParam(required = false) AlertRecord.AlertType alertType,@RequestParam(required = false) LocalDateTime startTime,
                                   @RequestParam(required = false) LocalDateTime endTime);

    List<BuildingEnergyVo> QueryBuildingEnergy();


    List<DeviceLatestCompareVO> QueryDeviceLatestCompare();

}
