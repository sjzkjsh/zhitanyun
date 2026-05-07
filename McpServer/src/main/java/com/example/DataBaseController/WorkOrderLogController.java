package com.example.DataBaseController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Entity.ReultEntity.Result;
import com.example.Entity.WorkOrderLog;
import com.example.Service.WorkOrderLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/WorkOrderLog")
public class WorkOrderLogController {
    @Autowired
    private WorkOrderLogService workOrderLogService;
    @RequestMapping("/getWorkOrderLog")
    public Result<Page<WorkOrderLog>> getWorkOrderLog(@RequestParam(defaultValue = "1") int pageNum,
                                                      @RequestParam(defaultValue = "10") int pageSize
    , @RequestParam(required = false) Long orderId,@RequestParam(required = false) String action,
                                                      @RequestParam(required = false) Long operatorId
    ,@RequestParam(required = false) LocalDateTime startTime,
                                                      @RequestParam(required = false)LocalDateTime endTime){
        return Result.success(workOrderLogService.getWorkOrderLog(pageNum, pageSize, orderId, action, operatorId, startTime, endTime));
    }
}
