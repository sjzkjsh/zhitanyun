package com.example.Repository;


import com.example.Entity.AnalysisEntity.DailyAnomalyScore;
import com.example.Entity.AnalysisEntity.DailyStats;
import com.example.Entity.AnalysisEntity.SelectedPeriod;
import com.example.Entity.AnalysisEntity.TimeBoundary;
import com.example.Mapper.EnergyReadingsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
public class SmartTimeSelector {

    @Autowired
    private EnergyReadingsMapper readingsMapper;

    /**
     * 智能选择最佳分析时段
     * 策略：找出异常密度最高、波动最大的连续时段
     */
    public SelectedPeriod selectBestPeriod(Integer deviceId, TimeBoundary boundary) {
        
        // 策略1：如果总数据少于7天，分析全部
        if (boundary.getTotal() < 168) {  // 168小时 = 7天
            return SelectedPeriod.builder()
                .start(boundary.getEarliest())
                .end(boundary.getLatest())
                .reason("数据量较少(" + boundary.getTotal() + "条)，分析全部可用数据")
                .expectedAnomalyCount((int) (boundary.getTotal() * 0.1))  // 预估10%异常
                .build();
        }

        // 策略2：扫描最近30天，找出异常高发日
        LocalDateTime latest = boundary.getLatest();
        LocalDateTime scanStart = latest.minusDays(30);
        
        List<DailyAnomalyScore> dailyScores = calculateDailyScores(deviceId, scanStart, latest);
        
        // 找出得分最高的连续3天
        List<DailyAnomalyScore> bestWindow = findBest3DayWindow(dailyScores);
        
        LocalDateTime bestStart = bestWindow.get(0).getDate().atStartOfDay();
        LocalDateTime bestEnd = bestWindow.get(bestWindow.size() - 1).getDate().atTime(23, 59, 59);
        
        int totalAnomalies = bestWindow.stream().mapToInt(DailyAnomalyScore::getAnomalyCount).sum();
        
        return SelectedPeriod.builder()
            .start(bestStart)
            .end(bestEnd)
            .reason(String.format("最近30天内异常高发时段：%s至%s，共%d个异常点，密度%.1f%%", 
                bestStart.toLocalDate(), bestEnd.toLocalDate(), 
                totalAnomalies, calculateDensity(bestWindow)))
            .expectedAnomalyCount(totalAnomalies)
            .dailyBreakdown(bestWindow)
            .build();
    }

    /**
     * 计算每日异常评分（多维度）
     */
    private List<DailyAnomalyScore> calculateDailyScores(Integer deviceId, LocalDateTime start, LocalDateTime end) {
        // 查询每日统计
        List<DailyStats> stats = readingsMapper.selectDailyStats(deviceId, start, end,100.0);
        
        List<DailyAnomalyScore> scores = new ArrayList<>();
        
        for (DailyStats stat : stats) {
            // 评分维度：
            // 1. 异常数量（直接超标）
            // 2. 波动程度（标准差）
            // 3. 峰值偏离度
            // 4. 数据新鲜度（越近越好）
            
            double anomalyScore = stat.getThresholdExceedCount() * 10;  // 直接异常，权重10
            
            double volatilityScore = stat.getPowerStd() != null ? 
                stat.getPowerStd().doubleValue() * 5 : 0;  // 波动大可能有问题
            
            double peakScore = stat.getMaxPower() != null && stat.getAvgPower() != null ?
                (stat.getMaxPower().doubleValue() / stat.getAvgPower().doubleValue() - 1) * 100 : 0;
            
            // 新鲜度加分（最近7天额外加分）
            double freshnessBonus = 0;
            long daysAgo = ChronoUnit.DAYS.between(stat.getDate(), LocalDateTime.now());
            if (daysAgo <= 7) freshnessBonus = 20;
            else if (daysAgo <= 14) freshnessBonus = 10;
            
            double totalScore = anomalyScore + volatilityScore + peakScore + freshnessBonus;
            
            scores.add(DailyAnomalyScore.builder()
                .date(stat.getDate())
                .anomalyCount(stat.getThresholdExceedCount())
                .maxPower(stat.getMaxPower())
                .avgPower(stat.getAvgPower())
                .volatility(stat.getPowerStd())
                .score(totalScore)
                .build());
        }
        
        return scores;
    }

    /**
     * 找出最佳3天窗口（滑动窗口）
     */
    private List<DailyAnomalyScore> findBest3DayWindow(List<DailyAnomalyScore> dailyScores) {
        if (dailyScores.size() <= 3) return dailyScores;
        
        double bestScore = -1;
        int bestIndex = 0;
        
        for (int i = 0; i <= dailyScores.size() - 3; i++) {
            double windowScore = dailyScores.get(i).getScore()
                + dailyScores.get(i + 1).getScore()
                + dailyScores.get(i + 2).getScore();
            
            // 额外加分：如果连续3天都有异常
            long continuousAnomaly = dailyScores.subList(i, i + 3).stream()
                .filter(d -> d.getAnomalyCount() > 0).count();
            if (continuousAnomaly == 3) windowScore += 15;  // 连续异常很重要！
            
            if (windowScore > bestScore) {
                bestScore = windowScore;
                bestIndex = i;
            }
        }
        
        return dailyScores.subList(bestIndex, bestIndex + 3);
    }

    private double calculateDensity(List<DailyAnomalyScore> window) {
        int totalRecords = window.size() * 24;  // 假设每天24条（小时级）
        int totalAnomalies = window.stream().mapToInt(DailyAnomalyScore::getAnomalyCount).sum();
        return totalRecords > 0 ? (totalAnomalies * 100.0 / totalRecords) : 0;
    }
}