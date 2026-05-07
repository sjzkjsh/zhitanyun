package com.example.DataBaseController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Entity.*;
import com.example.Entity.ReultEntity.Result;
import com.example.Enum.WorkOrderStatus;
import com.example.Mapper.DevicesMapper;
import com.example.Mapper.WorkOrderMapper;
import com.example.Service.WorkOrderLogService;
import com.example.Service.WorkOrderService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/workerOrder")
public class WorkerOrder {
    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private WorkOrderLogService workOrderLogService;
    @Autowired
    private DevicesMapper devicesMapper;
    @Autowired
    private WorkOrderMapper workOrderMapper;

    @GetMapping("/getWorkOrders")//获取工单
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

        return Result.success(workOrderService.getWorkOrders(status, priority, type, orderNo
                , buildingId, handlerId, startTime, endTime, overdue, page, size));
    }
    @GetMapping("/getOneOrder/{id}")//获取工单
    public Result<WorkOrder> getOneOrder(@PathVariable("id") int id){
        return Result.success(workOrderService.getWorkerOrder(id));
    }

    @PostMapping("/saveOrUpdate")//创建工单
    public Result<Boolean> saveOrUpdate(@RequestBody WorkOrder workOrder){

            // 可以设置创建时的默认值，如状态、提交时间等
            workOrder.setStatus(WorkOrderStatus.待处理);
            workOrder.setSubmitTime(LocalDateTime.now());

            if(!workOrderService.save(workOrder)){
                return Result.success(false);
            }
        //修改建筑设备信息
        LambdaUpdateWrapper<Devices> devicesLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        devicesLambdaUpdateWrapper.eq(Devices::getDeviceId, workOrder.getEquipmentId());
        devicesLambdaUpdateWrapper.eq(Devices::getBuildingId, workOrder.getBuildingId());
        devicesLambdaUpdateWrapper.set(Devices::getDeviceStatus, workOrder.getType());
        devicesMapper.update(devicesLambdaUpdateWrapper);

            // 2. 记录操作日志
            WorkOrderLog log = new WorkOrderLog();
            log.setId(workOrder.getId());
            log.setAction("创建");
            log.setOrderId(workOrder.getHandlerId());
            log.setContent("创建工单：" + workOrder.getDescription());
            workOrderLogService.save(log);
            return Result.success(true);
    }

    @PostMapping("/UpdateById")
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public Result<Boolean> UpdateById(@RequestParam Long id,@RequestParam String status,@RequestParam Long userId){
        // 检查工单是否存在
        WorkOrder existing = workOrderService.getById(id);
        if (existing == null) {
            return Result.error("工单不存在");
        }

        // 乐观锁：设置新值，MyBatis-Plus 自动在 WHERE 中加 version=旧值
        existing.setStatus(WorkOrderStatus.valueOf(status));
        existing.setHandlerId(userId);
        existing.setUpdatedAt(LocalDateTime.now());
        // version 字段由 MyBatis-Plus 自动递增

        boolean updated = workOrderService.updateById(existing);
        if (!updated) {
            return Result.error("该工单已被其他人处理，请刷新后重试");
        }

        //修改建筑设备信息
        LambdaUpdateWrapper<Devices> devicesLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        devicesLambdaUpdateWrapper.eq(Devices::getDeviceId, existing.getEquipmentId());
        devicesLambdaUpdateWrapper.eq(Devices::getBuildingId, existing.getBuildingId());
        devicesLambdaUpdateWrapper.set(Devices::getDeviceStatus, existing.getType());
        devicesMapper.update(devicesLambdaUpdateWrapper);

        // 记录操作日志
        WorkOrderLog log = new WorkOrderLog();
        log.setOrderId(id);
        log.setAction("更新状态");
        log.setContent("更新工单状态为：" + status + "，处理人ID：" + userId);
        workOrderLogService.save(log);

        return Result.success(true);
    }
    // 根据工单类型分组统计和工单数量，分为设备故障，设备保养，其他这三种
    @GetMapping("/CountByType")
    public Result<List<FaultTypeStatVO>> CountByType(){
        return Result.success(workOrderService.getFaultTypeStat());
    }

    // 根据工单状态分组，统计工单状态和工单数量，分为待处理，处理中，已完成，已关闭
    @GetMapping("/CountByStatus")
    public Result<List<FaultStatusVO>> CountByStatus(){
        return Result.success(workOrderService.getFaultStatusStat());
    }
    // 根据工单状态查询工单
    @GetMapping("/WorkerOrderByStatus")
    public Result<Page<WorkOrderListVO>> WorkerOrderByStatus(@RequestParam(defaultValue = "1") int page,
                                                             @RequestParam(defaultValue = "10") int size,
                                                             @RequestParam(defaultValue = "待处理")String workerStatus){
        Page<Object> page1 = new Page<>(page, size);
        return Result.success(workOrderService.WorkerOrderByStatus(page1,workerStatus));
    }
    @GetMapping("/GetOrderByBuildingDevice")
    public Result<List<WorkOrder>> GetOrderByBuildingDevice(@RequestParam(required = false) Long buildingId,
                                                            @RequestParam(required = false) Long deviceId){
        return Result.success(workOrderService.GetOrderByBuildingDevice(buildingId,deviceId));
    }

    // 获取所有异常工单
    @GetMapping("/errorOrder")
    public Result<Page<WorkOrder>> errorOrder(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size){
        return Result.success(workOrderService.ListWorkOrder(page, size));
    }
    // 获取工单数量
    @GetMapping("/count")
    public Result<Long> count(){
        return Result.success(workOrderService.getWorkOrderCount());
    }


    //根据设备状态分组，统计工单数量和查询创建的日期,用于折线图
    @GetMapping("/StatusWorkerOrder")
    public Result<List<WorkOrderStatusCountVO>> StatusWorkerOrder(){
        return Result.success(workOrderMapper.countByStatusAndDate());
    }
}
