package com.example.McpServices;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.Entity.WorkOrder;
import com.example.Entity.WorkOrderLog;
import com.example.Mapper.WorkOrderLogMapper;
import com.example.Mapper.WorkOrderMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkOrderMcpService {

    @Autowired
    private WorkOrderMapper workOrderMapper;
    @Autowired
    private WorkOrderLogMapper workOrderLogMapper;

    @Tool(name = "countWorkerOrder",description = "统计超时工单数量")
    public Long getWorkOrder() {
        return workOrderMapper.countOverdueOrders();
    }

    @Tool(name = "queryWorkerOrder",description = "查询待处理工单的信息")
    public List<WorkOrder> getWorkOrderList(@ToolParam(required = false) Long buildingId,
                                            @ToolParam(required = false) Long deviceId) {
        return workOrderMapper.GetOrderByBuildingDevice(buildingId, deviceId);
    }

    @Tool(name = "queryWorkerOrderLog"
            ,description = "查询工单日志")
    public List<WorkOrderLog> getWorkOrderLogList(@ToolParam(required = false) Long orderId,
                                                  @ToolParam(required = false) String action,
                                                  @ToolParam(required = false) Long operatorId,
                                                  @ToolParam(required = false) LocalDateTime startTime,
                                                  @ToolParam(required = false) LocalDateTime endTime){
        LambdaQueryWrapper<WorkOrderLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(orderId != null, WorkOrderLog::getOrderId, orderId)
                .eq(action != null, WorkOrderLog::getAction, action)
                .eq(operatorId != null, WorkOrderLog::getOperatorId, operatorId)
                .ge(startTime != null, WorkOrderLog::getCreatedAt, startTime)
                .le(endTime != null, WorkOrderLog::getCreatedAt, endTime);
        return workOrderLogMapper.selectList(wrapper);
    }
}
