package com.example.DataBaseController;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.example.Entity.DeviceEnergyBuildingVO;
import com.example.Service.DataBaseService.PageService;
import com.example.Service.EnergyReadingsService;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.time.LocalDateTime;


import java.util.List;
@RestController
@RequestMapping("/api/energy")
public class ExcelController {
        @Autowired
        private PageService pageService;

        /**
         * 导出能耗数据
         */

        @GetMapping("/excel")
        public byte[] exportEnergy(
                @RequestParam(required = false) Integer buildingId,
                @RequestParam(required = false) Integer deviceId,
                @RequestParam(required = false) @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
                @RequestParam(required = false) @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
                @RequestParam(required = false) String buildingType,
                @RequestParam(required = false) String deviceStatus,
                @RequestParam(required = false) String deviceCode) throws IOException {

            // 1. 查数据
            List<DeviceEnergyBuildingVO> dataList = pageService.queryVOForExport(
                    buildingId, deviceId, startTime, endTime,
                    buildingType, deviceStatus, deviceCode);

            // 2. 生成Excel到内存流
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ExcelWriter writer = ExcelUtil.getWriter(true);
            setEnergyHeaderAlias(writer);
            writer.write(dataList, true);
            writer.flush(baos);
            writer.close();

            // 3. 返回字节数组（不是void了）
            return baos.toByteArray();
        }

    private void setEnergyHeaderAlias(ExcelWriter writer) {
        // 按你希望的列顺序依次添加别名
        writer.addHeaderAlias("readingId", "检测记录");
        writer.addHeaderAlias("buildingId", "建筑ID");
        writer.addHeaderAlias("buildingCode", "建筑编号");
        writer.addHeaderAlias("buildingName", "建筑名称");
        writer.addHeaderAlias("buildingType", "建筑类型");
        writer.addHeaderAlias("location", "地址");
        writer.addHeaderAlias("deviceId", "设备ID");
        writer.addHeaderAlias("deviceType", "设备类型");
        writer.addHeaderAlias("installTime", "安装时间");
        writer.addHeaderAlias("deviceStatus", "设备状态");
        writer.addHeaderAlias("deviceCode", "设备编号");
        writer.addHeaderAlias("powerConsumption", "电耗(kWh)");
        writer.addHeaderAlias("waterConsumption", "水耗(m³)");
        writer.addHeaderAlias("waterFlowRate", "水流量");
        writer.addHeaderAlias("acPowerConsumption", "空调能耗(kWh)");
        writer.addHeaderAlias("acOutletTemp", "空调出风温度(°C)");
        writer.addHeaderAlias("acInletTemp", "空调回风温度(°C)");
        writer.addHeaderAlias("envTemp", "环境温度(°C)");
        writer.addHeaderAlias("humidity", "湿度(%)");
        writer.addHeaderAlias("occupancyDensity", "人员密度(人/m²)");
        writer.addHeaderAlias("powerLoad", "电力负荷");
        writer.addHeaderAlias("monitoringTime", "监控时间");
        writer.addHeaderAlias("dataSource", "数据来源");
        writer.addHeaderAlias("createdAt", "创建时间");

        // 启用仅导出别名列（确保未设别名的字段不出现）
        writer.setOnlyAlias(true);
    }
}
