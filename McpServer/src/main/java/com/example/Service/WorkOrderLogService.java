package com.example.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.Entity.WorkOrderLog;

import java.time.LocalDateTime;

public interface WorkOrderLogService extends IService<WorkOrderLog> {
     Page<WorkOrderLog> getWorkOrderLog(int page, int size, Long orderId, String action, Long operatorId, LocalDateTime startTime, LocalDateTime endTime);
}
