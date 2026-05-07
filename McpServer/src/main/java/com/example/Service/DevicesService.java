package com.example.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.Entity.BuildingDeviceVo;
import com.example.Entity.DeviceEnergyBuildingVO;
import com.example.Entity.DeviceEnergyVo;
import com.example.Entity.Devices;

import java.time.LocalDateTime;
import java.util.List;

public interface DevicesService extends IService<Devices> {
    //根据建筑id查询设备信息
    List<BuildingDeviceVo> queryDevicesbyBuilding(int buildingId);
    //根据建筑编号查询设备信息
    List<BuildingDeviceVo> queryDevicesbyBuilding(String buildingcode);
    //根据建筑名称查询设备信息
    List<BuildingDeviceVo> queryDevicesbyBuildingName(String buildingname);

    //查询所有设备的运行状态和设备信息
    List<BuildingDeviceVo> getDevices();
    //根据设备id查询设备运行状态和设备信息
    List<BuildingDeviceVo> getDevices(int devicesId);
    //根据设备编号查询设备运行状态和设备信息
    List<BuildingDeviceVo> getDevices(String deviceCode);
    //根据设备类型查询设备运行状态和设备信息
    List<BuildingDeviceVo> queryDeviceStatusbyType(String deviceType);

    //统计设备异常的数量
    int countDeviceException(LocalDateTime startTime, LocalDateTime endTime);


    List<DeviceEnergyVo> queryDeviceTypeCount();
    List<DeviceEnergyVo> queryDeviceCount();

}
