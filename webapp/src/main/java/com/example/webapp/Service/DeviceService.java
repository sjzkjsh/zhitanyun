package com.example.webapp.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.webapp.Entity.Devices;
import com.example.webapp.Entity.buildingDevice;

public interface DeviceService extends IService<Devices> {
    buildingDevice selectBuildingAndDevice(String deviceCode, String buildingCode);
}
