package com.example.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.Entity.FaultStatusVO;
import com.example.Entity.FaultTypeStatVO;
import com.example.Entity.WorkOrder;
import com.example.Entity.WorkOrderListVO;
import org.apache.ibatis.annotations.Param;
import org.hibernate.jdbc.Work;

import java.time.LocalDateTime;
import java.util.List;


public interface WorkOrderService extends IService<WorkOrder> {
    Page<WorkOrderListVO> getWorkOrders(@Param("status") String status,
                                        @Param("priority") String priority,
                                        @Param("type") String type,
                                        @Param("orderNo") String orderNo,
                                        @Param("buildingId") Long buildingId,
                                        @Param("handlerId") Long handlerId,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime,
                                        @Param("overdue") Boolean overdue,
                                        @Param("page") int  page,
                                        @Param("size")int  size);


    WorkOrder getWorkerOrder(@Param("id") int id);

    List<FaultTypeStatVO> getFaultTypeStat();


    List<FaultStatusVO> getFaultStatusStat();

    Page<WorkOrder> ListWorkOrder(int page,int size);
    Long getWorkOrderCount();

    Page<WorkOrderListVO> WorkerOrderByStatus(Page<Object> page1,String workerStatus);

    List<WorkOrder> GetOrderByBuildingDevice(Long buildingId, Long deviceId);
}
