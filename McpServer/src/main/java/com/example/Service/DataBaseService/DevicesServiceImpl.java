package com.example.Service.DataBaseService;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.Entity.BuildingDeviceVo;
import com.example.Entity.DeviceEnergyBuildingVO;
import com.example.Entity.DeviceEnergyVo;
import com.example.Entity.Devices;
import com.example.Mapper.DevicesMapper;
import com.example.Repository.BloomFilterHelper;
import com.example.Service.DevicesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.example.Repository.BloomFilterHelper.BLOOM_FILTER_DEVICE;


@Service
public class DevicesServiceImpl extends ServiceImpl<DevicesMapper, Devices> implements DevicesService {

    @Autowired
    private DevicesMapper devicesMapper;
    @Autowired
    private BloomFilterHelper bloomFilterHelper;

    //根据建筑id查询设备信息
    @Cacheable(value = "devices",
            key = "'buildingId:' + #buildingId",
            unless = "#result == null || #result.isEmpty()")
    @Override
    public List<BuildingDeviceVo> queryDevicesbyBuilding(int buildingId) {
        if(!bloomFilterHelper.mightContain(BLOOM_FILTER_DEVICE, buildingId)){
            return null;
        }
        return devicesMapper.queryDevicesbyBuildingId(buildingId);
    }
//    根据建筑编号查询设备信息
    @Cacheable(value = "devices",
            key = "'buildingCode:' + #buildingCode",
            unless = "#result == null || #result.isEmpty()")
    @Override
    public List<BuildingDeviceVo> queryDevicesbyBuilding(String buildingCode) {
        if(!bloomFilterHelper.mightContain(BLOOM_FILTER_DEVICE, buildingCode)){
            return null;
        }
        return devicesMapper.queryDevicesbyBuildingCode(buildingCode);
    }

    //根据建筑名称查询设备信息
    @Cacheable(value = "devices",
               key = "'buildingName:' + #buildingName",
               unless = "#result == null || #result.isEmpty()")
    @Override
    public List<BuildingDeviceVo> queryDevicesbyBuildingName(String buildingName) {
        if(!bloomFilterHelper.mightContain(BLOOM_FILTER_DEVICE, buildingName)){
            return null;
        }
        return devicesMapper.queryDevicesbyBuildingName(buildingName);
    }

    //查询所有设备的运行状态和设备信息
    @Cacheable(value = "devices",
                key = "'all'",
                unless = "#result == null || #result.isEmpty()")
    @Override
    public List<BuildingDeviceVo> getDevices() {

        return devicesMapper.getAllDevices();
    }

    //根据设备id查询设备运行状态和设备信息
    @Cacheable(value = "devices",
                key = "'deviceId:' + #devicesId",
                unless = "#result == null || #result.isEmpty()")
    @Override
    public List<BuildingDeviceVo> getDevices(int devicesId) {
        if(!bloomFilterHelper.mightContain(BLOOM_FILTER_DEVICE, devicesId)){
            return null;

        }
        return devicesMapper.queryDeviceById(devicesId);
    }

    //根据设备编号查询设备运行状态和设备信息



    @Cacheable(value = "devices",
                key = "'deviceCode:' + #deviceCode",
                unless = "#result == null || #result.isEmpty()")
    @Override
    public List<BuildingDeviceVo> getDevices(String deviceCode) {
        if(!bloomFilterHelper.mightContain(BLOOM_FILTER_DEVICE, deviceCode)){
            return null;
        }
        return devicesMapper.queryDeviceByCode(deviceCode);
    }

    //根据设备类型查询设备运行状态和设备信息

    @Cacheable(value = "devices",
                key = "'deviceType:' + #deviceType",
                unless = "#result == null || #result.isEmpty()")
    @Override
    public List<BuildingDeviceVo> queryDeviceStatusbyType(String deviceType) {
        if(!bloomFilterHelper.mightContain(BLOOM_FILTER_DEVICE, deviceType)){
            return null;
        }
        return devicesMapper.queryDeviceStatusbyType(deviceType);
    }

    @Cacheable(value = "devices",
                key = "'countDeviceException'",
                unless = "#result == 0")
    @Override
    public int countDeviceException(LocalDateTime start, LocalDateTime end) {
        return devicesMapper.queryDeviceStatusCount(start, end);
    }

    @Override
    public List<DeviceEnergyVo> queryDeviceTypeCount() {
        return devicesMapper.queryDeviceTypeCount();
    }

    @Override
    public List<DeviceEnergyVo> queryDeviceCount() {
        return devicesMapper.queryDeviceCount();
    }




}
