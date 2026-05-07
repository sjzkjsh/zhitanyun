package com.example.webapp.Entity;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class CopHealthResult {
    private String healthLevel;          // 健康等级：优秀、良好、一般、较差、异常
    private Double score;                // 综合评分 0-100
    private Double cop;                  // 当前 COP 值
    private String diagnosis;            // 诊断结论
    private List<String> suggestions;    // 改进建议
    private List<String> abnormalItems;  // 异常指标列表
    private boolean dataValid;           // 原始数据是否有效
    private String errorMessage;         // 错误信息（数据无效时）
}