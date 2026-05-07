package com.example.Service.WorkOrderServiceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.Entity.WorkOrderLog;
import com.example.Mapper.WorkOrderLogMapper;
import com.example.Service.WorkOrderLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Service
public class WorkOrderLogServiceImpl extends ServiceImpl<WorkOrderLogMapper, WorkOrderLog> implements WorkOrderLogService {

    @Autowired
    private WorkOrderLogMapper workOrderLogMapper;

    @Override
    public Page<WorkOrderLog> getWorkOrderLog(@RequestParam int pageNum,
                                              @RequestParam int pageSize,
                                              Long orderId,
                                              String action,
                                              Long operatorId,
                                              LocalDateTime startTime,
                                              LocalDateTime endTime) {
        Page<WorkOrderLog> page=new Page<>(pageNum,pageSize);
        return workOrderLogMapper.select(page,orderId, action, operatorId,startTime, endTime );

    }
}
