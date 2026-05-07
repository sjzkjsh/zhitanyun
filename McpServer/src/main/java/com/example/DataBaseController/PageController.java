package com.example.DataBaseController;



import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Entity.*;
import com.example.Entity.ReultEntity.Result;
import com.example.Mapper.DevicesMapper;
import com.example.Service.DataBaseService.PageService;
import com.example.Service.EnergyReadingsService;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/energy")
public class PageController {
    @Autowired
    private PageService pageService;
    @Autowired
    private EnergyReadingsService energyReadingsService;
    @Autowired
    private DevicesMapper devicesMapper;


    @GetMapping("/readings")
    public Result<Page<DeviceEnergyBuildingVO>> getReadings(
            @RequestParam(required = false) Integer buildingId,
            @RequestParam(required = false) Integer deviceId,
            @RequestParam(required = false) @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(required = false) String buildingType,
            @RequestParam(required = false) String deviceStatus,
            @RequestParam(required = false) String deviceCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(pageService.queryVO(
                buildingId, deviceId, startTime, endTime,
                buildingType, deviceStatus, deviceCode, page, size));
    }

    @GetMapping("/queryDeviceBuilding")
    public Result<Page<BuildingDeviceVo>> getDeviceBuilding(@RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "10") int size,
                                                            @RequestParam(required = false) String deviceType
    ){
        Page<Object> objectPage = new Page<>(page, size);
        return Result.success(devicesMapper.pageByDeviceType(objectPage , deviceType));
    }

    @GetMapping("/queryEnergyDevice")
    public Result<Page<energyDeviceVo>> queryEnergyDeviceBuilding(@RequestParam(defaultValue = "1") int page,
                                                                  @RequestParam(defaultValue = "10") int size,
                                                                  @RequestParam(required = false) String deviceStatus
    ){
        Page<Object> objectPage = new Page<>(page, size);
        return Result.success(devicesMapper.pageByDeviceStatus(objectPage , deviceStatus));
    }


    @GetMapping("/DeviceBuilding")
    public Result<Page<BuildingDeviceVo>> getDeviceBuilding(@RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "10") int size,
                                                            @RequestParam(required = false) String deviceType,
                                                            @RequestParam(required = false) String buildingName,
                                                            @RequestParam(required = false) String deviceStatus,
                                                            @RequestParam(required = false) String buildingCode){
        return Result.success(pageService.PageDevices(page , size, deviceType, buildingName, deviceStatus, buildingCode));
    }
    @GetMapping("/queryErrorEnergy")
    public Result<Page<AlertVo>> queryErrorEnergy(@RequestParam(defaultValue = "1") int current,
                                                           @RequestParam(defaultValue = "10") int size,
                                                  @RequestParam(required = false) Integer buildingId,
                                                  @RequestParam(required = false) String metricName,
                                                  @RequestParam(required = false) Integer deviceId,  @RequestParam(required = false) Integer status,
                                                  @RequestParam(required = false) AlertRecord.AlertType alertType,@RequestParam(required = false) LocalDateTime startTime,
                                                  @RequestParam(required = false) LocalDateTime endTime) {
        return Result.success(energyReadingsService.QueryErrorEnergy(current, size, buildingId,metricName, deviceId, status, alertType, startTime, endTime));
    }

    @GetMapping("/lastEnergy")
    public Result<List<DeviceLatestCompareVO>> getLastEnergy() {
        return Result.success(energyReadingsService.QueryDeviceLatestCompare());
    }
}