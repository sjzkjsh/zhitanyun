package com.example.webapp.Database;

import com.alibaba.excel.EasyExcel;
import com.example.webapp.Entity.*;
import com.example.webapp.Entity.Vo.MonthlyAnalysisExportVO;
import com.example.webapp.Entity.Vo.MonthlyAnalysisVO;
import com.example.webapp.Service.LoginService;
import com.example.webapp.Service.energyService;
import com.example.webapp.Util.LoginCustomerHolder;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ReportController {

    @Autowired
    private LoginService loginService; // 用于获取用户信息
    @Autowired
    private energyService energy; // 用于查询能耗数据


    @GetMapping("/analysis/yearly")
    @Operation(summary = "获取年度月度能耗分析报表")
    public Result<List<MonthlyAnalysisVO>> getYearlyAnalysis() {
        // 1. 获取用户权限
        Long userId = LoginCustomerHolder.getLoginCustomer().getId();
        customer user = loginService.getById(userId); // 建议通过ID查询
        String deviceCode = user.getDeviceCode();
        String buildingCode = user.getBuildingCode();
        if (deviceCode == null) {
            return Result.fail("未绑定设备");
        }
        // 2. 调用 Service 分析逻辑
        List<MonthlyAnalysisVO> data = energy.getYearlyAnalysis(buildingCode, deviceCode);
        return Result.success(data);
    }
    @GetMapping("/export/analysis")
    @Operation(summary = "导出年度智能分析报告Excel")
    public void exportAnalysisReport(HttpServletResponse response) throws IOException {
        // 1. 获取当前登录用户信息
        Long userId = LoginCustomerHolder.getLoginCustomer().getId();
        customer user = loginService.getById(userId);
        String deviceCode = user.getDeviceCode();
        String buildingCode = user.getBuildingCode();
        if (deviceCode == null || deviceCode.isEmpty()) {
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\":500, \"message\":\"用户未绑定设备，无法导出\"}");
            return;
        }
        // 2. 【关键点】复用您已经实现的年度分析逻辑
        // 这个方法里已经包含了：查询数据 -> 计算环比 -> 判断状态 -> 生成建议
        List<MonthlyAnalysisVO> analysisData = energy.getYearlyAnalysis(buildingCode, deviceCode);
        // 3. 数据转换：将 VO 转换为 Excel 导出对象
        List<MonthlyAnalysisExportVO> exportList = analysisData.stream().map(vo -> {
            MonthlyAnalysisExportVO exportVo = new MonthlyAnalysisExportVO();
            exportVo.setMonth(vo.getMonth());
            exportVo.setPower(vo.getPower());
            exportVo.setWater(vo.getWater());
            // 格式化环比数据：例如 "12.5%" 或 "-5.0%"
            if (vo.getChangeRate() != null) {
                String prefix = vo.getChangeRate() > 0 ? "+" : "";
                exportVo.setChangeRateStr(prefix + String.format("%.1f", vo.getChangeRate()) + "%");
            } else {
                exportVo.setChangeRateStr("-");
            }
            exportVo.setTrend(vo.getTrend());
            exportVo.setStatus(vo.getStatus());
            exportVo.setSuggestion(vo.getSuggestion());
            return exportVo;
        }).collect(Collectors.toList());
        // 4. 设置响应头并生成 Excel 文件
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 文件名示例：智能能耗分析报告_DEV001.xlsx
        String fileName = URLEncoder.encode("智能能耗分析报告_" + deviceCode, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        // 5. 使用 EasyExcel 写入数据
        EasyExcel.write(response.getOutputStream(), MonthlyAnalysisExportVO.class)
                .sheet("年度能耗分析")
                .doWrite(exportList);
    }
}
