package com.example.webapp.Service.ServiceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.webapp.Entity.Devices;
import com.example.webapp.Entity.buildingDevice;
import com.example.webapp.Mapper.DeviceMapper;
import com.example.webapp.Service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Devices> implements DeviceService {

    @Autowired
    private DeviceMapper deviceMapper;

    @Override
    public buildingDevice selectBuildingAndDevice(String deviceCode, String buildingCode) {
        return deviceMapper.selectBuildingAndDevice(deviceCode,buildingCode);
    }
}
