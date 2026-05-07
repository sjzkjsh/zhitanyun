package com.example.McpServices;

import com.example.Entity.BuildingDeviceVo;
import com.example.Service.DevicesService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class McpDevicesService {

    @Autowired
    private DevicesService devicesService;

    @Tool(name = "query_devices_by_building_id",
            description = "根据建筑 ID 查询该建筑下的所有设备列表。" +
                    "参数 buildingId: 建筑 ID。" +
                    "返回字段：device_id（设备 ID）、device_code、device_type、" +
                    "device_status（运行状态：正常/维护保养/设备故障/其他）、building_name（建筑名称）、building_code（建筑编号）、building_type（建筑类型）。" +
                    "适用场景：已知建筑 ID，需要查看该建筑内所有设备分布情况。")
    public List<BuildingDeviceVo> queryDevicesbyBuildingId(int buildingId) {
        List<BuildingDeviceVo> list = devicesService.queryDevicesbyBuilding(buildingId);
        return list == null ? Collections.emptyList() : list;
    }

    @Tool(name = "query_devices_by_building_code",
            description = "根据建筑编号查询该建筑下的所有设备列表。" +
                    "参数 buildingCode: 建筑编号。" +
                    "返回字段：device_id、device_code、device_type、device_status、building_name、building_code、building_type。" +
                    "适用场景：通过建筑编号快速定位建筑并查看其设备。")
    public List<BuildingDeviceVo> queryDevicesbyBuildingCode(String buildingcode) {
        List<BuildingDeviceVo> list = devicesService.queryDevicesbyBuilding(buildingcode);
        return list == null ? Collections.emptyList() : list;
    }

    @Tool(name = "query_devices_by_building_name",
            description = "根据建筑名称查询该建筑下的所有设备列表。" +
                    "参数 buildingName: 建筑名称。" +
                    "返回字段：device_id、device_code、device_type、device_status、building_name、building_code、building_type。" +
                    "适用场景：通过建筑中文名称查找设备，适合自然语言交互。")
    public List<BuildingDeviceVo> queryDevicesbyBuildingName(String buildingname) {
        List<BuildingDeviceVo> list = devicesService.queryDevicesbyBuildingName(buildingname);
        return list == null ? Collections.emptyList() : list;
    }

    @Tool(name = "query_all_devices",
            description = "查询系统中所有设备的完整列表。" +
                    "无参数。" +
                    "返回字段：device_id、device_code、device_type、device_status、building_name、building_code、building_type、install_date（安装日期）、created_at（创建时间）。" +
                    "注意：数据量可能较大，适用于全局设备盘点或总览场景。")
    public List<BuildingDeviceVo> getDevices() {
        List<BuildingDeviceVo> list = devicesService.getDevices();
        return list == null ? Collections.emptyList() : list;
    }

    @Tool(name = "query_device_by_id",
            description = "根据设备 ID 查询设备的详细信息。" +
                    "参数 deviceId: 设备 ID。" +
                    "返回字段：device_id、device_code、device_type、device_status、install_date、created_at、building_name、building_code、building_type。" +
                    "适用场景：精确定位特定设备，查看其详细档案和运行状态。")
    public BuildingDeviceVo queryDevices(int devicesId) {
        List<BuildingDeviceVo> list = devicesService.getDevices(devicesId);
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    @Tool(name = "query_device_by_code",
            description = "根据设备编号查询设备的详细信息。" +
                    "参数 deviceCode: 设备编号。" +
                    "返回字段：device_id、device_code、device_type、device_status、install_date、created_at、building_name、building_code、building_type。" +
                    "适用场景：通过设备铭牌编号快速查询设备，适合现场运维人员使用。")
    public BuildingDeviceVo queryDeviceStatusbyCode(String deviceCode) {
        List<BuildingDeviceVo> list = devicesService.getDevices(deviceCode);
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    @Tool(name = "query_devices_by_type",
            description = "根据设备类型批量查询同类设备。" +
                    "参数 deviceType: 设备类型例如：综合电表。" +
                    "返回字段：device_id、device_code、device_type、device_status、building_name、building_code、building_type。" +
                    "适用场景：统计某类设备的分布情况，如'查看所有空调机组'或'所有循环泵的运行状态'。")
    public List<BuildingDeviceVo> queryDeviceStatusbyType(String deviceType) {
        List<BuildingDeviceVo> list = devicesService.queryDeviceStatusbyType(deviceType);
        return list == null ? Collections.emptyList() : list;
    }
}