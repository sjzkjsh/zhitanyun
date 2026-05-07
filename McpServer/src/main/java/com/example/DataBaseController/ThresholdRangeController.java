package com.example.DataBaseController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Entity.AnalysisEntity.ThresholdRange;
import com.example.Mapper.ThresholdRangeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/thresholdRange")
public class ThresholdRangeController {

    @Autowired
    private ThresholdRangeMapper thresholdRangeMapper;
    @RequestMapping("/select")
    public Page<ThresholdRange> select(@RequestParam(defaultValue = "1") int pageNum,
                         @RequestParam(defaultValue = "10") int pageSize,
                         @RequestParam(required = false) String metricName,
                         @RequestParam(required = false) Integer buildingId,
                         @RequestParam(required = false) Integer deviceId,
                         @RequestParam(required = false) LocalDateTime startTime,
                         @RequestParam(required = false) LocalDateTime endTime) {
        Page<ThresholdRange> page=new Page<>(pageNum,pageSize);
        return thresholdRangeMapper.selectPageWithTimeRange(page, buildingId, deviceId, metricName, startTime, endTime);
    }
    @RequestMapping("/insert")
    public int insert(@RequestBody ThresholdRange thresholdRange) {
        return thresholdRangeMapper.insert(thresholdRange);
    }
    @RequestMapping("/update")
    public int update(@RequestBody ThresholdRange thresholdRange) {
        return thresholdRangeMapper.updateById(thresholdRange);
    }

}
