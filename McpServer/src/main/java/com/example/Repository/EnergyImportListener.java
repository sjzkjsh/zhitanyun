package com.example.Repository;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.excel.metadata.data.ReadCellData;

import com.example.Entity.ExcelEntity.EnergyImportExcelDTO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * EasyExcel 能耗数据导入监听器
 * 功能：逐行解析Excel，收集有效数据和错误数据
 */
@Slf4j
@Getter
public class EnergyImportListener extends AnalysisEventListener<EnergyImportExcelDTO> {

    // 有效数据列表
    private final List<EnergyImportExcelDTO> dataList = new ArrayList<>();

    // 错误数据列表
    private final List<EnergyImportExcelDTO> errorList = new ArrayList<>();

    // 总行数（含表头）
    private int totalRows = 0;

    // 表头信息（列名 -> 列索引）
    private Map<Integer, String> headMap;

    /**
     * 每解析一行数据时调用
     */
    @Override
    public void invoke(EnergyImportExcelDTO data, AnalysisContext context) {
        totalRows++;
        int rowNum = context.readRowHolder().getRowIndex() + 1; // Excel行号从1开始
        data.setRowNum(rowNum);

        // 基础校验
        String errorMsg = validateBasic(data);

        if (errorMsg != null) {
            // 校验失败，加入错误列表
            data.setErrorMsg(errorMsg);
            errorList.add(data);
            log.warn("第{}行数据校验失败：{}", rowNum, errorMsg);
        } else {
            // 校验通过，加入有效列表
            dataList.add(data);
        }
    }

    /**
     * 解析表头时调用
     */
    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        log.info("解析到表头：{}", headMap.values());
        // 可以在这里校验表头是否完整
    }

    /**
     * 所有数据解析完成后调用
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("Excel解析完成：总行数={}, 有效数据={}, 错误数据={}",
                totalRows, dataList.size(), errorList.size());
    }

    /**
     * 解析异常时调用（如数据类型转换错误）
     */
    @Override
    public void onException(Exception exception, AnalysisContext context) {
        totalRows++;
        int rowNum = context.readRowHolder().getRowIndex() + 1;

        EnergyImportExcelDTO errorData = new EnergyImportExcelDTO();
        errorData.setRowNum(rowNum);

        if (exception instanceof ExcelDataConvertException) {
            // 数据类型转换错误
            ExcelDataConvertException ex = (ExcelDataConvertException) exception;
            int columnIndex = ex.getColumnIndex() + 1; // 列号从1开始
            errorData.setErrorMsg(String.format("第%d列数据格式错误", columnIndex));
            log.error("第{}行第{}列数据格式错误：{}", rowNum, columnIndex, ex.getMessage());
        } else {
            // 其他异常
            errorData.setErrorMsg("解析异常：" + exception.getMessage());
            log.error("第{}行解析异常：{}", rowNum, exception.getMessage());
        }

        errorList.add(errorData);
    }

    /**
     * 基础校验
     */
    private String validateBasic(EnergyImportExcelDTO data) {
        // 建筑编号校验
        if (data.getBuildingCode() == null || data.getBuildingCode().trim().isEmpty()) {
            return "建筑编号不能为空";
        }

        // 设备编号校验
        if (data.getDeviceCode() == null || data.getDeviceCode().trim().isEmpty()) {
            return "设备编号不能为空";
        }

        // 监控时间校验
        if (data.getMonitoringTime() == null) {
            return "监控时间不能为空或格式错误";
        }

        // 监控时间不能是未来
        if (data.getMonitoringTime().isAfter(java.time.LocalDateTime.now())) {
            return "监控时间不能是未来时间";
        }

        // 至少有一项能耗数据
        boolean hasEnergyData = data.getPowerConsumption() != null ||
                data.getWaterConsumption() != null ||
                data.getAcPowerConsumption() != null;

        if (!hasEnergyData) {
            return "至少填写一项能耗数据（电力、水或空调）";
        }

        return null; // 校验通过
    }
}