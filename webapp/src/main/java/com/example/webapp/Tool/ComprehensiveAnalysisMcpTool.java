package com.example.webapp.Tool;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.webapp.Entity.BuildingDeviceId;
import com.example.webapp.Entity.ComprehensiveAnalysisResult;
import com.example.webapp.Entity.customer;
import com.example.webapp.Mapper.BuildingMapper;
import com.example.webapp.Service.LoginService;
import com.example.webapp.Service.ServiceImpl.ComprehensiveAnalysisServiceImpl;
import com.example.webapp.Util.LoginCustomerHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ComprehensiveAnalysisMcpTool {

    private final ComprehensiveAnalysisServiceImpl analysisService;
    private final LoginService loginService;
    private final BuildingMapper buildingMapper;

    @Tool(name = "全面分析"
            ,description = """
            获取当前客户绑定设备的全方位能效分析报告，包含COP健康评估、能耗指标、环境参数和优化建议。
            当用户询问：全面分析、综合评估、运行状况、节能建议 时调用。
            """)
    public ComprehensiveAnalysisResult getComprehensiveAnalysis() {
        String customerName = LoginCustomerHolder.getLoginCustomer().getName();
        LambdaQueryWrapper<customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(customer::getName, customerName);
        customer currentCustomer = loginService.getOne(wrapper);
        BuildingDeviceId id = buildingMapper.getId(currentCustomer.getDeviceCode(), currentCustomer.getBuildingCode());
        return analysisService.analyzeComprehensive(id.getBuildingId(), id.getDeviceId());
    }
}