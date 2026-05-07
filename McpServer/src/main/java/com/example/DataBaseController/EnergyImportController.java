package com.example.DataBaseController;
import com.alibaba.excel.EasyExcel;
import com.example.Entity.ExcelEntity.EnergyImportExcelDTO;
import com.example.Entity.ExcelEntity.ImportResultVO;
import com.example.Repository.EnergyImportListener; // 假设你已有这个监听器
import com.example.Service.ImportService.EnergyImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@Slf4j
@RestController
@RequestMapping("/api/energy")
@RequiredArgsConstructor
public class EnergyImportController {
    private final EnergyImportService energyImportService;
    /**
     * WebFlux 文件上传接口
     * 使用 @RequestPart("file") FilePart 接收文件流
     */
    @PostMapping("/import")
    public Mono<ResponseEntity<ImportResultVO>> importFile(@RequestPart("file") FilePart filePart) {
        String filename = filePart.filename();
        log.info("开始导入文件: {}", filename);
        // 1. 将 WebFlux 的 DataBuffer 流转换为 InputStream (同步流，用于 EasyExcel)
        // 注意：这里将文件读入内存，大文件请慎用或改用临时文件方案
        return DataBufferUtils.join(filePart.content())
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer); // 释放内存
                    return new ByteArrayInputStream(bytes);
                })
                .flatMap(inputStream -> {
                    // 2. 在弹性线程池中执行阻塞操作 (解析 + 入库)
                    // WebFlux 主线程不允许阻塞，必须切换线程
                    return Mono.fromCallable(() -> processImportSync(inputStream, filename))
                            .subscribeOn(Schedulers.boundedElastic());
                })
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("导入系统异常", e);
                    ImportResultVO errorVO = new ImportResultVO();
                    errorVO.setMessage("系统错误: " + e.getMessage());
                    // 注意：这里不手动设置 failCount，由业务层决定
                    return Mono.just(ResponseEntity.badRequest().body(errorVO));
                });
    }
    /**
     * 同步处理逻辑 (在独立线程中运行)
     * 包含：文件解析、数据清洗、调用Service
     */
    private ImportResultVO processImportSync(java.io.InputStream inputStream, String filename) {
        List<EnergyImportExcelDTO> dataList;
        List<ErrorRecord> errorList = new ArrayList<>();
        try {
            // 根据文件后缀选择解析方式
            if (filename.toLowerCase().endsWith(".csv")) {
                dataList = parseCsv(inputStream, errorList);
            } else {
                // Excel 处理
                dataList = parseExcel(inputStream, errorList);
            }
            // 如果解析阶段有错误，直接返回
            if (!errorList.isEmpty()) {
                ImportResultVO result = new ImportResultVO();
                result.setTotalCount(dataList.size() + errorList.size());
                result.setMessage("数据解析失败");
                for (ErrorRecord err : errorList) {
                    result.addParseError(err.rowNum, err.errorMsg);
                }
                return result;
            }
            // 调用 Service 进行入库
            return energyImportService.importData(dataList, filename);
        } catch (Exception e) {
            log.error("处理异常", e);
            throw new RuntimeException(e);
        }
    }
    // ================= 解析逻辑 =================
    private List<EnergyImportExcelDTO> parseExcel(java.io.InputStream inputStream, List<ErrorRecord> errorList) {
        EnergyImportListener listener = new EnergyImportListener();
        EasyExcel.read(inputStream, EnergyImportExcelDTO.class, listener)
                .headRowNumber(1)
                .sheet()
                .doRead();
        if (listener.getErrorList() != null) {
            for (EnergyImportExcelDTO err : listener.getErrorList()) {
                errorList.add(new ErrorRecord(err.getRowNum(), err.getDeviceCode(), err.getErrorMsg()));
            }
        }
        return listener.getDataList();
    }
    private List<EnergyImportExcelDTO> parseCsv(java.io.InputStream inputStream, List<ErrorRecord> errorList) throws Exception {
        List<EnergyImportExcelDTO> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {
            log.info("CSV表头: {}", csvParser.getHeaderNames());
            int rowNum = 1;
            for (CSVRecord record : csvParser) {
                rowNum++;
                EnergyImportExcelDTO dto = new EnergyImportExcelDTO();
                try {
                    // 建筑信息
                    dto.setBuildingCode(getCsvValue(record, "building_code", "buildingCode", "建筑编号"));
                    dto.setBuildingName(getCsvValue(record, "building_name", "buildingName", "建筑名称"));
                    dto.setBuildingType(getCsvValue(record, "building_type", "buildingType", "建筑类型"));
                    dto.setLocation(getCsvValue(record, "location", "地址", "位置"));
                    // 设备信息
                    dto.setDeviceCode(getCsvValue(record, "device_code", "deviceCode", "设备编号"));
                    dto.setDeviceType(getCsvValue(record, "device_type", "deviceType", "设备类型"));
                    dto.setInstallTime(getCsvValue(record, "install_date", "installDate", "install_time", "installTime", "安装时间"));
                    dto.setDeviceStatus(getCsvValue(record, "device_status", "deviceStatus", "设备状态"));
                    // 能耗基础数据
                    dto.setMonitoringTime(parseDateTime(getCsvValue(record, "monitoring_time", "monitoringTime", "time", "监控时间", "时间")));
                    dto.setPowerConsumption(parseDouble(getCsvValue(record, "power_consumption", "powerConsumption", "power", "电力能耗", "电耗", "能耗")));
                    dto.setWaterConsumption(parseDouble(getCsvValue(record, "water_consumption", "waterConsumption", "water", "水消耗", "水耗")));
                    dto.setWaterFlowRate(parseDouble(getCsvValue(record, "water_flow_rate", "waterFlowRate", "flow_rate", "flowRate", "水流量", "流量")));
                    dto.setAcPowerConsumption(parseDouble(getCsvValue(record, "ac_power_consumption", "acPowerConsumption", "ac_power", "acPower", "空调功耗", "空调能耗")));
                    dto.setAcOutletTemp(parseDouble(getCsvValue(record, "ac_outlet_temp", "acOutletTemp", "outlet_temp", "outletTemp", "空调出口温度", "出风温度")));
                    dto.setAcInletTemp(parseDouble(getCsvValue(record, "ac_inlet_temp", "acInletTemp", "inlet_temp", "inletTemp", "空调入口温度", "回风温度")));
                    dto.setEnvTemp(parseDouble(getCsvValue(record, "env_temp", "envTemp", "environment_temp", "environmentTemp", "环境温度")));
                    dto.setHumidity(parseDouble(getCsvValue(record, "humidity", "湿度")));
                    dto.setOccupancyDensity(parseDouble(getCsvValue(record, "occupancy_density", "occupancyDensity", "density", "人员密度", "密度")));
                    // --- 新增字段解析 ---
                    dto.setAcPower(parseDouble(getCsvValue(record, "ac_power", "acPower", "空调功率")));
                    dto.setPowerLoad(parseDouble(getCsvValue(record, "power_load", "powerLoad", "电力负载")));
                    dto.setDataSource(getCsvValue(record, "data_source", "dataSource", "数据来源"));
                    dto.setRawFile(getCsvValue(record, "raw_file", "rawFile", "原始文件"));
                    dto.setEndTime(parseDateTime(getCsvValue(record, "end_time", "endTime", "结束时间")));
                    // 设置行号，方便错误定位
                    dto.setRowNum(rowNum);
                    // 基础校验
                    if (dto.getBuildingCode() == null || dto.getBuildingCode().isEmpty()) {
                        errorList.add(new ErrorRecord(rowNum, "", "building_code is required"));
                        continue;
                    }
                    if (dto.getDeviceCode() == null || dto.getDeviceCode().isEmpty()) {
                        errorList.add(new ErrorRecord(rowNum, dto.getBuildingCode(), "device_code is required"));
                        continue;
                    }
                    if (dto.getMonitoringTime() == null) {
                        errorList.add(new ErrorRecord(rowNum, dto.getDeviceCode(), "monitoring_time format error"));
                        continue;
                    }
                    list.add(dto);
                } catch (Exception e) {
                    errorList.add(new ErrorRecord(rowNum, "", "parse error: " + e.getMessage()));
                }
            }
        }
        return list;
    }
    /**
     * 增强版取值：防止 BOM 头干扰，支持模糊匹配
     */
    private String getCsvValue(CSVRecord record, String... possibleNames) {
        // 1. 尝试直接映射
        for (String name : possibleNames) {
            if (record.isMapped(name)) {
                String value = record.get(name);
                return value != null && !value.trim().isEmpty() ? value.trim() : null;
            }
        }
        // 2. 遍历实际表头进行清洗比对
        Map<String, String> recordMap = record.toMap();
        for (String actualHeader : recordMap.keySet()) {
            // 去除 BOM (\uFEFF) 和 空格
            String cleanHeader = removeBOM(actualHeader).trim();
            for (String targetName : possibleNames) {
                if (cleanHeader.equalsIgnoreCase(targetName.trim())) {
                    String value = recordMap.get(actualHeader);
                    return value != null && !value.trim().isEmpty() ? value.trim() : null;
                }
            }
        }
        return null;
    }
    private String removeBOM(String input) {
        if (input == null) return null;
        if (input.startsWith("\uFEFF")) {
            return input.substring(1);
        }
        return input;
    }
    private LocalDateTime parseDateTime(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        String[] patterns = {
                "d/M/yyyy HH:mm:ss",      // 1/4/2026 08:00:00
                "d/M/yyyy H:mm:ss",       // 1/4/2026 8:00:00
                "dd/MM/yyyy HH:mm:ss",    // 01/04/2026 08:00:00
                "yyyy-MM-dd HH:mm:ss",
                "yyyy/MM/dd HH:mm:ss",
                "yyyy-MM-dd HH:mm",
                "yyyy/MM/dd HH:mm",
                "yyyy-MM-dd",
                "yyyy/MM/dd"
        };
        for (String pattern : patterns) {
            try {
                return LocalDateTime.parse(str.trim(), DateTimeFormatter.ofPattern(pattern));
            } catch (Exception e) {
                if (!pattern.contains("HH")) {
                    try {
                        return java.time.LocalDate.parse(str.trim(), DateTimeFormatter.ofPattern(pattern)).atStartOfDay();
                    } catch (Exception ignored) {}
                }
            }
        }
        return null;
    }
    private Double parseDouble(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        try {
            String cleaned = str.replace(",", "").replace("，", "").replace("kWh", "").replace("m³", "").replace("℃", "").replace("%", "").trim();
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    private record ErrorRecord(int rowNum, String deviceCode, String errorMsg) {}
}