package com.example.webapp.Database;

import com.example.webapp.Entity.MonthlyTrendDTO;
import com.example.webapp.Entity.Result;
import com.example.webapp.Service.energyService;
import com.example.webapp.Util.UserContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class energyController {
    @Autowired
    private energyService energy;
    @Autowired
    private UserContextUtil userContextUtil;


    @RequestMapping("/energy")//获取数据
    public Result<Map<String, Object>> getDashboardData() {
        String deviceCode = userContextUtil.getCurrentDeviceCode();
        String buildingCode = userContextUtil.getCurrentBuildingCode();
        return Result.success(energy.getDashboardData(buildingCode, deviceCode));
    }

    @RequestMapping("/energyByYear")//获取数据
    public Result<List<MonthlyTrendDTO>> getCurrentYearMonthlyTrend(){
        String deviceCode = userContextUtil.getCurrentDeviceCode();
        String buildingCode = userContextUtil.getCurrentBuildingCode();
        return Result.success(energy.getCurrentYearMonthlyTrend(buildingCode, deviceCode));
    }
}
