package com.atguigu.DatabaseController;

import com.atguigu.FeignInterface.PageFeign;
import com.atguigu.Result.Result;
import com.example.Entity.*;
import com.example.Entity.EnergyEntity.AcPowerTrendVO;
import com.example.Entity.EnergyEntity.EnergyTrendVO;
import com.example.Entity.EnergyEntity.WaterFlowRateVo;
import com.example.Entity.EnergyEntity.WaterTrendVO;
import com.example.Entity.PieEntity.EnergyPieResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/energy")
public class PowerController {

    @Autowired
    PageFeign pageFeign;
    //趋势折线图



    //AC功率
    @Cacheable(
            value = "acPowerTrend",
            key = "'trend:' + (#buildingId ?: 'all') + ':' + (#deviceId ?: 'none')",
            unless = "#result == null || #result.data == null || #result.data.isEmpty()"
    )
    @GetMapping("/acPower")
    public Result<List<AcPowerTrendVO>> getAcPower(@RequestParam(required = false) Integer buildingId,
                                                   @RequestParam(required = false) Integer deviceId){
        return pageFeign.getAcPower(buildingId, deviceId);
    }
    @GetMapping("/acPowerByBuilding")
    public Result<List<BuildingAcPower>> getAcPowerByBuilding(){
        return pageFeign.getAcPowerByBuilding();
    }
    @GetMapping("/BuildingAcPower")
    Result<List<EnergyReadingVO>> queryBuildingEnergy(@RequestParam(required = false) String deviceType,
                                                      @RequestParam(required = false)String deviceStatus,
                                                      @RequestParam(required = false) Integer buildingId,
                                                      @RequestParam(required = false) Integer deviceId){
        return pageFeign.queryBuildingEnergy(deviceType, deviceStatus, buildingId, deviceId);
    }




    //水表功率
    @Cacheable(
            value = "waterPowerTrend",
            key = "'trend:' + (#buildingId ?: 'all') + ':' + (#deviceId ?: 'none')",
            unless = "#result == null || #result.data == null || #result.data.isEmpty()"
    )
    @GetMapping("/waterPower")
    public Result<List<WaterTrendVO>> getWaterPower(@RequestParam(required = false) Integer buildingId,
                                                    @RequestParam(required = false) Integer deviceId) {
        return pageFeign.getWaterPower(buildingId, deviceId);

    }
    @GetMapping("/waterPowerByBuilding")
    public Result<List<BuildingWaterTrendVO>> getWaterPowerByBuilding(){
        return pageFeign.getWaterPowerByBuilding();
    }
    @GetMapping("/BUildingWaterPower")
    Result<List<BuildingWaterPower>> getBuildingWaterEnergy(@RequestParam(required = false)String deviceType,
                                                            @RequestParam(required = false)String deviceStatus,
                                                            @RequestParam(required = false) Integer buildingId,
                                                            @RequestParam(required = false)Integer deviceId){
        return pageFeign.getBuildingWaterEnergy(deviceType, deviceStatus, buildingId, deviceId);
    }


    //电表功率
    @Cacheable(
            value = "energyPowerTrend",
            key = "'trend:' + (#buildingId ?: 'all') + ':' + (#devicesId ?: 'none')",
            unless = "#result == null || #result.data == null || #result.data.isEmpty()"
    )
    @GetMapping("/energyPower")
    public Result<List<EnergyTrendVO>> getEnergyPower(@RequestParam(required = false) Integer buildingId
                                                    , @RequestParam(required = false) Integer devicesId){
        return pageFeign.getEnergyPower(buildingId, devicesId);
    }

    @GetMapping("/energyPowerByBuilding")
    Result<List<BuildingEnergyTrendVO>> getEnergyPowerByBuilding(){
        return pageFeign.getEnergyPowerByBuilding();
    }
    @GetMapping("/BuildingEnergyPower")
    Result<List<BuildingEnergyPower>> getBuildingEnergy(@RequestParam(required = false)String deviceType,
                                                        @RequestParam(required = false)String deviceStatus,
                                                        @RequestParam(required = false)Integer buildingId,
                                                        @RequestParam(required = false)Integer deviceId){
        return pageFeign.getBuildingEnergy(deviceType, deviceStatus, buildingId, deviceId);
    }


    //水表流量
    @Cacheable(
            value = "waterFlowTrend",
            key = "'trend:' + (#buildingId ?: 'all') + ':' + (#deviceId ?: 'none')",
            unless = "#result == null || #result.data == null || #result.data.isEmpty()"
    )
    @GetMapping("/waterFlow")
    public Result<List<WaterFlowRateVo>> getWaterFlow(@RequestParam(required = false) Integer buildingId,
                                                      @RequestParam(required = false) Integer deviceId){
        return pageFeign.getWaterFlow(buildingId, deviceId);
    }
    @GetMapping("/waterFlowByBuilding")
    Result<List<BuildingWaterFlowRateVO>> getWaterFlowByBuilding(){
        return pageFeign.getWaterFlowByBuilding();
    }
    @GetMapping("/BuildingWaterFlow")
    Result<List<BuildingWaterFlowRate>> getBuildingWaterFlow( @RequestParam(required = false)String deviceType,
                                                              @RequestParam(required = false)  String deviceStatus,
                                                              @RequestParam(required = false) Integer buildingId,
                                                              @RequestParam(required = false) Integer deviceId){
        return pageFeign.getBuildingWaterFlow(deviceType, deviceStatus, buildingId, deviceId);
    }




    //扇形统计图
    @Cacheable(
            value = "energyPie",
            key = "'pie:' + (#buildingId ?: 'all') + ':' + (#deviceId ?: 'none')",
            unless = "#result == null || #result.data == null || #result.data.pieData == null || #result.data.pieData.isEmpty()"
    )
    @GetMapping("/pie")
    public Result<EnergyPieResult> getEnergyPie(
            @RequestParam(required = false) Integer buildingId,
            @RequestParam(required = false) Integer deviceId){

        return pageFeign.getEnergyPie(buildingId, deviceId);
    }

    @Cacheable(
            value = "buildingEnergy",
            key = "'buildingEnergy'",
            unless = "#result == null || #result.data == null || #result.data.isEmpty()"
    )
    @GetMapping("/buildingEnergy")
    public Result<List<BuildingEnergyVo>> getBuildingEnergy(){
        return pageFeign.getBuildingEnergy();
    }


    @PostMapping("/cache/clear")
    @CacheEvict(value = {
            "acPowerTrend", "waterPowerTrend", "energyPowerTrend",
            "waterFlowTrend", "energyPie"
    }, allEntries = true)
    public Result<Void> clearCache() {
        return Result.success();
    }
}
