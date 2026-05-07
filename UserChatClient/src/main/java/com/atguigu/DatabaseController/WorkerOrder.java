package com.atguigu.DatabaseController;


import com.atguigu.FeignInterface.PageFeign;
import com.atguigu.Result.Result;
import com.atguigu.Util.LoginUserHolder;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Entity.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/workerOrder")
public class WorkerOrder {

    @Autowired
    private PageFeign feign;
    @RequestMapping("/getWorkOrders")
    public Result<Page<WorkOrderListVO>> getWorkOrders(@RequestParam(required = false)@Param("status") String status,
                                                                                      @RequestParam(required = false)@Param("priority") String priority,
                                                                                      @RequestParam(required = false)@Param("type") String type,
                                                                                      @RequestParam(required = false)@Param("orderNo") String orderNo,
                                                                                      @RequestParam(required = false)@Param("buildingId") Long buildingId,
                                                                                      @RequestParam(required = false)@Param("handlerId") Long handlerId,
                                                                                      @RequestParam(required = false)@Param("startTime") LocalDateTime startTime,
                                                                                      @RequestParam(required = false)@Param("endTime") LocalDateTime endTime,
                                                                                      @RequestParam(required = false)@Param("overdue") Boolean overdue,
                                                                                      @RequestParam(required = false,defaultValue = "1")@Param("page")int  page,
                                                                                      @RequestParam(required = false,defaultValue = "10")@Param("size") int size){
        return feign.getWorkOrders(status, priority, type, orderNo, buildingId, handlerId, startTime, endTime, overdue, page, size);
    }
    @GetMapping("/WorkerOrderByStatus")
    public Result<Page<WorkOrderListVO>> WorkerOrderByStatus(@RequestParam(defaultValue = "1") int page,
                                                                                            @RequestParam(defaultValue = "10") int size,
                                                                                            @RequestParam(defaultValue = "待处理")String workerStatus){
            return feign.WorkerOrderByStatus(page, size, workerStatus);
         }

    //根据设备状态分组，统计工单数量和查询创建的日期
    @GetMapping("/StatusWorkerOrder")
    public Result<List<WorkOrderStatusCountVO>> StatusWorkerOrder(){
        return feign.StatusWorkerOrder();
    }

    @GetMapping("getOneOrder/{id}")
    public Result<WorkOrder> getOneOrder(@PathVariable("id")  int id){
        return feign.getOneOrder(id);
    }
    @PostMapping("saveOrUpdate")
    public Result<Boolean> saveOrUpdate(@RequestBody WorkOrder workOrder){
        return feign.saveOrUpdate(workOrder);
    }
    @PostMapping("UpdateById")
    public Result<Boolean> UpdateById(@RequestParam int id,@RequestParam String status){
        Long userId = LoginUserHolder.getLoginUser().getUserId();
        return feign.UpdateById(id, status, userId);
    }
    @GetMapping("CountByType")
    public Result<List<FaultTypeStatVO>> CountByType(){
        return feign.CountByType();
    }
    @GetMapping("CountByStatus")
    public Result<List<FaultStatusVO>> CountByStatus(){
        return feign.CountByStatus();
    }
    @GetMapping("/errorOrder")
    public Result<Page<WorkOrder>> errorOrder(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size){
        return feign.errorOrder(page, size);
    }
    @GetMapping("/count")
    public Result<Long> count(){
        return feign.count();
    }

    @GetMapping("/GetOrderByBuildingDevice")
    public Result<List<WorkOrder>> GetOrderByBuildingDevice(@RequestParam(required = false) Long buildingId,
                                                            @RequestParam(required = false) Long deviceId){
        return feign.GetOrderByBuildingDevice(buildingId, deviceId);
    }
}
