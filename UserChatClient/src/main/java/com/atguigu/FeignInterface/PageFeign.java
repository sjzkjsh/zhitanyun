package com.atguigu.FeignInterface;

import com.atguigu.Result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Entity.*;
import com.example.Entity.AnalysisEntity.ThresholdRange;
import com.example.Entity.ChatEntity.ChatMessage;
import com.example.Entity.ChatEntity.ContextCreateRequest;
import com.example.Entity.ChatEntity.ContextListVO;
import com.example.Entity.ChatEntity.ContextUpdateRequest;
import com.example.Entity.CopEntity.CopResult;
import com.example.Entity.EnergyEntity.AcPowerTrendVO;
import com.example.Entity.EnergyEntity.EnergyTrendVO;
import com.example.Entity.EnergyEntity.WaterFlowRateVo;
import com.example.Entity.EnergyEntity.WaterTrendVO;
import com.example.Entity.ExcelEntity.ImportResultVO;
import com.example.Entity.PieEntity.EnergyPieResult;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.ibatis.annotations.Param;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@FeignClient(name = "McpServer", url = "http://localhost:8014")
public interface PageFeign {

    /**
     * 数据清洗导入功能
     */
    @PostMapping(value = "/api/energy/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ImportResultVO importFile(@RequestPart("file") MultipartFile file);


    /*-------------------------Count--------------------------*/
    @GetMapping("count/build")
    int BuildingCount();

    @GetMapping("count/power")
    Result<BigDecimal> SumPowerConsumption(@RequestParam(required = false) Integer Year);

    @GetMapping("count/consumption")
    Result<Double> SumPower();

    @GetMapping("count/water")
    Result<BigDecimal> SumWaterConsumption(@RequestParam(required = false) Integer Year);

    @GetMapping("count/waterconsumption")
    Result<Double> SumWater();
    @GetMapping("count/exception")
    Result<Integer> CountException(@RequestParam(required = false) LocalDateTime startTime,
                                   @RequestParam(required = false) LocalDateTime endTime);
    //平均设备环境温度
    @GetMapping("count/env")
    Result<Double> AvgEnv(@RequestParam(required = false) Integer buildingId,
                          @RequestParam(required = false) Integer deviceId);
    //平均设备环境人员密度
    @GetMapping("count/humidity")
    Result<Double> AvgHumidity(@RequestParam(required = false) Integer buildingId,
                               @RequestParam(required = false) Integer deviceId);
    //平均负载
    @GetMapping("count/powerLoad")
    public Result<Double> PowerLoad(@RequestParam(required = false) Integer buildingId
            ,@RequestParam(required = false) Integer deviceId);


    @GetMapping("count/groupDevice")
    Result<List<DeviceEnergyVo>> GroupDevice();
    @GetMapping("count/groupDeviceStatus")
    Result<List<DeviceEnergyVo>> GroupDeviceCount();
    //根据设备状态分组
    @GetMapping("count/deviceStatus")
    Result<List<DeviceEnergyVo>> DeviceStatus();
    //设备状态分组并统计数量
    @GetMapping("count/DeviceStatus")
    Result<List<DeviceEnergyVo>> DeviceStatusCount();

    @GetMapping("count/carbon")
    Result<List<CoVo>> CarbonTrend(@RequestParam(required = false) Integer buildingId,
                                   @RequestParam(required = false) Integer deviceId,
                                   @RequestParam(required = false) LocalDateTime startTime,
                                   @RequestParam(required = false) LocalDateTime endTime);
    /*-------------------------导出Excel--------------------------*/
    @GetMapping("api/energy/excel")
    byte[] exportEnergy(
            @RequestParam(required = false) Integer buildingId,
            @RequestParam(required = false) Integer deviceId,
            @RequestParam(required = false) @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(required = false) String buildingType,
            @RequestParam(required = false) String deviceStatus,
            @RequestParam(required = false) String deviceCode);

    /*-------------------------分页--------------------------*/
    @GetMapping("api/energy/readings")
    Result<Page<DeviceEnergyBuildingVO>> getReadings(
            @RequestParam(required = false) Integer buildingId,
            @RequestParam(required = false) Integer deviceId,
            @RequestParam(required = false) @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(required = false) String buildingType,
            @RequestParam(required = false) String deviceStatus,
            @RequestParam(required = false) String deviceCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size);

    @GetMapping("api/energy/queryDeviceBuilding")
    Result<Page<BuildingDeviceVo>> getDeviceBuilding(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     @RequestParam(required = false) String deviceType
    );
    @GetMapping("api/energy/queryEnergyDevice")
    Result<Page<energyDeviceVo>> queryEnergyDeviceBuilding(@RequestParam(defaultValue = "1") int page,
                                                           @RequestParam(defaultValue = "10") int size,
                                                           @RequestParam(required = false) String deviceStatus
    );


    @GetMapping("api/energy/DeviceBuilding")
    Result<Page<BuildingDeviceVo>> getDeviceBuilding(@RequestParam("page") int page,
                                                     @RequestParam("size") int size,
                                                     @RequestParam(required = false) String deviceType,
                                                     @RequestParam(required = false) String buildingName,
                                                     @RequestParam(required = false) String deviceStatus,
                                                     @RequestParam(required = false) String buildingCode);

    @GetMapping("api/energy/lastEnergy")
    Result<List<DeviceLatestCompareVO>> getLastEnergy();


    @GetMapping("api/energy/queryErrorEnergy")
    Result<Page<AlertVo>> queryErrorEnergy(@RequestParam(defaultValue = "1") int current,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(required = false) Integer buildingId,
                                           @RequestParam(required = false) String metricName,
                                           @RequestParam(required = false) Integer deviceId,  @RequestParam(required = false) Integer status,
                                           @RequestParam(required = false) AlertRecord.AlertType alertType,@RequestParam(required = false) LocalDateTime startTime,
                                           @RequestParam(required = false) LocalDateTime endTime)  ;

    @GetMapping("energy/buildingEnergy")
    Result<List<BuildingEnergyVo>> getBuildingEnergy();


    /*-------------------------更新--------------------------*/
    //能耗回显
    @GetMapping("update/query")
    Result<energyReadings> QueryEnergy(@RequestParam("readingId") int readingId);

    //能耗修改
    @PostMapping("update/energy")
    Result<Object> UpdateEnergy(@RequestBody energyReadings energy);

    //建筑回显
    @GetMapping("update/building")
    Result<Buildings> QueryBuilding(@RequestParam("buildingId") int buildingId);

    //修改建筑
    @PostMapping("update/updateBuilding")
    Result<Object> UpdateBuilding(@RequestBody Buildings buildings);

    //设备回显
    @GetMapping("update/device")
    Result<Devices> QueryDevice(@RequestParam("deviceId") int deviceId);

    //设备修改
    @PostMapping("update/updateDevice")
    Result<Object> UpdateDevice(@RequestBody Devices devices);


    // ========== 上下文管理接口 ==========

    @PostMapping("chat/context")
    Result<String> createContext(@RequestBody ContextCreateRequest request);


    @GetMapping("chat/contexts")
    Result<List<ContextListVO>> listContexts(@RequestParam String userId);


    @GetMapping("chat/context/{contextId}/history")
    Result<List<ChatMessage>> getHistory(@PathVariable String contextId);


    @PutMapping("chat/context/{contextId}")
    void updateContext(
            @PathVariable String contextId,
            @RequestBody ContextUpdateRequest request);


    @DeleteMapping("chat/context/{contextId}")
    void deleteContext(@PathVariable String contextId);



    @GetMapping("file/content")
    String getFileContext(@PathVariable String fileId);

    @GetMapping("file/name")
    String getFileName(@PathVariable String fileId);


    // ========== 能耗趋势接口 ==========

    @GetMapping("energy/acPower")
    Result<List<AcPowerTrendVO>> getAcPower(@RequestParam(required = false) Integer buildingId,
                                            @RequestParam(required = false) Integer deviceId);
    @GetMapping("energy/waterFlowByBuilding")
    Result<List<BuildingWaterFlowRateVO>> getWaterFlowByBuilding();
    @GetMapping("energy/BuildingWaterFlow")
    Result<List<BuildingWaterFlowRate>> getBuildingWaterFlow(@RequestParam(required = false) String deviceType,
                                                             @RequestParam(required = false) String deviceStatus,
                                                             @RequestParam(required = false) Integer buildingId,
                                                             @RequestParam(required = false) Integer deviceId);
    @GetMapping("energy/BuildingEnergyPower")
    Result<List<BuildingEnergyPower>> getBuildingEnergy(@RequestParam(required = false) String deviceType,
                                                        @RequestParam(required = false) String deviceStatus,
                                                        @RequestParam(required = false) Integer buildingId,
                                                        @RequestParam(required = false) Integer deviceId);
    @GetMapping("energy/BUildingWaterPower")
    Result<List<BuildingWaterPower>> getBuildingWaterEnergy(@RequestParam(required = false) String deviceType,
                                                            @RequestParam(required = false) String deviceStatus,
                                                            @RequestParam(required = false) Integer buildingId,
                                                            @RequestParam(required = false) Integer deviceId);
    @GetMapping("energy/BuildingAcPower")
    Result<List<EnergyReadingVO>> queryBuildingEnergy(@RequestParam(required = false) String deviceType,
                                                      @RequestParam(required = false) String deviceStatus,
                                                      @RequestParam(required = false) Integer buildingId,
                                                      @RequestParam(required = false) Integer deviceId);
    @GetMapping("energy/waterPower")
    Result<List<WaterTrendVO>> getWaterPower(@RequestParam(required = false) Integer buildingId,
                                             @RequestParam(required = false) Integer deviceId);
    @GetMapping("energy/energyPowerByBuilding")
    Result<List<BuildingEnergyTrendVO>> getEnergyPowerByBuilding();

    @GetMapping("energy/energyPower")
    Result<List<EnergyTrendVO>> getEnergyPower(@RequestParam(required = false) Integer buildingId,
                                               @RequestParam(required = false) Integer deviceId);
    @GetMapping("energy/waterPowerByBuilding")
    public Result<List<BuildingWaterTrendVO>> getWaterPowerByBuilding();
    @GetMapping("energy/waterFlow")
    Result<List<WaterFlowRateVo>> getWaterFlow(@RequestParam(required = false) Integer buildingId,
                                               @RequestParam(required = false) Integer deviceId);
    @GetMapping("energy/acPowerByBuilding")
    public Result<List<BuildingAcPower>> getAcPowerByBuilding();
    // 能耗饼图
    @GetMapping("energy/pie")
    Result<EnergyPieResult> getEnergyPie(
            @RequestParam(required = false) Integer buildingId,
            @RequestParam(required = false) Integer deviceId);



    // 缓存清除接口
    @PostMapping("cache/evict")
    Result<Void> evictCache(
            @RequestParam String cacheName,
            @RequestParam String key);

    @PostMapping("cache/clear")
    Result<Void> clearCache(@RequestParam String cacheName);

    @PostMapping("cache/clear/all")
    Result<Void> clearAllCache();



    //工单
    @RequestMapping("workerOrder/getWorkOrders")
    Result<Page<WorkOrderListVO>> getWorkOrders(@RequestParam(required = false)@Param("status") String status,
                                                @RequestParam(required = false)@Param("priority") String priority,
                                                @RequestParam(required = false)@Param("type") String type,
                                                @RequestParam(required = false)@Param("orderNo") String orderNo,
                                                @RequestParam(required = false)@Param("buildingId") Long buildingId,
                                                @RequestParam(required = false)@Param("handlerId") Long handlerId,
                                                @RequestParam(required = false)@Param("startTime") LocalDateTime startTime,
                                                @RequestParam(required = false)@Param("endTime") LocalDateTime endTime,
                                                @RequestParam(required = false)@Param("overdue") Boolean overdue,
                                                @RequestParam(required = false,defaultValue = "1")@Param("page")int  page,
                                                @RequestParam(required = false,defaultValue = "10")@Param("size") int size);
    //查询工单详情
    @GetMapping("workerOrder/getOneOrder/{id}")
    Result<WorkOrder> getOneOrder(@PathVariable("id") int id);
    //插入工单
    @PostMapping("workerOrder/saveOrUpdate")
    Result<Boolean> saveOrUpdate(@RequestBody  WorkOrder workOrder);
    //修改工单
    @PostMapping("workerOrder/UpdateById")
    Result<Boolean> UpdateById(@RequestParam int id,@RequestParam String status,@RequestParam Long userId);
    //工单统计，根据工单类型
    @GetMapping("workerOrder/CountByType")
    Result<List<FaultTypeStatVO>> CountByType();
    //工单统计，根据工单状态
    @GetMapping("workerOrder/CountByStatus")
    Result<List<FaultStatusVO>> CountByStatus();
    //根据工单状态查询工单
    @GetMapping("workerOrder/WorkerOrderByStatus")
    Result<Page<WorkOrderListVO>> WorkerOrderByStatus(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      @RequestParam(defaultValue = "待处理")String workerStatus);
    //根据设备状态分组，统计工单数量和查询创建的日期
    @GetMapping("workerOrder/StatusWorkerOrder")
    Result<List<WorkOrderStatusCountVO>> StatusWorkerOrder();
    //获取超时工单
    @GetMapping("workerOrder/errorOrder")
    Result<Page<WorkOrder>> errorOrder(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int size);
    //超时工单数量
    @GetMapping("workerOrder/count")
    Result<Long> count();

    @RequestMapping("WorkOrderLog/getWorkOrderLog")
    Result<Page<WorkOrderLog>> getWorkOrderLog(@RequestParam(defaultValue = "1") int  page
            ,@RequestParam(defaultValue = "10") int  size, @RequestParam(required = false) Long orderId,@RequestParam(required = false) String action, @RequestParam(required = false) Long operatorId
            ,@RequestParam(required = false) LocalDateTime startTime,
                                               @RequestParam(required = false)LocalDateTime endTime);
    //查询顾客列表
    @RequestMapping("customer/customer")
    Result<Page<customerVo>> customer(@RequestParam(required = false,defaultValue = "1") int  page
            , @RequestParam(defaultValue = "15",required = false) int  size,
                                      @RequestParam(required = false) String name,
                                      @RequestParam(required = false) String email,
                                      @RequestParam(required = false) String phone,
                                      @RequestParam(required = false) String buildingCode,
                                      @RequestParam(required = false) String deviceCode,
                                      @RequestParam(required = false) String status,
                                      @RequestParam(required = false)LocalDateTime startTime,
                                      @RequestParam(required = false)LocalDateTime endTime);
    //回显
    @RequestMapping("customer/selectByid/{id}")
    Result<customer> selectCustomerById(@PathVariable("id") String id);
    //修改
    @RequestMapping("customer/updateByid")
    Result<Integer> updateCustomerById(@RequestBody customer cust);


    @GetMapping("api/documents")
    public List<DocumentDTO> listDocuments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category);
    //查询使用说明书
    @GetMapping("api/documentUse")
    public List<DocumentDTO> listDocumentsUse(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category);


