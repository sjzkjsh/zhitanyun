package com.example.Service.WorkOrderServiceImpl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.Entity.FaultStatusVO;
import com.example.Entity.FaultTypeStatVO;
import com.example.Entity.WorkOrder;
import com.example.Entity.WorkOrderListVO;
import com.example.Mapper.WorkOrderMapper;
import com.example.Service.WorkOrderService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkOrderServiceImpl extends ServiceImpl<WorkOrderMapper, WorkOrder> implements WorkOrderService {

    @Autowired
    private WorkOrderMapper workOrderMapper;


    @Override
    public Page<WorkOrderListVO> getWorkOrders(@Param("status") String status,
                                               @Param("priority") String priority,
                                               @Param("type") String type,
                                               @Param("orderNo") String orderNo,
                                               @Param("buildingId") Long buildingId,
                                               @Param("handlerId") Long handlerId,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime,
                                               @Param("overdue") Boolean overdue,
                                               @Param("page")int  page,
                                               @Param("size") int size) {

        Page<WorkOrderListVO> page1 = new Page(page,size);
        return workOrderMapper.pageQuery(page1,status, priority, type, orderNo, buildingId, handlerId, startTime, endTime, overdue);
    }

    @Override
    public WorkOrder getWorkerOrder(@Param("id") int id) {
        return workOrderMapper.getWorkOrder(id);
    }

    @Override
    public List<FaultTypeStatVO> getFaultTypeStat() {
        return workOrderMapper.countByType();
    }

    @Override
    public List<FaultStatusVO> getFaultStatusStat() {
        return workOrderMapper.countByStatus();
    }

    @Override
    public Page<WorkOrder> ListWorkOrder(int  page, int size) {
        Page<WorkOrder> page1 = new Page(page,size);
        return workOrderMapper.selectOverdueOrders(page1);
    }

    @Override
    public Long getWorkOrderCount() {
        return workOrderMapper.countOverdueOrders();
    }

    @Override
    public Page<WorkOrderListVO> WorkerOrderByStatus(Page<Object> page1, String workerStatus) {
        return workOrderMapper.WorkerOrderByStatus(page1, workerStatus);
    }

    @Override
    public List<WorkOrder> GetOrderByBuildingDevice(@RequestParam(required = false) Long buildingId,
                                                    @RequestParam(required = false) Long deviceId) {
        return workOrderMapper.GetOrderByBuildingDevice(buildingId, deviceId);
    }


}
