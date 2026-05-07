package com.example.Service.DataBaseService;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Entity.BuildingDeviceVo;
import com.example.Entity.DeviceEnergyBuildingVO;
import com.example.Mapper.DevicesMapper;
import com.example.Mapper.EnergyReadingsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PageService {
    @Autowired
    private EnergyReadingsMapper energyReadingMapper;

    @Autowired
    private DevicesMapper devicesMapper;



    public Page<DeviceEnergyBuildingVO> queryVO(
            Integer buildingId,
            Integer deviceId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String buildingType,
            String deviceStatus,
            String deviceCode,
            int pageNum,
            int pageSize) {
        Page<DeviceEnergyBuildingVO> page = new Page<>(pageNum, pageSize);
        return energyReadingMapper.selectVOWithConditions(
                page, buildingId, deviceId, startTime, endTime,
                buildingType, deviceStatus, deviceCode);
    }

    public List<DeviceEnergyBuildingVO> queryVOForExport(
            Integer buildingId,
            Integer deviceId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String buildingType,
            String deviceStatus,
            String deviceCode) {
        return energyReadingMapper.queryVOForExport(
                buildingId, deviceId, startTime, endTime,
                buildingType, deviceStatus, deviceCode);
    }

    public Page<BuildingDeviceVo> PageDevices(int pageNum,
                                              int pageSize,
                                              @RequestParam(required = false) String deviceType,
                                              @RequestParam(required = false) String buildingName,
                                              @RequestParam(required = false) String deviceStatus,
                                              @RequestParam(required = false) String buildingCode) {
        Page<DeviceEnergyBuildingVO> page = new Page<>(pageNum, pageSize);
        return devicesMapper.getPageDevices(page, deviceType, buildingName, deviceStatus, buildingCode);
    }
}