    @GetMapping("api/pdf/{id}")
    ResponseEntity<InputStreamResource> getPdf(@PathVariable Long id, HttpServletRequest request);
    //统计总数
    @RequestMapping("customer/selectCount")
    Result<Integer> selectCount();
    //统计正常
    @RequestMapping("customer/selectCountByStatus")
    Result<Integer> selectCountByStatus();
    //统计异常
    @RequestMapping("customer/selectStatus")
    Result<Integer> selectStatus();

    @RequestMapping("thresholdRange/select")
    public Page<ThresholdRange> select(@RequestParam(defaultValue = "1") int pageNum,
                                       @RequestParam(defaultValue = "10") int pageSize,
                                       @RequestParam(required = false) String metricName,
                                       @RequestParam(required = false) Integer buildingId,
                                       @RequestParam(required = false) Integer deviceId,
                                       @RequestParam(required = false) LocalDateTime startTime,
                                       @RequestParam(required = false) LocalDateTime endTime);
    @RequestMapping("thresholdRange/update")
    public int update(@RequestBody ThresholdRange thresholdRange);
    @RequestMapping("thresholdRange/insert")
    public int insert(@RequestBody ThresholdRange thresholdRange);

    @GetMapping("workerOrder/GetOrderByBuildingDevice")
    Result<List<WorkOrder>> GetOrderByBuildingDevice(@RequestParam(required = false) Long buildingId,
                                                     @RequestParam(required = false) Long deviceId);


