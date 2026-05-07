package com.example.webapp.Service.ServiceImpl;

import com.example.webapp.Entity.CopHealthResult;
import com.example.webapp.Entity.InstantCopResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CopHealthAnalysisService {

    /**
     * 根据瞬时 COP 结果评估健康状态
     */
    public CopHealthResult analyzeCopHealth(InstantCopResult copResult) {
        CopHealthResult result = new CopHealthResult();
        
        if (!copResult.isValid()) {
            result.setDataValid(false);
            result.setErrorMessage(copResult.getMessage());
            result.setHealthLevel("未知");
            result.setScore(0.0);
            return result;
        }

        result.setDataValid(true);
        result.setCop(copResult.getCop());
        
        double cop = copResult.getCop();
        double deltaT = copResult.getDeltaT();
        double flowRate = copResult.getWaterFlowRate();
        double power = copResult.getPowerConsumption();
        double cooling = copResult.getCoolingCapacity();

        List<String> abnormalItems = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        
        // 1. COP 评分 (0-50分)
        double copScore = 0;
        String copLevel;
        if (cop >= 5.0) {
            copScore = 50;
            copLevel = "优秀";
        } else if (cop >= 4.0) {
            copScore = 40;
            copLevel = "良好";
        } else if (cop >= 3.0) {
            copScore = 30;
            copLevel = "一般";
            suggestions.add("建议检查制冷剂充注量或清洗冷凝器，提升能效");
            abnormalItems.add("COP偏低(低于4.0)");
        } else if (cop >= 2.0) {
            copScore = 20;
            copLevel = "较差";
            suggestions.add("COP严重偏低，可能存在制冷剂泄漏、压缩机故障或换热器脏堵，请立即排查");
            abnormalItems.add("COP严重偏低(低于3.0)");
        } else {
            copScore = 0;
            copLevel = "异常";
            suggestions.add("COP异常，建议立即停机检查制冷系统");
            abnormalItems.add("COP异常(低于2.0)");
        }

        // 2. 温差评分 (0-20分)
        double deltaTScore = 0;
        if (deltaT >= 4.5 && deltaT <= 6.5) {
            deltaTScore = 20;
        } else if (deltaT >= 3.5 && deltaT <= 7.5) {
            deltaTScore = 15;
            suggestions.add("供回水温差略偏离标准范围(4.5~6.5℃)，检查水流量或末端负荷");
            abnormalItems.add("温差异常");
        } else {
            deltaTScore = 5;
            suggestions.add("供回水温差严重偏离标准，可能存在水流短路、传感器故障或负荷不匹配");
            abnormalItems.add("温差严重异常");
        }

        // 3. 单位制冷量功耗评估 (0-20分) —— 制冷量/功耗 ≈ COP 已涵盖，可用其他指标
        // 此处简化为基于功率/制冷量比值
        double efficiencyRatio = cooling / power; // 其实就是 COP
        
        // 4. 综合评分
        double totalScore = copScore + deltaTScore;
        result.setScore(Math.min(totalScore, 100));
        
        // 确定健康等级
        if (totalScore >= 90) result.setHealthLevel("优秀");
        else if (totalScore >= 75) result.setHealthLevel("良好");
        else if (totalScore >= 60) result.setHealthLevel("一般");
        else if (totalScore >= 40) result.setHealthLevel("较差");
        else result.setHealthLevel("异常");

        // 生成诊断结论
        result.setDiagnosis(String.format("当前COP为%.2f，供回水温差%.1f℃。%s",
                cop, deltaT, copLevel.equals("优秀") ? "系统运行高效" : "存在能效提升空间"));
        
        result.setAbnormalItems(abnormalItems);
        result.setSuggestions(suggestions);
        
        return result;
    }
}