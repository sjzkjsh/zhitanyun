package com.example.Repository;

import com.alibaba.excel.EasyExcel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.io.File;
import java.util.List;

@Slf4j
@Component
public class ExcelExportUtil {
    
    public String exportToExcel(List<?> data, String fileName) {
        try {
            String tempDir = System.getProperty("java.io.tmpdir");
            String filePath = tempDir + File.separator + fileName + ".xlsx";
            EasyExcel.write(filePath, data.get(0).getClass())
                    .sheet("异常能耗设备")
                    .doWrite(data);
            log.info("Excel 导出成功: {}", filePath);
            return filePath;
        } catch (Exception e) {
            log.error("Excel 导出失败", e);
            throw new RuntimeException("导出 Excel 失败", e);
        }
    }
}