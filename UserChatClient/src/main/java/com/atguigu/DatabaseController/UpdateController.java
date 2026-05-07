package com.atguigu.DatabaseController;

import com.atguigu.FeignInterface.PageFeign;

import com.atguigu.Result.Result;
import com.example.Entity.Buildings;
import com.example.Entity.Devices;
import com.example.Entity.energyReadings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/update")
public class UpdateController {

    @Autowired
    PageFeign pageFeign;

    @GetMapping ("/query")
    public Result<energyReadings> QueryEnergy(@RequestParam("readingId")int readingId){
        return pageFeign.QueryEnergy(readingId);
    }
    @GetMapping("/building")
    public Result<Buildings> QueryBuilding(@RequestParam("buildingId")int buildingId) {
        return pageFeign.QueryBuilding(buildingId);
    }
    @GetMapping("/device")
    public Result<Devices> QueryDevice(@RequestParam("deviceId")int deviceId){
        return pageFeign.QueryDevice(deviceId);
    }






    @PostMapping("/energy")
    public Result<Object> UpdateEnergy(@RequestBody energyReadings energy){
        Result<Object> objectResult = pageFeign.UpdateEnergy(energy);
        if(objectResult!=null||objectResult.getCode()==200){
            pageFeign.clearCache("mcpEnergy");
            pageFeign.clearCache("energyTrend");
            pageFeign.clearCache("energyStats");
        }
        return objectResult;
    }

    @PostMapping("/updateBuilding")
    public Result<Object> UpdateBuilding(@RequestBody Buildings buildings){
        Result<Object> objectResult = pageFeign.UpdateBuilding(buildings);
        if (objectResult!=null||objectResult.getCode()==200){
            pageFeign.clearCache("buildings");
        }
        return objectResult;
    }





    @PostMapping("/updateDevice")
    public Result<Object> UpdateDevice(@RequestBody Devices devices){
        Result<Object> objectResult = pageFeign.UpdateDevice(devices);
        if (objectResult!=null||objectResult.getCode()==200){
            pageFeign.clearCache("devices");
            pageFeign.clearCache("buildings");
        }
        return objectResult;
    }
}
