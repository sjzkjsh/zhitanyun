package com.example.McpServiceConfig;

import com.example.Service.BuildingsService;
import com.example.Service.DevicesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Order(1)
@Component
public class CacheWarmupConfig implements CommandLineRunner {

    private final BuildingsService buildingsService;
    private final DevicesService devicesService;

    public CacheWarmupConfig(BuildingsService buildingsService, DevicesService devicesService) {
        this.buildingsService = buildingsService;
        this.devicesService = devicesService;
    }

    @Override
    public void run(String... args) {
        log.info("========== 缓存预热开始 ==========");
        long start = System.currentTimeMillis();

        try {
            // 预热建筑信息
            buildingsService.queryBuildings();                          // key: 'all'
            buildingsService.countBuildings();                         // key: 'count'
            buildingsService.list().forEach(b -> {
                buildingsService.queryBuildings(b.getBuildingId());    // key: buildingId
                buildingsService.queryBuildings(b.getBuildingName());  // key: buildingName
                buildingsService.queryBuildingsbyCode(b.getBuildingCode()); // key: buildingCode
            });
            log.info("建筑信息缓存预热完成");

            // 预热设备信息
            devicesService.getDevices();                               // key: 'all'
            devicesService.list().forEach(d -> {
                devicesService.getDevices(d.getDeviceId());            // key: 'deviceId:X'
                devicesService.getDevices(d.getDeviceCode());          // key: 'deviceCode:X'
                if (d.getBuildingId() != null) {
                    devicesService.queryDevicesbyBuilding(d.getBuildingId()); // key: 'buildingId:X'
                }
                if (d.getDeviceType() != null) {
                    devicesService.queryDeviceStatusbyType(d.getDeviceType()); // key: 'deviceType:X'
                }
            });
            log.info("设备信息缓存预热完成");

            long cost = System.currentTimeMillis() - start;
            log.info("========== 缓存预热完成，耗时 {}ms ==========", cost);
        } catch (Exception e) {
            log.error("缓存预热异常，不影响服务启动", e);
        }
    }
}
