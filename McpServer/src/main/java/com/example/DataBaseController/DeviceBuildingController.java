package com.example.DataBaseController;

import com.example.Entity.BuildingVo;
import com.example.Entity.DeviceBuildingVo;
import com.example.Entity.DeviceStatusCountVo;
import com.example.Entity.ReultEntity.Result;
import com.example.Mapper.BuildingsMapper;
import com.example.Mapper.DevicesMapper;
import com.example.Mapper.EnergyReadingsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/deviceBuilding")
public class DeviceBuildingController {

    @Autowired
    private DevicesMapper deviceBuildingService;
    @Autowired
    private BuildingsMapper buildingsMapper;
    @Autowired
    private EnergyReadingsMapper energyReadingsMapper;

    @GetMapping("/queryBuildingIdAndName")
    public Result<List<BuildingVo>> queryBuildingIdAndName(){
        return Result.success(buildingsMapper.queryBuildingIdAndName());
    }

    @RequestMapping("/queryDeviceBuildingVo")
    public Result<List<DeviceBuildingVo>> queryDeviceBuildingVo(@RequestParam Integer buildingId) {
        return Result.success(deviceBuildingService.queryDeviceBuildingVo(buildingId));
    }
    @RequestMapping("/queryDeviceBuildingVoByDeviceId")
    public Result<List<DeviceStatusCountVo>> queryDeviceBuildingVoByDeviceId(@RequestParam(required = false) Integer BuildingId) {
        return Result.success(deviceBuildingService.queryDeviceStatusByBuildingId(BuildingId));
    }
}