    @GetMapping("deviceBuilding/queryBuildingIdAndName")
     Result<List<BuildingVo>> queryBuildingIdAndName();

    @RequestMapping("deviceBuilding/queryDeviceBuildingVo")
    Result<List<DeviceBuildingVo>> queryDeviceBuildingVo(@RequestParam Integer buildingId);
    @RequestMapping("deviceBuilding/queryDeviceBuildingVoByDeviceId")
    Result<List<DeviceStatusCountVo>> queryDeviceBuildingVoByDeviceId(
            @RequestParam(required = false) Integer BuildingId);

    @RequestMapping("cop/compute")
    Result<CopResult> copCompute(@RequestParam(required = false) Integer buildingId,
                                 @RequestParam(required = false)Integer deviceId);

    @RequestMapping("alertRecord/update")
    Result<String> update(@RequestBody AlertRecord alertRecord);

    @RequestMapping("alertRecord/select")
    Result<List<AlertRecord>> select();

    @RequestMapping("/building_optimization")
    Map<String,Object> buildingOptimization(@RequestParam(required = false) Integer buildingId,
                                                  @RequestParam(required = false) Integer deviceId);
    @GetMapping("api/reports/download/{fileName}")
    ResponseEntity<Resource> downloadReport(@PathVariable String fileName);
}