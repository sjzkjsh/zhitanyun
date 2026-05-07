package com.atguigu.DatabaseController;

import com.atguigu.FeignInterface.PageFeign;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Entity.AnalysisEntity.ThresholdRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/thresholdRange")
public class thresholdRangeController {
    @Autowired
    PageFeign pageFeign;

    @RequestMapping("thresholdRange/select")
    public Page<ThresholdRange> select(@RequestParam(defaultValue = "1") int pageNum,
                                       @RequestParam(defaultValue = "10") int pageSize,
                                       @RequestParam(required = false) String metricName,
                                       @RequestParam(required = false) Integer buildingId,
                                       @RequestParam(required = false) Integer deviceId,
                                       @RequestParam(required = false) LocalDateTime startTime,
                                       @RequestParam(required = false) LocalDateTime endTime){
        return pageFeign.select(pageNum,pageSize,metricName,buildingId,deviceId,startTime,endTime);
    }
    @RequestMapping("thresholdRange/update")
    public int update(@RequestBody ThresholdRange thresholdRange){
        return pageFeign.update(thresholdRange);
    }
    @RequestMapping("thresholdRange/insert")
    public int insert(@RequestBody ThresholdRange thresholdRange){
        return pageFeign.insert(thresholdRange);
    }
}
