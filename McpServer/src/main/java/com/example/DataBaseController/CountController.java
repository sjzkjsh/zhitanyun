package com.example.DataBaseController;

import com.example.Entity.CoVo;
import com.example.Entity.DeviceEnergyVo;
import com.example.Entity.ReultEntity.Result;
import com.example.Mapper.DevicesMapper;
import com.example.Mapper.EnergyReadingsMapper;
import com.example.Service.BuildingsService;
import com.example.Service.DevicesService;
import com.example.Service.EnergyReadingsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/count")
public class CountController {

    @Autowired
    private BuildingsService buildingsService;
    @Autowired
    private EnergyReadingsService energyReadingsService;
    @Autowired
    private DevicesService device;
    @Autowired
    private DevicesMapper devicesMapper;
    @Autowired
    private EnergyReadingsMapper energyReadingsMapper;
    @GetMapping("/build")
    public int BuildingCount(){
        return buildingsService.countBuildings();
    }

    //年电力总能耗
    @GetMapping("/power")
    public Result<BigDecimal> SumPowerConsumption(@RequestParam(required = false) Integer Year){
        return Result.success(energyReadingsService.sumPowerByYear(Year));
    }

    //年总能耗
    @GetMapping("/consumption")
    public Result<Double> SumPower(){
        return Result.success(energyReadingsService.sumPower());
    }


    //年水耗
    @GetMapping("/water")
    public Result<BigDecimal> SumWaterConsumption(String startTime, String endTime){
        return Result.success(energyReadingsService.sumWaterByYear(startTime,endTime));
    }

    //年总水耗
    @GetMapping("/waterconsumption")
    public Result<Double> SumWater(){
        return Result.success(energyReadingsService.sumWater());
    }


    //异常设备的数量
    @GetMapping("/exception")
    public Result<Integer> CountException(@RequestParam(required = false) LocalDateTime startTime,@RequestParam(required = false) LocalDateTime endTime){
        return Result.success(device.countDeviceException(startTime, endTime));
    }

    //平均环境温度
    @GetMapping("/env")
    public Result<Double> AvgEnv(@RequestParam(required = false) Integer buildingId
            ,@RequestParam(required = false) Integer deviceId){
        return Result.success(energyReadingsService.AvgEnv(buildingId,deviceId));
    }
    //平均环境湿度
    @GetMapping("/humidity")
    public Result<Double> AvgHumidity(@RequestParam(required = false) Integer buildingId
            ,@RequestParam(required = false) Integer deviceId){
        return Result.success(energyReadingsService.AvgHumidity(buildingId,deviceId));
    }
    //平均负载
    @GetMapping("/powerLoad")
    public Result<Double> PowerLoad(@RequestParam(required = false) Integer buildingId
            ,@RequestParam(required = false) Integer deviceId){
        return Result.success(energyReadingsMapper.getPowerLoad(buildingId,deviceId));
    }

    //设备分组
    @GetMapping("/groupDevice")
    public Result<List<DeviceEnergyVo>> GroupDevice(){
        return Result.success(device.queryDeviceTypeCount());
    }
    //设备类型分组
    @GetMapping("/groupDeviceStatus")
    public Result<List<DeviceEnergyVo>> GroupDeviceCount(){
        return Result.success(device.queryDeviceCount());
    }

    //根据设备状态分组
    @GetMapping("/deviceStatus")
    public Result<List<DeviceEnergyVo>> DeviceStatus(){
        return Result.success(devicesMapper.queryDeviceStatusAndEnergy());
    }
    //设备状态分组并统计数量
    @GetMapping("/DeviceStatus")
    public Result<List<DeviceEnergyVo>> DeviceStatusCount(){
        return Result.success(devicesMapper.queryDeviceStatus());
    }


    //碳排放趋势
    @GetMapping("/carbon")
    public Result<List<CoVo>> CarbonTrend(@RequestParam(required = false) Integer buildingId,
                                          @RequestParam(required = false) Integer deviceId,
                                          @RequestParam(required = false) LocalDateTime startTime,
                                          @RequestParam(required = false) LocalDateTime endTime){
        return Result.success(energyReadingsMapper.getCarbonEmissionTrend(buildingId,deviceId,startTime,endTime));
    }


}
