package com.example.webapp.Database;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.webapp.Entity.*;

import com.example.webapp.Entity.Vo.WorkerOrderVo;
import com.example.webapp.Mapper.BuildingMapper;
import com.example.webapp.Mapper.CustomerMapper;
import com.example.webapp.Mapper.DeviceMapper;
import com.example.webapp.Mapper.WorkerOrderMapper;
import com.example.webapp.Mapper.WorkOrderLogMapper;
import com.example.webapp.Service.BuildingService;
import com.example.webapp.Service.DeviceService;
import com.example.webapp.Service.LoginService;
import com.example.webapp.Service.WorkerOrderService;
import com.example.webapp.Util.LoginCustomerHolder;
import com.example.webapp.Util.UserContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api")
public class WorkerOrderController {

    @Autowired
    private WorkerOrderService workerOrderService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private LoginService loginService;
    @Autowired
    private DeviceMapper deviceMapper;
    @Autowired
    private BuildingMapper buildingMapper;
    @Autowired
    private BuildingService buildingService;
    @Autowired
    private CustomerMapper customerMapper;
    @Autowired
    private WorkerOrderMapper workerOrderMapper;
    @Autowired
    private WorkOrderLogMapper workOrderLogMapper;
    @Autowired
    private UserContextUtil userContextUtil;
    @RequestMapping("/device")
    public buildingDevice getDevice(){
        String deviceCode = userContextUtil.getCurrentDeviceCode();
        String buildingCode = userContextUtil.getCurrentBuildingCode();
        buildingDevice buildingDevice = deviceService.selectBuildingAndDevice(deviceCode, buildingCode);
        return buildingDevice;
    }

    @PostMapping("/workerOrder")
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public boolean addWorkerOrder(@RequestBody WorkerOrderRequest  request){

        // 1. 获取当前登录用户（用 id 查询，更高效且避免同名问题）
        Long userId = LoginCustomerHolder.getLoginCustomer().getId();
        customer one = customerMapper.selectById(userId);

        if (one == null) {
            return false;
        }

        // 2. 先查询设备是否存在
        LambdaQueryWrapper<Devices> deviceQuery = new LambdaQueryWrapper<>();
        deviceQuery.eq(Devices::getDeviceCode, one.getDeviceCode());
        Devices device = deviceService.getOne(deviceQuery);
        if (device == null) {
            return false; // 设备不存在，直接返回失败
        }

        // 3. 更新设备状态（设备已确认存在）
        LambdaUpdateWrapper<Devices> devicesWrapper = new LambdaUpdateWrapper<>();
        devicesWrapper.eq(Devices::getDeviceCode, one.getDeviceCode())
                .set(Devices::getDeviceStatus, request.getType());
        deviceService.update(devicesWrapper);

        // 4. 查询建筑信息
        LambdaQueryWrapper<Buildings> buildingsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        buildingsLambdaQueryWrapper.eq(Buildings::getBuildingCode, one.getBuildingCode());
        Buildings buildings = buildingService.getOne(buildingsLambdaQueryWrapper);

        // 5. 组装工单对象
        WorkOrder workOrder = new WorkOrder();
        workOrder.setType(request.getType());
        workOrder.setDescription(request.getDescription());
        workOrder.setPriority(request.getPriority() != null ? request.getPriority() : "中");
        workOrder.setExpectedDeadline(request.getExpectedDeadline());
        workOrder.setLocation(buildings != null ? buildings.getLocation() : null);
        workOrder.setBuildingId(device.getBuildingId());
        workOrder.setEquipmentId(device.getDeviceId());
        workOrder.setOrderNo("WO" + System.currentTimeMillis());
        workOrder.setStatus("待处理");
        workOrder.setSubmitTime(new Date());
        workOrder.setCreatedAt(new Date());
        workOrder.setUpdatedAt(new Date());

        // 6. 保存工单
        boolean isSuccess = workerOrderService.save(workOrder);

        // 7. 记录工单日志（之前缺少 save 调用）
        if (isSuccess) {
            WorkOrderLog workOrderLog = new WorkOrderLog();
            workOrderLog.setOrderId(workOrder.getId());
            workOrderLog.setAction("创建工单");
            workOrderLog.setContent("用户ID " + userId + " 提交了故障工单");
            workOrderLog.setCreatedAt(new Date());
            workOrderLogMapper.insert(workOrderLog);
        }

        return isSuccess;
    }
    @DeleteMapping("/workerOrder/{orderNo}")
    public Result deleteWorkerOrder(@PathVariable("orderNo") String orderNo) {
        LambdaQueryWrapper<WorkOrder> workOrderLambdaQueryWrapper = new LambdaQueryWrapper<>();
        workOrderLambdaQueryWrapper.eq(WorkOrder::getOrderNo, orderNo);
        if (workerOrderService.remove(workOrderLambdaQueryWrapper)){
            return Result.success("删除成功");
        }else {
            return Result.error("删除失败");
        }
    }

    @GetMapping("/getWorkOrder")
    public Result<List<WorkerOrderVo>> getWorkOrder() {
        Long id = LoginCustomerHolder.getLoginCustomer().getId();
        customer customer = customerMapper.selectById(id);
        BuildingDeviceId id1 = buildingMapper.getId(customer.getDeviceCode(), customer.getBuildingCode());
        int buildingId = id1.getBuildingId();
        int deviceId = id1.getDeviceId();
        List<WorkerOrderVo> worker = workerOrderMapper.getWorkOrder(buildingId, deviceId);
        return Result.success(worker);
    }

}
