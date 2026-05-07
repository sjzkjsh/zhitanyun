package com.atguigu.DatabaseController;

import com.atguigu.FeignInterface.PageFeign;
import com.atguigu.Result.Result;
import com.example.Entity.BuildingVo;
import com.example.Entity.DeviceBuildingVo;


import com.example.Entity.DeviceStatusCountVo;
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
    private PageFeign pageFeign;

    @GetMapping("/queryBuildingIdAndName")
    public Result<List<BuildingVo>> queryBuildingIdAndName(){
        return pageFeign.queryBuildingIdAndName();
    }

    @RequestMapping("/queryDeviceBuildingVo")
    public Result<List<DeviceBuildingVo>> queryDeviceBuildingVo(@RequestParam Integer buildingId){
        return pageFeign.queryDeviceBuildingVo(buildingId);
    }
    @RequestMapping("/queryDeviceBuildingVoByDeviceId")
    public Result<List<DeviceStatusCountVo>> queryDeviceBuildingVoByDeviceId(@RequestParam(required = false) Integer BuildingId) {
        return pageFeign.queryDeviceBuildingVoByDeviceId(BuildingId);
    }
}
