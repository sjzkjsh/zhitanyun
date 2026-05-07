package com.example.webapp.Service.ServiceImpl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.example.webapp.Entity.*;

import com.example.webapp.Entity.ExcelVo.ComprehensiveExportVO;
import com.example.webapp.Entity.ExcelVo.CopExportVO;
import com.example.webapp.Entity.ExcelVo.CopHealthExportVO;
import com.example.webapp.Entity.ExcelVo.EnergyExportVO;
import com.example.webapp.Mapper.BuildingMapper;

import com.example.webapp.Mapper.CustomerMapper;
import com.example.webapp.Mapper.energyMapper;
import com.example.webapp.Service.*;
import com.example.webapp.Util.LoginCustomerHolder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportService {

    private final CopServiceImpl copService;
    private final CopHealthAnalysisService copHealthService;
    private final ComprehensiveAnalysisServiceImpl comprehensiveService;
    private final energyMapper energyMapper;
    private final CustomerMapper mapper;
    private final BuildingMapper buildingMapper;

    /**
     * 导出综合报表（多Sheet）
     */
    public void exportComprehensiveReport(HttpServletResponse response) throws IOException {
        // 1. 获取当前用户及设备信息
        Long customerId = LoginCustomerHolder.getLoginCustomer().getId();
        LambdaQueryWrapper<customer> wrapper = new LambdaQueryWrapper<customer>().eq(customer::getId, customerId);
        customer currentCustomer = mapper.selectOne(wrapper);
        BuildingDeviceId id = buildingMapper.getId(currentCustomer.getDeviceCode(), currentCustomer.getBuildingCode());
        int buildingId = id.getBuildingId();
        int deviceId = id.getDeviceId();

        // 2. 获取各项数据
        InstantCopResult copResult = copService.calculateInstantCop();
        CopHealthResult copHealth = copHealthService.analyzeCopHealth(copResult);
        LatestEnergyDTO energyData = energyMapper.getLatestEnergyFields(buildingId, deviceId);
        ComprehensiveAnalysisResult comprehensive = comprehensiveService.analyzeComprehensive(buildingId, deviceId);

        // 3. 转换数据为导出VO
        CopExportVO copExport = convertCop(copResult);
        EnergyExportVO energyExport = convertEnergy(energyData);
        CopHealthExportVO healthExport = convertHealth(copHealth);
        ComprehensiveExportVO compExport = convertComprehensive(comprehensive);

        // 4. 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("能效综合报表", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 5. 写入多Sheet
        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build();

        // Sheet1: 瞬时COP
        WriteSheet sheet1 = EasyExcel.writerSheet(0, "瞬时COP").head(CopExportVO.class).build();
        excelWriter.write(List.of(copExport), sheet1);

        // Sheet2: 基础能耗
        WriteSheet sheet2 = EasyExcel.writerSheet(1, "基础能耗").head(EnergyExportVO.class).build();
        excelWriter.write(List.of(energyExport), sheet2);

        // Sheet3: COP健康评估
        WriteSheet sheet3 = EasyExcel.writerSheet(2, "COP健康评估").head(CopHealthExportVO.class).build();
        excelWriter.write(List.of(healthExport), sheet3);

        // Sheet4: 全方位分析
        WriteSheet sheet4 = EasyExcel.writerSheet(3, "全方位分析").head(ComprehensiveExportVO.class).build();
        excelWriter.write(List.of(compExport), sheet4);

        excelWriter.finish();
        log.info("报表导出成功，用户：{}", currentCustomer.getName());
    }

    private CopExportVO convertCop(InstantCopResult src) {
        CopExportVO vo = new CopExportVO();
        vo.setCop(src.getCop());
        vo.setCoolingCapacity(src.getCoolingCapacity());
        vo.setPowerConsumption(src.getPowerConsumption());
        vo.setDeltaT(src.getDeltaT());
        vo.setWaterFlowRate(src.getWaterFlowRate());
        vo.setValid(src.isValid() ? "有效" : "无效");
        vo.setMessage(src.getMessage());
        return vo;
    }

    private EnergyExportVO convertEnergy(LatestEnergyDTO src) {
        EnergyExportVO vo = new EnergyExportVO();
        if (src != null) {
            vo.setPowerConsumption(src.getPowerConsumption());
            vo.setAcPowerConsumption(src.getAcPowerConsumption());
            vo.setAcInletTemp(src.getAcInletTemp());
            vo.setAcOutletTemp(src.getAcOutletTemp());
            vo.setWaterFlowRate(src.getWaterFlowRate());
            vo.setWaterConsumption(src.getWaterConsumption());
        }
        return vo;
    }

    private CopHealthExportVO convertHealth(CopHealthResult src) {
        CopHealthExportVO vo = new CopHealthExportVO();
        vo.setHealthLevel(src.getHealthLevel());
        vo.setScore(src.getScore());
        vo.setCop(src.getCop());
        vo.setDiagnosis(src.getDiagnosis());
        vo.setAbnormalItems(src.getAbnormalItems() != null ? String.join("；", src.getAbnormalItems()) : "");
        vo.setSuggestions(src.getSuggestions() != null ? String.join("；", src.getSuggestions()) : "");
        return vo;
    }

    private ComprehensiveExportVO convertComprehensive(ComprehensiveAnalysisResult src) {
        ComprehensiveExportVO vo = new ComprehensiveExportVO();
        vo.setOverallAssessment(src.getOverallAssessment());
        vo.setWarningLevel(src.getWarningLevel());
        vo.setPriorityActions(src.getPriorityActions() != null ? String.join("；", src.getPriorityActions()) : "");
        if (src.getCopHealth() != null) {
            vo.setCop(src.getCopHealth().getCop());
        }
        if (src.getEnvMetrics() != null) {
            vo.setEnvTemp(String.valueOf(src.getEnvMetrics().getOrDefault("envTemp", "")));
            vo.setHumidity(String.valueOf(src.getEnvMetrics().getOrDefault("humidity", "")));
            vo.setOccupancyDensity(String.valueOf(src.getEnvMetrics().getOrDefault("occupancyDensity", "")));
        }
        if (src.getEnergyMetrics() != null) {
            vo.setTotalPower((Double) src.getEnergyMetrics().getOrDefault("totalPower", 0.0));
            vo.setAcPower((Double) src.getEnergyMetrics().getOrDefault("acPower", 0.0));
        }
        return vo;
    }
}