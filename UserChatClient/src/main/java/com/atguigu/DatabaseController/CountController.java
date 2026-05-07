package com.atguigu.DatabaseController;
import com.atguigu.FeignInterface.PageFeign;
import com.atguigu.Result.Result;
import com.example.Entity.CoVo;
import com.example.Entity.DeviceEnergyVo;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/count")
public class CountController {
    private final PageFeign countFeign;

    //建筑数量统计
    @Cacheable(value = "BuildingCount", key = "'all'")
        @GetMapping("/build")
    public int BuildingCount(){
        return countFeign.BuildingCount();
    }

    //电耗统计
    @Cacheable(
            value = "PowerCount",
            key = "'sum:' + (#Year ?: 'all')",  // Year为空时用'all'
            unless = "#result == null || #result.data == null"
    )
    @GetMapping("/power")
    public Result<BigDecimal> SumPowerConsumption(@RequestParam(required = false) Integer Year){
        return countFeign.SumPowerConsumption(Year);
    }

    //总电耗
    @Cacheable(
            value = "PowerCount",
            key = "'total'",
            unless = "#result == null || #result.data == null"
    )
    @GetMapping("/consumption")
    public Result<Double> SumPower(){
        return countFeign.SumPower();
    }

    //水耗统计
    @Cacheable(
            value = "WaterCount",
            key = "'sum:' + (#Year ?: 'all')",
            unless = "#result == null || #result.data == null"
    )
    @GetMapping("/water")
    public Result<BigDecimal> SumWaterConsumption(@RequestParam(required = false)Integer Year){
        return countFeign.SumWaterConsumption(Year);
    }

    //总水耗
    @Cacheable(
            value = "WaterCount",
            key = "'total'",
            unless = "#result == null || #result.data == null"
    )
    @GetMapping("/waterconsumption")
    public Result<Double> SumWater(){
        return countFeign.SumWater();
    }

    //异常设备统计
    @Cacheable(
            value = "ExceptionCount",
            key = "'count:' + (#startTime ?: 'all') + ':' + (#endTime ?: 'all')",
            unless = "#result == null || #result.data == null"
    )
    @GetMapping("/exception")
    public Result<Integer> CountException(@RequestParam(required = false) LocalDateTime startTime
            , @RequestParam(required = false) LocalDateTime endTime){
            return countFeign.CountException(startTime, endTime);
    }

    @Cacheable(
            value = "EnvCount",
            key = "'avg:' + (#buildingId ?: 'all') + ':' + (#deviceId ?: 'all')",
            unless = "#result == null || #result.data == null"
    )
    @GetMapping("/env")
    public Result<Double> AvgEnv(@RequestParam(required = false) Integer buildingId
            ,@RequestParam(required = false) Integer deviceId) {
        return countFeign.AvgEnv(buildingId,deviceId);
    }
    @Cacheable(
            value = "HumidityCount",
            key = "'avg:' + (#buildingId ?: 'all') + ':' + (#deviceId ?: 'all')",
            unless = "#result == null || #result.data == null"
    )
    @GetMapping("/humidity")
    public Result<Double> AvgHumidity(@RequestParam(required = false) Integer buildingId
            ,@RequestParam(required = false) Integer deviceId) {
        return countFeign.AvgHumidity(buildingId,deviceId);
    }
    //平均负载
    @Cacheable(
            value = "PowerLoadCount",
            key = "'avg:' + (#buildingId ?: 'all') + ':' + (#deviceId ?: 'all')",
            unless = "#result == null || #result.data == null"
    )
    @GetMapping("/powerLoad")
    public Result<Double> PowerLoad(@RequestParam(required = false) Integer buildingId
            ,@RequestParam(required = false) Integer deviceId){
        return countFeign.PowerLoad(buildingId,deviceId);
    }

    @Cacheable(
            value = "CarbonCount",
            key = "'trend:' + (#buildingId ?: 'all') + ':' + (#deviceId ?: 'all')",
            unless = "#result == null || #result.data == null"
    )
    @GetMapping("/carbon")
    public Result<List<CoVo>> CarbonTrend(@RequestParam(required = false) Integer buildingId,
                                   @RequestParam(required = false) Integer deviceId,
                                   @RequestParam(required = false) LocalDateTime startTime,
                                   @RequestParam(required = false) LocalDateTime endTime){
        return countFeign.CarbonTrend(buildingId,deviceId,startTime,endTime);
    }

    @Cacheable(
            value = "DeviceCount",
            key = "'group'",
            unless = "#result == null || #result.data == null"
    )
    @GetMapping("/groupDevice")
    public Result<List<DeviceEnergyVo>> GroupDevice(){
        return countFeign.GroupDevice();
    }
    @GetMapping("/groupDeviceStatus")
    public Result<List<DeviceEnergyVo>> GroupDeviceStatus(){
        return countFeign.GroupDeviceCount();
    }

    //根据设备状态分组
    @GetMapping("/deviceStatus")
    public Result<List<DeviceEnergyVo>> DeviceStatus(){
        return countFeign.DeviceStatus();
    }
    //设备状态分组并统计数量
    @GetMapping("/DeviceStatus")
    public Result<List<DeviceEnergyVo>> DeviceStatusCount(){
        return countFeign.DeviceStatusCount();
    }


    //刷新数据接口
    @PostMapping("/cache/clear")
    @CacheEvict(value = {"BuildingCount", "PowerCount", "WaterCount"}, allEntries = true)
    public Result<Void> clearCache() {
        return Result.success();
    }
}
