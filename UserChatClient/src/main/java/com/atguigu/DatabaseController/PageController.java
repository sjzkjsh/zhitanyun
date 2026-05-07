package com.atguigu.DatabaseController;

import com.atguigu.FeignInterface.PageFeign;
import com.atguigu.Result.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Entity.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RequestMapping("/api/energy")
@RestController
public class PageController {

    private final PageFeign pagefeign;
    public PageController(PageFeign pagefeign) {
        this.pagefeign = pagefeign;
    }
    @GetMapping("/readings")
    public Result<Page<DeviceEnergyBuildingVO>> getReadings(
            @RequestParam(required = false) Integer buildingId,
            @RequestParam(required = false) Integer deviceId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(required = false) String buildingType,
            @RequestParam(required = false) String deviceStatus,
            @RequestParam(required = false) String deviceCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size){
        return pagefeign.getReadings(buildingId,deviceId,startTime,endTime,
                buildingType,deviceStatus,deviceCode,page,size);
    }
    @GetMapping("/DeviceBuilding")
    public Result<Page<BuildingDeviceVo>> getDeviceBuilding(@RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int size,
                                                            @RequestParam(required = false) String deviceType,
                                                            @RequestParam(required = false) String buildingName,
                                                            @RequestParam(required = false) String deviceStatus,
                                                            @RequestParam(required = false) String buildingCode){
        return pagefeign.getDeviceBuilding(page, size, deviceType, buildingName, deviceStatus, buildingCode);
    }

    @GetMapping("/queryErrorEnergy")
    public Result<Page<AlertVo>> queryErrorEnergy(@RequestParam(defaultValue = "1") int current,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  @RequestParam(required = false) Integer buildingId,
                                                  @RequestParam(required = false) String metricName,
                                                  @RequestParam(required = false) Integer deviceId,  @RequestParam(required = false) Integer status,
                                                  @RequestParam(required = false) AlertRecord.AlertType alertType,@RequestParam(required = false) LocalDateTime startTime,
                                                  @RequestParam(required = false) LocalDateTime endTime){
        return pagefeign.queryErrorEnergy(current, size, buildingId, metricName,deviceId, status, alertType, startTime, endTime);
    }
    @GetMapping("/queryDeviceBuilding")
    Result<Page<BuildingDeviceVo>> getDeviceBuilding(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     @RequestParam(required = false) String deviceType
    ){
        return pagefeign.getDeviceBuilding(page, size, deviceType);
    }
    @GetMapping("/queryEnergyDevice")
    public Result<Page<energyDeviceVo>> queryEnergyDeviceBuilding(@RequestParam(defaultValue = "1") int page,
                                                                  @RequestParam(defaultValue = "10") int size,
                                                                  @RequestParam(required = false) String deviceStatus
    ){
        return pagefeign.queryEnergyDeviceBuilding(page, size, deviceStatus);
    }

    @GetMapping("/lastEnergy")
    public Result<List<DeviceLatestCompareVO>> getLastEnergy() {
        return pagefeign.getLastEnergy();
    }
}
