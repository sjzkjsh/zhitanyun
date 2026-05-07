package com.example.Entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("alert_record")
public class AlertRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 设备ID */
    private Integer deviceId;

    /** 设备编号（冗余字段，便于列表展示） */
    private String deviceCode;

    /** 建筑ID（冗余） */
    private Integer buildingId;

    /** 触发告警的能耗读数ID */
    private Integer readingId;

    /** 触发的阈值规则ID */
    private Long thresholdId;

    /** 异常指标名称 */
    private String metricName;

    /** 异常时刻的实际读数 */
    private BigDecimal abnormalValue;

    /** 阈值下限（告警时刻快照） */
    private BigDecimal minValue;

    /** 阈值上限（告警时刻快照） */
    private BigDecimal maxValue;

    /** 单位（快照） */
    private String unit;

    /** 异常类型：超上限 / 低于下限 */
    private AlertType alertType;

    /** 告警级别：1-提示，2-一般，3-严重 */
    private Integer alertLevel;

    /** 处理状态：0-未处理，1-已确认，2-已忽略，3-已解决 */
    private Integer status;

    /** 处理人 */
    private String handledBy;

    /** 处理时间 */
    private LocalDateTime handledAt;

    /** 处理备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // ------------------- 枚举定义 -------------------
    public enum AlertType {
        ABOVE_MAX,  // 超过上限
        BELOW_MIN   // 低于下限
    }

    // 可选：状态常量
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_CONFIRMED = 1;
    public static final int STATUS_IGNORED = 2;
    public static final int STATUS_RESOLVED = 3;
}