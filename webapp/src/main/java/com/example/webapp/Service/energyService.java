package com.example.webapp.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.webapp.Entity.Vo.MonthlyAnalysisVO;
import com.example.webapp.Entity.MonthlySummaryDTO;
import com.example.webapp.Entity.MonthlyTrendDTO;
import com.example.webapp.Entity.energyReadings;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface energyService extends IService<energyReadings> {

    List<MonthlyAnalysisVO> getYearlyAnalysis(String buildingCode, String deviceCode);

    MonthlySummaryDTO getMonthlySummary(String deviceCode, String buildingCode, String month);
    Map<String, Object> getDashboardData(String buildingCode, String deviceCode);
    List<MonthlyTrendDTO> getCurrentYearMonthlyTrend(String buildingCode, String deviceCode);
    List<energyReadings> queryEnergyReadings(LocalDateTime startTime, LocalDateTime endTime);
}
