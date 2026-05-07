package com.example.webapp.Service.ServiceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.webapp.Entity.*;
import com.example.webapp.Entity.Vo.MonthlyAnalysisVO;
import com.example.webapp.Mapper.BuildingMapper;
import com.example.webapp.Mapper.CustomerMapper;
import com.example.webapp.Mapper.energyMapper;
import com.example.webapp.Service.energyService;
import com.example.webapp.Util.LoginCustomerHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class energyServiceimpl extends ServiceImpl <energyMapper, energyReadings> implements energyService {

    @Autowired
    energyMapper mapper;
    @Autowired
    CustomerMapper customerMapper;
    @Autowired
    BuildingMapper buildingMapper;

    @Override//获取指定设备指定月份的月度总览
    public MonthlySummaryDTO getMonthlySummary(String deviceCode, String buildingCode, String month) {
        return mapper.getMonthlySummary(deviceCode, buildingCode, month);
    }

    @Override//获取设备指定月份的月度总览
    public Map<String, Object> getDashboardData(String buildingCode, String deviceCode) {
        MonthComparisonDTO comp = mapper.getMonthComparison(buildingCode, deviceCode);

        // 计算四个指标的环比（%）
        Map<String, Object> result = new HashMap<>();
        result.put("lastMonth", Map.of(
                "totalTce", formatNumber(comp.getLastMonthTce(), 1),      // 总能耗保留1位小数
                "totalPower", formatNumber(comp.getLastMonthPower(), 0),  // 电耗取整
                "totalWater", formatNumber(comp.getLastMonthWater(), 0),  // 水耗取整
                "totalAcPower", formatNumber(comp.getLastMonthAcPower(), 0) // 空调能耗取整
        ));
        result.put("comparison", Map.of(
                "tceChange", calcPercent(comp.getLastMonthTce(), comp.getPrevMonthTce()),
                "powerChange", calcPercent(comp.getLastMonthPower(), comp.getPrevMonthPower()),
                "waterChange", calcPercent(comp.getLastMonthWater(), comp.getPrevMonthWater()),
                "acChange", calcPercent(comp.getLastMonthAcPower(), comp.getPrevMonthAcPower())
        ));
        return result;
    }
    @Override//获取指定设备指定年份的月度分析
    public List<MonthlyAnalysisVO> getYearlyAnalysis(String buildingCode, String deviceCode) {
        // 1. 调用您已有的 Mapper 方法获取原始月度数据
        // 注意：您的 SQL 已经按时间 ASC 排序了，这对计算环比非常重要
        List<MonthlyTrendDTO> rawData = mapper.getCurrentYearMonthlyTrend(buildingCode, deviceCode);

        List<MonthlyAnalysisVO> resultList = new ArrayList<>();

        // 用于记录上一个月的数据，初始为 null
        Double lastMonthPower = null;

        for (MonthlyTrendDTO data : rawData) {
            MonthlyAnalysisVO vo = new MonthlyAnalysisVO();

            // 2. 基础数据填充
            vo.setMonth(data.getMonth());
            vo.setPower(data.getTotalPower());
            vo.setWater(data.getTotalWater());

            // 3. --- 核心分析逻辑 ---
            if (lastMonthPower != null && lastMonthPower > 0) {
                // 计算环比公式：(本月 - 上月) / 上月 * 100
                double change = (data.getTotalPower() - lastMonthPower) / lastMonthPower * 100;
                vo.setChangeRate(change);

                // 判定逻辑
                if (change > 15) {
                    vo.setTrend("大幅上升");
                    vo.setStatus("警告");
                    vo.setSuggestion("能耗环比增幅超15%，建议核查生产排班或设备能效");
                } else if (change < -10) {
                    vo.setTrend("显著下降");
                    vo.setStatus("良好");
                    vo.setSuggestion("节能效果显著，请总结本月经验");
                } else {
                    vo.setTrend("平稳");
                    vo.setStatus("正常");
                    vo.setSuggestion("-");
                }
            } else {
                // 第一个数据点没有上月数据，无法对比
                vo.setChangeRate(0.0);
                vo.setTrend("基准月");
                vo.setStatus("正常");
            }

            // 更新 lastMonthPower 供下一次循环使用
            lastMonthPower = data.getTotalPower();

            resultList.add(vo);
        }

        return resultList;
    }

    @Override
    public List<MonthlyTrendDTO> getCurrentYearMonthlyTrend(String buildingCode, String deviceCode) {
        return mapper.getCurrentYearMonthlyTrend(buildingCode, deviceCode);
    }

    @Override
    public List<energyReadings> queryEnergyReadings(@RequestParam(required = false) LocalDateTime startTime,
                                                    @RequestParam(required = false) LocalDateTime endTime) {
        Long id = LoginCustomerHolder.getLoginCustomer().getId();
        customer customer = customerMapper.selectById(id);
        BuildingDeviceId id1 = buildingMapper.getId(customer.getDeviceCode(), customer.getBuildingCode());
        int deviceId = id1.getDeviceId();
        int buildingId = id1.getBuildingId();
        return mapper.queryEnergyReadings(deviceId, buildingId, startTime, endTime);
    }

    private double calcPercent(Double current, Double previous) {
        if (previous == null || previous == 0) return 0;
        return (current - previous) / previous * 100;
    }

    private String formatNumber(Double value, int decimals) {
        if (value == null) return "0";
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(decimals, RoundingMode.HALF_UP);
        return bd.toString();
    }
}
