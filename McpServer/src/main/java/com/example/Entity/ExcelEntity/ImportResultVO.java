package com.example.Entity.ExcelEntity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ImportResultVO {
    private Integer totalCount = 0;
    private Integer successCount = 0;
    private Integer failCount = 0;
    private Integer skipCount = 0;
    private Boolean success;
    private String message;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp = LocalDateTime.now();

    private List<ErrorInfo> errors = new ArrayList<>();

    @Data
    public static class ErrorInfo {
        // 你要的 5 个字段
        private Integer rowNum;            // 行号
        private String buildingCode;       // 建筑编号
        private String buildingName;       // 建筑名称
        private String deviceCode;         // 设备编号
        private String deviceType;         // 设备类型

        // 导入状态
        private String status;             // SUCCESS / SKIP / FAIL
        private String statusDesc;         // 成功 / 跳过 / 失败
        private String errorMsg;           // 错误原因（成功时为空）

        // 构造器
        public ErrorInfo() {}

        public static ErrorInfo of(Integer rowNum, String buildingCode, String buildingName,
                                   String deviceCode, String deviceType,
                                   String status, String statusDesc, String errorMsg) {
            ErrorInfo info = new ErrorInfo();
            info.rowNum = rowNum;
            info.buildingCode = buildingCode;
            info.buildingName = buildingName;
            info.deviceCode = deviceCode;
            info.deviceType = deviceType;
            info.status = status;
            info.statusDesc = statusDesc;
            info.errorMsg = errorMsg;
            return info;
        }
    }

    // 快捷方法
    public void addSuccess(Integer rowNum, String buildingCode, String buildingName,
                           String deviceCode, String deviceType) {
        errors.add(ErrorInfo.of(rowNum, buildingCode, buildingName, deviceCode, deviceType,
                "SUCCESS", "成功", null));
    }

    public void addSkip(Integer rowNum, String buildingCode, String buildingName,
                        String deviceCode, String deviceType, String reason) {
        errors.add(ErrorInfo.of(rowNum, buildingCode, buildingName, deviceCode, deviceType,
                "SKIP", "跳过", reason));
        skipCount++;
    }

    public void addFail(Integer rowNum, String buildingCode, String buildingName,
                        String deviceCode, String deviceType, String errorMsg) {
        errors.add(ErrorInfo.of(rowNum, buildingCode, buildingName, deviceCode, deviceType,
                "FAIL", "失败", errorMsg));
        failCount++;
    }

    public Boolean getSuccess() {
        return failCount == 0;
    }
    public void addParseError(Integer rowNum, String errorMsg) {
        // 解析错误时没有 buildingName/deviceType，传 null 或空字符串
        errors.add(ErrorInfo.of(rowNum, null, null, null, null,
                "FAIL", "解析失败", errorMsg));
        failCount++;
    }
    public String getSummary() {
        return String.format("总计%d条，成功%d条，失败%d条，跳过%d条",
                totalCount, successCount, failCount, skipCount);
    }
}