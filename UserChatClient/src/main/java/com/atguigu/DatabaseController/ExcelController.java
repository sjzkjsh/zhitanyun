package com.atguigu.DatabaseController;

import com.atguigu.FeignInterface.PageFeign;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RequestMapping("/api/energy")
@RestController
public class ExcelController {

    @Autowired
    private PageFeign pageFeign;

    @GetMapping("/excel")
    public void exportEnergy(
            @RequestParam(required = false) Integer buildingId,
            @RequestParam(required = false) Integer deviceId,
            @RequestParam(required = false) @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(required = false) String buildingType,
            @RequestParam(required = false) String deviceStatus,
            @RequestParam(required = false) String deviceCode,
            HttpServletResponse response) throws IOException {

        // 1. 调 Feign 获取 Excel 字节数组
        byte[] excelBytes = pageFeign.exportEnergy(
                buildingId, deviceId, startTime, endTime,
                buildingType, deviceStatus, deviceCode);

        // 2. 设置响应头
        String fileName = "能耗数据_" + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));

        // 3. 写给浏览器
        response.getOutputStream().write(excelBytes);
        response.getOutputStream().flush();
    }
}