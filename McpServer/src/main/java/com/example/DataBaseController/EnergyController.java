package com.example.DataBaseController;

import com.example.Entity.*;
import com.example.Entity.EnergyEntity.AcPowerTrendVO;
import com.example.Entity.EnergyEntity.EnergyTrendVO;
import com.example.Entity.EnergyEntity.WaterFlowRateVo;
import com.example.Entity.EnergyEntity.WaterTrendVO;
import com.example.Entity.ReultEntity.Result;
import com.example.Mapper.EnergyReadingsMapper;
import com.example.Service.EnergyReadingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/energy")
public class EnergyController {

    @Autowired
    EnergyReadingsService energyReadingsService;
    @Autowired
    EnergyReadingsMapper energyReadingsMapper;


    //空调系统能耗
    @GetMapping("/acPower")
    public Result<List<AcPowerTrendVO>> getAcPower(@RequestParam(required = false) Integer buildingId, @RequestParam(required = false) Integer deviceId){
        return Result.success(energyReadingsService.getAcPowerTrend(buildingId, deviceId));
    }
    //根据建筑分组的空调系统能耗

    @GetMapping("/acPowerByBuilding")
    public Result<List<BuildingAcPower>> getAcPowerByBuilding(){
        return Result.success(energyReadingsMapper.getAcPowerTrendGroupByBuilding());
    }
    //查询建筑空调系统能耗
    @GetMapping("/BuildingAcPower")
    public Result<List<EnergyReadingVO>> queryBuildingEnergy(@RequestParam(required = false) String deviceType,
                                                           @RequestParam(required = false) String deviceStatus,
                                                           @RequestParam(required = false) Integer buildingId,
                                                             @RequestParam(required = false) Integer deviceId){
        return Result.success(energyReadingsMapper.queryEnergyReadings(deviceType, deviceStatus, buildingId, deviceId));
    }

    //水耗
    @GetMapping("/waterPower")
    public Result<List<WaterTrendVO>> getWaterPower(@RequestParam(required = false) Integer buildingId, @RequestParam(required = false) Integer deviceId){
        return Result.success(energyReadingsService.getWaterTrend(buildingId, deviceId));
    }
    //根据建筑分组的水耗
    @GetMapping("/waterPowerByBuilding")
    public Result<List<BuildingWaterTrendVO>> getWaterPowerByBuilding(){
        return Result.success(energyReadingsMapper.getWaterTrendGroupByBuilding());
    }
    //查询建筑水耗
    @GetMapping("/BUildingWaterPower")
    public Result<List<BuildingWaterPower>> getBuildingWaterEnergy(@RequestParam(required = false) String deviceType,
                                                                   @RequestParam(required = false) String deviceStatus,
                                                                   @RequestParam(required = false) Integer buildingId,
                                                                   @RequestParam(required = false) Integer deviceId){
        return Result.success(energyReadingsMapper.queryWaterPower(deviceType, deviceStatus, buildingId, deviceId));
    }

    //电耗
    @GetMapping("/energyPower")
    public Result<List<EnergyTrendVO>> getEnergyPower(@RequestParam(required = false) Integer buildingId, @RequestParam(required = false) Integer deviceId){
        return Result.success(energyReadingsService.getEnergyTrend(buildingId, deviceId));
    }
    //根据建筑分组的电耗
    @GetMapping("/energyPowerByBuilding")
    public Result<List<BuildingEnergyTrendVO>> getEnergyPowerByBuilding(){
        return Result.success(energyReadingsMapper.getEnergyTrendGroupByBuilding());
    }
    //查询建筑电耗
    @GetMapping("/BuildingEnergyPower")
    public Result<List<BuildingEnergyPower>> getBuildingEnergy(@RequestParam(required = false) String deviceType,
                                                               @RequestParam(required = false)String deviceStatus,
                                                               @RequestParam(required = false) Integer buildingId,
                                                               @RequestParam(required = false) Integer deviceId){
        return Result.success(energyReadingsMapper.queryEnergyPower(deviceType, deviceStatus, buildingId, deviceId));
    }
    //水流量
    @GetMapping("/waterFlow")
    public Result<List<WaterFlowRateVo>> getWaterFlow(@RequestParam(required = false) Integer buildingId, @RequestParam(required = false) Integer deviceId){
        return Result.success(energyReadingsService.getWaterFlowRate(buildingId, deviceId));
    }
    //根据建筑分组的水流量
    @GetMapping("/waterFlowByBuilding")
    public Result<List<BuildingWaterFlowRateVO>> getWaterFlowByBuilding(){
        return Result.success(energyReadingsMapper.getWaterFlowRateGroupByBuilding());
    }
    //查询建筑水流量
    @GetMapping("/BuildingWaterFlow")
    public Result<List<BuildingWaterFlowRate>> getBuildingWaterFlow(@RequestParam(required = false) String deviceType,
                                                                    @RequestParam(required = false) String deviceStatus,
                                                                    @RequestParam(required = false) Integer buildingId,
                                                                    @RequestParam(required = false)Integer deviceId){
        return Result.success(energyReadingsMapper.queryWaterFlowRate(deviceType, deviceStatus, buildingId, deviceId));
    }
}
