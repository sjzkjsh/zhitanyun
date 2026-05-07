package com.example.DataBaseController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.Entity.AlertRecord;
import com.example.Entity.ReultEntity.Result;
import com.example.Service.AlertRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;

import java.util.List;

@RestController
@RequestMapping("/alertRecord")
public class AlertRecordController {
    @Autowired
    private AlertRecordService alertRecordService;

    @RequestMapping("/update")
    public Result<String> update(@RequestBody AlertRecord alertRecord) {
        if(alertRecordService.saveOrUpdate(alertRecord))
        {
            return Result.success();
        }
            return Result.error("更新失败");
    }
    @RequestMapping("/select")
    public Result<List<AlertRecord>> select() {
        LambdaQueryWrapper<AlertRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlertRecord::getStatus, AlertRecord.STATUS_PENDING);
        List<AlertRecord> a = alertRecordService.list(wrapper);
        return Result.success(a);
    }
}
