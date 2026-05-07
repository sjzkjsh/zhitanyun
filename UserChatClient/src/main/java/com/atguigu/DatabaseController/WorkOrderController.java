package com.atguigu.DatabaseController;

import com.atguigu.FeignInterface.PageFeign;
import com.atguigu.Result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Entity.WorkOrderLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/WorkOrderLog")
public class WorkOrderController {
    @Autowired
    private PageFeign pageFeign;
    @RequestMapping("/getWorkOrderLog")
    public Result<Page<WorkOrderLog>> getWorkOrderLog(@RequestParam(defaultValue = "1")int  page,@RequestParam(defaultValue = "15") int size
    ,@RequestParam(required = false) Long orderId,@RequestParam(required = false) String action,
                                                      @RequestParam(required = false) Long operatorId
            ,@RequestParam(required = false) LocalDateTime startTime
    ,@RequestParam(required = false) LocalDateTime endTime){
        return pageFeign.getWorkOrderLog(page, size, orderId, action, operatorId, startTime, endTime);
    }

}
