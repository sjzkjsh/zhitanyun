package com.example.Service.ImportService;

import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.Entity.Buildings;
import com.example.Entity.Devices;
import com.example.Entity.ExcelEntity.EnergyImportExcelDTO;
import com.example.Entity.ExcelEntity.ImportResultVO;
import com.example.Entity.energyReadings;
import com.example.Mapper.BuildingsMapper;
import com.example.Mapper.DevicesMapper;
import com.example.Mapper.EnergyReadingsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnergyImportService {
    private final BuildingsMapper buildingsMapper;
    private final DevicesMapper devicesMapper;
    private final EnergyReadingsMapper energyReadingsMapper;

    @Transactional(rollbackFor = Exception.class)
    public ImportResultVO importData(List<EnergyImportExcelDTO> dtoList, String filename) {
        ImportResultVO result = new ImportResultVO();
        result.setTotalCount(dtoList.size());
        if (dtoList == null || dtoList.isEmpty()) {
            return result;
        }
        // 1. 清洗数据
        List<EnergyImportExcelDTO> cleaned = cleanData(dtoList, result);
        // 2. 处理建筑
        Map<String, Integer> buildingMap = upsertBuildings(cleaned);
        // 3. 处理设备
        Map<String, Integer> deviceMap = upsertDevices(cleaned, buildingMap);
        // 4. 插入能耗数据
        insertReadings(cleaned, buildingMap, deviceMap, filename, result);

        // 统计成功数
        result.setSuccessCount((int) result.getErrors().stream()
                .filter(e -> "SUCCESS".equals(e.getStatus()))
                .count());

        return result;
    }

    private List<EnergyImportExcelDTO> cleanData(List<EnergyImportExcelDTO> list, ImportResultVO result) {
        List<EnergyImportExcelDTO> cleaned = new ArrayList<>();
        Set<String> uniqueKeys = new HashSet<>();

        for (EnergyImportExcelDTO dto : list) {
            // 清洗编码
            dto.setBuildingCode(dto.getBuildingCode().trim().toUpperCase());
            dto.setDeviceCode(dto.getDeviceCode().trim().toUpperCase());

            // 默认值
            if (dto.getDeviceStatus() == null) {
                dto.setDeviceStatus("正常");
            }

            // 数值归一化
            dto.setPowerConsumption(normalize(dto.getPowerConsumption()));
            dto.setWaterConsumption(normalize(dto.getWaterConsumption()));
            dto.setWaterFlowRate(normalize(dto.getWaterFlowRate()));
            dto.setAcPowerConsumption(normalize(dto.getAcPowerConsumption()));
            dto.setAcOutletTemp(normalize(dto.getAcOutletTemp()));
            dto.setAcInletTemp(normalize(dto.getAcInletTemp()));
            dto.setEnvTemp(normalize(dto.getEnvTemp()));
            dto.setHumidity(normalize(dto.getHumidity()));
            dto.setOccupancyDensity(normalize(dto.getOccupancyDensity()));
            dto.setAcPower(normalize(dto.getAcPower()));
            dto.setPowerLoad(normalize(dto.getPowerLoad()));

            // 校验
            if (dto.getMonitoringTime() == null) {
                result.addFail(dto.getRowNum(), dto.getBuildingCode(), dto.getBuildingName(),
                        dto.getDeviceCode(), dto.getDeviceType(), "监控时间不能为空");
                continue;
            }

            if (dto.getMonitoringTime().isAfter(LocalDateTime.now())) {
                result.addFail(dto.getRowNum(), dto.getBuildingCode(), dto.getBuildingName(),
                        dto.getDeviceCode(), dto.getDeviceType(), "监控时间不能是未来");
                continue;
            }

            // 去重
            String key = dto.getBuildingCode() + "_" + dto.getDeviceCode() + "_" + dto.getMonitoringTime();
            if (!uniqueKeys.add(key)) {
                result.addSkip(dto.getRowNum(), dto.getBuildingCode(), dto.getBuildingName(),
                        dto.getDeviceCode(), dto.getDeviceType(), "重复数据");
                continue;
            }

            // 清洗通过
            result.addSuccess(dto.getRowNum(), dto.getBuildingCode(), dto.getBuildingName(),
                    dto.getDeviceCode(), dto.getDeviceType());
            cleaned.add(dto);
        }
        return cleaned;
    }

    private Map<String, Integer> upsertBuildings(List<EnergyImportExcelDTO> list) {
        Set<String> codes = list.stream().map(EnergyImportExcelDTO::getBuildingCode).collect(Collectors.toSet());
        LambdaQueryWrapper<Buildings> query = new LambdaQueryWrapper<>();
        query.in(Buildings::getBuildingCode, codes);
        List<Buildings> existing = buildingsMapper.selectList(query);
        Map<String, Buildings> existingMap = existing.stream()
                .collect(Collectors.toMap(Buildings::getBuildingCode, b -> b, (k1, k2) -> k1));
        Map<String, Integer> result = new HashMap<>();
        for (EnergyImportExcelDTO dto : list) {
            String code = dto.getBuildingCode();
            if (result.containsKey(code)) continue;
            Buildings b = existingMap.get(code);
            if (b != null) {
                boolean needUpdate = false;
                if (StringUtils.isNotBlank(dto.getBuildingName()) && !dto.getBuildingName().equals(b.getBuildingName())) {
                    b.setBuildingName(dto.getBuildingName());
                    needUpdate = true;
                }
                if (StringUtils.isNotBlank(dto.getBuildingType()) && !dto.getBuildingType().equals(b.getBuildingType())) {
                    b.setBuildingType(dto.getBuildingType());
                    needUpdate = true;
                }
                if (StringUtils.isNotBlank(dto.getLocation()) && !dto.getLocation().equals(b.getLocation())) {
                    b.setLocation(dto.getLocation());
                    needUpdate = true;
                }
                if (needUpdate) buildingsMapper.updateById(b);
                result.put(code, b.getBuildingId());
            } else {
                Buildings newB = new Buildings();
                newB.setBuildingCode(code);
                newB.setBuildingName(StringUtils.isNotBlank(dto.getBuildingName()) ? dto.getBuildingName() : code);
                newB.setBuildingType(StringUtils.isNotBlank(dto.getBuildingType()) ? dto.getBuildingType() : "办公楼");
                newB.setLocation(dto.getLocation());
                newB.setCreatedAt(LocalDateTime.now());
                buildingsMapper.insert(newB);
                result.put(code, newB.getBuildingId());
            }
        }
        return result;
    }

    private Map<String, Integer> upsertDevices(List<EnergyImportExcelDTO> list, Map<String, Integer> buildingMap) {
        Set<String> codes = list.stream().map(EnergyImportExcelDTO::getDeviceCode).collect(Collectors.toSet());
        LambdaQueryWrapper<Devices> query = new LambdaQueryWrapper<>();
        query.in(Devices::getDeviceCode, codes);
        List<Devices> existing = devicesMapper.selectList(query);
        Map<String, Devices> existingMap = existing.stream()
                .collect(Collectors.toMap(Devices::getDeviceCode, d -> d, (k1, k2) -> k1));
        Map<String, Integer> result = new HashMap<>();
        for (EnergyImportExcelDTO dto : list) {
            String code = dto.getDeviceCode();
            if (result.containsKey(code)) continue;
            Integer buildingId = buildingMap.get(dto.getBuildingCode());
            Devices d = existingMap.get(code);
            if (d != null) {
                if (!buildingId.equals(d.getBuildingId())) {
                    d.setBuildingId(buildingId);
                    devicesMapper.updateById(d);
                }
                result.put(code, d.getDeviceId());
            } else {
                Devices newD = new Devices();
                newD.setDeviceCode(code);
                newD.setBuildingId(buildingId);
                newD.setDeviceType(StringUtils.isNotBlank(dto.getDeviceType()) ? dto.getDeviceType() : "综合采集器");
                newD.setDeviceStatus(dto.getDeviceStatus());
                newD.setInstallDate(parseDate(dto.getInstallTime()));
                newD.setCreatedAt(new java.util.Date());
                devicesMapper.insert(newD);
                result.put(code, newD.getDeviceId());
            }
        }
        return result;
    }

    private void insertReadings(List<EnergyImportExcelDTO> list,
                                Map<String, Integer> buildingMap,
                                Map<String, Integer> deviceMap,
                                String filename,
                                ImportResultVO result) {
        // 建立 rowNum 到 ErrorInfo 的映射
        Map<Integer, ImportResultVO.ErrorInfo> infoMap = result.getErrors().stream()
                .collect(Collectors.toMap(ImportResultVO.ErrorInfo::getRowNum, e -> e, (k1, k2) -> k1));

        for (EnergyImportExcelDTO dto : list) {
            ImportResultVO.ErrorInfo info = infoMap.get(dto.getRowNum());

            energyReadings r = new energyReadings();

            Integer buildingId = buildingMap.get(dto.getBuildingCode());
            if (buildingId == null) {
                log.error("无法找到建筑ID，建筑编号: {}", dto.getBuildingCode());
                if (info != null) {
                    info.setStatus("FAIL");
                    info.setStatusDesc("失败");
                    info.setErrorMsg("无法找到关联的建筑ID");
                }
                result.setFailCount(result.getFailCount() + 1);
                continue;
            }
            r.setBuildingId(buildingId);

            Integer deviceId = deviceMap.get(dto.getDeviceCode());
            if (deviceId == null) {
                log.error("无法找到设备ID，设备编号: {}", dto.getDeviceCode());
                if (info != null) {
                    info.setStatus("FAIL");
                    info.setStatusDesc("失败");
                    info.setErrorMsg("无法找到关联的设备ID");
                }
                result.setFailCount(result.getFailCount() + 1);
                continue;
            }
            r.setDeviceId(deviceId);

            r.setMonitoringTime(dto.getMonitoringTime());
            r.setPowerConsumption(dto.getPowerConsumption());
            r.setWaterConsumption(dto.getWaterConsumption());
            r.setWaterFlowRate(dto.getWaterFlowRate());
            r.setAcPowerConsumption(dto.getAcPowerConsumption());
            r.setAcOutletTemp(dto.getAcOutletTemp());
            r.setAcInletTemp(dto.getAcInletTemp());
            r.setEnvTemp(dto.getEnvTemp());
            r.setHumidity(dto.getHumidity());
            r.setOccupancyDensity(dto.getOccupancyDensity());
            r.setAcPower(dto.getAcPower());
            r.setPowerLoad(dto.getPowerLoad());
            r.setDataSource(StringUtils.isNotBlank(dto.getDataSource()) ? dto.getDataSource() : "EXCEL_IMPORT");
            r.setRawFile(StringUtils.isNotBlank(dto.getRawFile()) ? dto.getRawFile() : filename);
            r.setEndTime(dto.getEndTime());
            r.setCreatedAt(LocalDateTime.now());

            try {
                energyReadingsMapper.insert(r);
                // 保持 SUCCESS 状态
            } catch (Exception e) {
                if (e.getCause() != null && e.getCause().getMessage() != null &&
                        e.getCause().getMessage().contains("Duplicate entry")) {
                    if (info != null) {
                        info.setStatus("SKIP");
                        info.setStatusDesc("跳过");
                        info.setErrorMsg("数据库主键冲突");
                    }
                    result.setSkipCount(result.getSkipCount() + 1);
                } else {
                    log.error("数据库插入失败: {}", e.getMessage());
                    if (info != null) {
                        info.setStatus("FAIL");
                        info.setStatusDesc("失败");
                        info.setErrorMsg("数据库错误: " + e.getMessage());
                    }
                    result.setFailCount(result.getFailCount() + 1);
                }
            }
        }
    }

    private Double normalize(Double val) {
        return val == null || val < 0 ? 0.0 : val;
    }

    private Date parseDate(String str) {
        if (str == null || str.trim().isEmpty()) return null;
        try {
            return Date.valueOf(LocalDate.parse(str.replace("/", "-"), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } catch (Exception e) {
            return null;
        }
    }
}