package com.example.DataBaseController;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.Entity.Buildings;
import com.example.Entity.Devices;
import com.example.Entity.ReultEntity.Result;
import com.example.Entity.energyReadings;
import com.example.Service.BuildingsService;
import com.example.Service.DevicesService;
import com.example.Service.UpdateEnergyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/update")
public class UpdateController {

    @Autowired
    private UpdateEnergyService updateEnergyService;

    @Autowired
    private BuildingsService buildingsService;

    @Autowired
    private DevicesService devicesService;



    //回显
    @GetMapping("/query")
    public Result<energyReadings> QueryEnergy(@RequestParam("readingId")int readingId){
        LambdaQueryWrapper<energyReadings> energyReadingsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        energyReadingsLambdaQueryWrapper.eq(energyReadings::getReadingId, readingId);
        energyReadings one = updateEnergyService.getOne(energyReadingsLambdaQueryWrapper);
        return Result.success(one);
    }
    //修改
    @PostMapping("/energy")
    public Result<Object> UpdateEnergy(@RequestBody energyReadings energy){
        if (updateEnergyService.updateById(energy)) {
            return Result.success();
        }
        return Result.error("更新失败");
    }

    //回显
    @GetMapping("/building")
    public Result<Buildings> QueryBuilding(@RequestParam("buildingId")int buildingId){
        LambdaQueryWrapper<Buildings> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Buildings::getBuildingId, buildingId);

        return Result.success(buildingsService.getOne(wrapper));
    }

    //修改建筑
    @PostMapping("/updateBuilding")
    public Result<Object> UpdateBuilding(@RequestBody Buildings buildings){
        if (buildingsService.updateById(buildings)) {
            return Result.success();
        }
        return Result.error("更新失败");
    }


    @GetMapping("/device")
    public Result<Devices> QueryDevice(@RequestParam("deviceId")int deviceId){
        LambdaQueryWrapper<Devices> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Devices::getDeviceId, deviceId);
        Devices one = devicesService.getOne(wrapper);
        return Result.success(one);
    }

    //修改设备
    @PostMapping("/updateDevice")
    public Result<Object> UpdateDevice(@RequestBody Devices devices){
        if (devicesService.updateById(devices)) {
            return Result.success(devices);
        }
        return Result.error("更新失败");
    }
}
