package com.example.McpServices;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.Entity.AlertRecord;
import com.example.Entity.AnalysisEntity.ThresholdRange;
import com.example.Entity.Devices;
import com.example.Entity.WorkOrder;
import com.example.Entity.WorkOrderLog;
import com.example.Enum.Priority;
import com.example.Enum.WorkOrderStatus;
import com.example.Mapper.AlertRecordMapper;
import com.example.Mapper.DevicesMapper;
import com.example.Mapper.WorkOrderLogMapper;
import com.example.Mapper.WorkOrderMapper;
import com.example.Service.AnalysisService.ThresholdRangeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * MCP 事务操作工具 — 让大模型可以执行写操作
 * 包含：工单管理、设备状态管理、阈值管理、告警管理
 */
@Service
public class McpTransactionService {

    private static final Logger log = LoggerFactory.getLogger(McpTransactionService.class);

    @Autowired
    private WorkOrderMapper workOrderMapper;
    @Autowired
    private WorkOrderLogMapper workOrderLogMapper;
    @Autowired
    private DevicesMapper devicesMapper;
    @Autowired
    private AlertRecordMapper alertRecordMapper;
    @Autowired
    private ThresholdRangeService thresholdRangeService;

    // ==================== 工单操作 ====================

    @Tool(name = "create_work_order", description = """
            【创建工单】
            当用户报告设备故障或需要维修时，调用此工具创建工单。

            参数：
            - deviceCode: 设备编号（必填）
            - buildingId: 建筑ID（必填）
            - type: 工单类型，如"设备故障"、"设备保养"、"其他"（必填）
            - description: 故障描述（必填）
            - priority: 优先级，"高"、"中"、"低"（可选，默认"中"）
            - operatorId: 操作人ID（可选）

            返回：工单编号和创建结果
            """)
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createWorkOrder(
            @ToolParam(description = "设备编号") String deviceCode,
            @ToolParam(description = "建筑ID") Integer buildingId,
            @ToolParam(description = "工单类型") String type,
            @ToolParam(description = "故障描述") String description,
            @ToolParam(required = false, description = "优先级：高/中/低") String priority,
            @ToolParam(required = false, description = "操作人ID") Long operatorId) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 查询设备
            LambdaQueryWrapper<Devices> deviceQuery = new LambdaQueryWrapper<>();
            deviceQuery.eq(Devices::getDeviceCode, deviceCode);
            Devices device = devicesMapper.selectOne(deviceQuery);

            if (device == null) {
                result.put("success", false);
                result.put("message", "设备编号 " + deviceCode + " 不存在");
                return result;
            }

            // 创建工单
            WorkOrder workOrder = new WorkOrder();
            workOrder.setOrderNo("WO" + System.currentTimeMillis());
            workOrder.setType(type);
            workOrder.setDescription(description);
            workOrder.setPriority(priority != null ? Priority.valueOf(priority) : Priority.中);
            workOrder.setStatus(WorkOrderStatus.待处理);
            workOrder.setBuildingId(Long.valueOf(buildingId));
            workOrder.setEquipmentId(Long.valueOf(device.getDeviceId()));
            workOrder.setLocation(device.getBuildingId() != null ? String.valueOf(device.getBuildingId()) : null);
            workOrder.setSubmitTime(LocalDateTime.now());
            workOrder.setCreatedAt(LocalDateTime.now());
            workOrder.setUpdatedAt(LocalDateTime.now());
            if (operatorId != null) {
                workOrder.setHandlerId(operatorId);
            }

            workOrderMapper.insert(workOrder);

            // 记录日志
            WorkOrderLog workOrderLog = new WorkOrderLog();
            workOrderLog.setOrderId(workOrder.getId());
            workOrderLog.setAction("创建工单");
            workOrderLog.setContent("AI 助手创建工单：" + description);
            workOrderLog.setCreatedAt(LocalDateTime.now());
            workOrderLogMapper.insert(workOrderLog);

            // 更新设备状态（根据工单类型映射到正确的设备状态）
            String deviceStatus = mapTypeToDeviceStatus(type);
            LambdaUpdateWrapper<Devices> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Devices::getDeviceCode, deviceCode)
                    .set(Devices::getDeviceStatus, deviceStatus);
            devicesMapper.update(null, updateWrapper);

            result.put("success", true);
            result.put("message", "工单创建成功");
            result.put("orderNo", workOrder.getOrderNo());
            result.put("orderId", workOrder.getId());
            result.put("status", "待处理");

            log.info("AI 创建工单成功：{}", workOrder.getOrderNo());

        } catch (Exception e) {
            log.error("创建工单失败", e);
            result.put("success", false);
            result.put("message", "创建工单失败：" + e.getMessage());
        }

        return result;
    }

    @Tool(name = "update_work_order_status", description = """
            【更新工单状态】
            处理工单时调用，更新工单状态。

            参数：
            - orderId: 工单ID（必填）
            - status: 新状态，"处理中"、"已完成"、"已关闭"（必填）
            - handlerId: 处理人ID（可选）
            - remark: 处理备注（可选）

            返回：更新结果
            """)
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateWorkOrderStatus(
            @ToolParam(description = "工单ID") Long orderId,
            @ToolParam(description = "新状态：处理中/已完成/已关闭") String status,
            @ToolParam(required = false, description = "处理人ID") Long handlerId,
            @ToolParam(required = false, description = "处理备注") String remark) {

        Map<String, Object> result = new HashMap<>();

        try {
            WorkOrder existing = workOrderMapper.selectById(orderId);
            if (existing == null) {
                result.put("success", false);
                result.put("message", "工单 " + orderId + " 不存在");
                return result;
            }

            // 校验状态值是否合法
            WorkOrderStatus newStatus;
            try {
                newStatus = WorkOrderStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                result.put("success", false);
                result.put("message", "无效的工单状态: " + status + "，可选值：待处理、处理中、已完成、已关闭");
                return result;
            }

            // 乐观锁更新
            existing.setStatus(newStatus);
            existing.setUpdatedAt(LocalDateTime.now());
            if (handlerId != null) {
                existing.setHandlerId(handlerId);
            }
            if ("已完成".equals(status)) {
                existing.setCompletedTime(LocalDateTime.now());
            }

            int rows = workOrderMapper.updateById(existing);
            if (rows == 0) {
                result.put("success", false);
                result.put("message", "工单已被其他人处理，请刷新后重试");
                return result;
            }

            // 记录日志
            WorkOrderLog workOrderLog = new WorkOrderLog();
            workOrderLog.setOrderId(orderId);
            workOrderLog.setAction("更新状态");
            workOrderLog.setContent("状态更新为：" + status + (remark != null ? "，备注：" + remark : ""));
            workOrderLog.setCreatedAt(LocalDateTime.now());
            workOrderLogMapper.insert(workOrderLog);

            result.put("success", true);
            result.put("message", "工单状态已更新为：" + status);
            result.put("orderNo", existing.getOrderNo());
            result.put("newStatus", status);

            log.info("AI 更新工单状态：{} → {}", orderId, status);

        } catch (Exception e) {
            log.error("更新工单状态失败", e);
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
        }

        return result;
    }

    // ==================== 设备操作 ====================

    @Tool(name = "update_device_status", description = """
            【更新设备状态】
            当需要修改设备运行状态时调用。

            参数：
            - deviceCode: 设备编号（必填）
            - status: 新状态，"正常"、"故障"、"维护保养"（必填）

            返回：更新结果
            """)
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateDeviceStatus(
            @ToolParam(description = "设备编号") String deviceCode,
            @ToolParam(description = "新状态：正常/故障/维护保养") String status) {

        Map<String, Object> result = new HashMap<>();

        try {
            LambdaUpdateWrapper<Devices> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(Devices::getDeviceCode, deviceCode)
                    .set(Devices::getDeviceStatus, status);

            int rows = devicesMapper.update(null, wrapper);
            if (rows == 0) {
                result.put("success", false);
                result.put("message", "设备 " + deviceCode + " 不存在");
                return result;
            }

            result.put("success", true);
            result.put("message", "设备 " + deviceCode + " 状态已更新为：" + status);

            log.info("AI 更新设备状态：{} → {}", deviceCode, status);

        } catch (Exception e) {
            log.error("更新设备状态失败", e);
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
        }

        return result;
    }

    // ==================== 阈值操作 ====================

    @Tool(name = "update_threshold_range", description = """
            【更新告警阈值】
            当需要调整设备或建筑的能耗告警阈值时调用。

            参数：
            - metricName: 指标名称，如"power_consumption"、"ac_power"、"env_temp"（必填）
            - minValue: 最小值（可选，null 表示不限下限）
            - maxValue: 最大值（可选，null 表示不限上限）
            - deviceId: 设备ID（可选，null 表示建筑级或全局级）
            - buildingId: 建筑ID（可选，null 表示全局级）
            - unit: 单位（可选）

            返回：更新结果
            """)
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateThresholdRange(
            @ToolParam(description = "指标名称") String metricName,
            @ToolParam(required = false, description = "最小值") BigDecimal minValue,
            @ToolParam(required = false, description = "最大值") BigDecimal maxValue,
            @ToolParam(required = false, description = "设备ID") Integer deviceId,
            @ToolParam(required = false, description = "建筑ID") Integer buildingId,
            @ToolParam(required = false, description = "单位") String unit) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 查找已有的阈值配置
            LambdaQueryWrapper<ThresholdRange> query = new LambdaQueryWrapper<>();
            query.eq(ThresholdRange::getMetricName, metricName);

            if (deviceId != null) {
                query.eq(ThresholdRange::getDeviceId, deviceId);
            } else {
                query.isNull(ThresholdRange::getDeviceId);
            }

            if (buildingId != null) {
                query.eq(ThresholdRange::getBuildingId, buildingId);
            } else {
                query.isNull(ThresholdRange::getBuildingId);
            }

            ThresholdRange existing = thresholdRangeService.getOne(query);

            if (existing != null) {
                // 更新已有配置
                existing.setMinValue(minValue);
                existing.setMaxValue(maxValue);
                if (unit != null) {
                    existing.setUnit(unit);
                }
                thresholdRangeService.updateById(existing);
                result.put("message", "阈值配置已更新");
            } else {
                // 创建新配置
                ThresholdRange newRange = new ThresholdRange();
                newRange.setMetricName(metricName);
                newRange.setMinValue(minValue);
                newRange.setMaxValue(maxValue);
                newRange.setDeviceId(deviceId);
                newRange.setBuildingId(buildingId);
                newRange.setUnit(unit);
                newRange.setCreatedAt(LocalDateTime.now());
                thresholdRangeService.save(newRange);
                result.put("message", "阈值配置已创建");
            }

            result.put("success", true);
            result.put("metricName", metricName);
            result.put("minValue", minValue);
            result.put("maxValue", maxValue);
            result.put("level", deviceId != null ? "设备级" : (buildingId != null ? "建筑级" : "全局级"));

            log.info("AI 更新阈值：{} [{}, {}]", metricName, minValue, maxValue);

        } catch (Exception e) {
            log.error("更新阈值失败", e);
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
        }

        return result;
    }

    // ==================== 告警操作 ====================

    /**
     * 将工单类型映射为设备状态
     */
    private String mapTypeToDeviceStatus(String type) {
        if (type == null) return "正常";
        if (type.contains("故障")) return "故障";
        if (type.contains("保养") || type.contains("维护")) return "维护保养";
        return "正常";
    }

    @Tool(name = "close_alert", description = """
            【关闭告警】
            当确认异常已处理或为误报时，关闭告警记录。

            参数：
            - alertId: 告警ID（必填）
            - remark: 关闭原因（可选）

            返回：关闭结果
            """)
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> closeAlert(
            @ToolParam(description = "告警ID") Long alertId,
            @ToolParam(required = false, description = "关闭原因") String remark) {

        Map<String, Object> result = new HashMap<>();

        try {
            AlertRecord alert = alertRecordMapper.selectById(alertId);
            if (alert == null) {
                result.put("success", false);
                result.put("message", "告警 " + alertId + " 不存在");
                return result;
            }

            LambdaUpdateWrapper<AlertRecord> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(AlertRecord::getId, alertId)
                    .set(AlertRecord::getStatus, AlertRecord.STATUS_RESOLVED)
                    .set(AlertRecord::getHandledAt, LocalDateTime.now())
                    .set(AlertRecord::getRemark, remark != null ? remark : "AI 助手手动关闭");

            alertRecordMapper.update(null, wrapper);

            result.put("success", true);
            result.put("message", "告警已关闭");
            result.put("alertId", alertId);

            log.info("AI 关闭告警：{}", alertId);

        } catch (Exception e) {
            log.error("关闭告警失败", e);
            result.put("success", false);
            result.put("message", "关闭失败：" + e.getMessage());
        }

        return result;
    }

    @Tool(name = "batch_close_alerts", description = """
            【批量关闭告警】
            批量关闭某个设备的所有待处理告警。

            参数：
            - deviceCode: 设备编号（必填）
            - remark: 关闭原因（可选）

            返回：关闭数量
            """)
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchCloseAlerts(
            @ToolParam(description = "设备编号") String deviceCode,
            @ToolParam(required = false, description = "关闭原因") String remark) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 查询设备
            LambdaQueryWrapper<Devices> deviceQuery = new LambdaQueryWrapper<>();
            deviceQuery.eq(Devices::getDeviceCode, deviceCode);
            Devices device = devicesMapper.selectOne(deviceQuery);

            if (device == null) {
                result.put("success", false);
                result.put("message", "设备 " + deviceCode + " 不存在");
                return result;
            }

            // 批量关闭告警
            LambdaUpdateWrapper<AlertRecord> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(AlertRecord::getDeviceId, device.getDeviceId())
                    .eq(AlertRecord::getStatus, AlertRecord.STATUS_PENDING)
                    .set(AlertRecord::getStatus, AlertRecord.STATUS_RESOLVED)
                    .set(AlertRecord::getHandledAt, LocalDateTime.now())
                    .set(AlertRecord::getRemark, remark != null ? remark : "AI 助手批量关闭");

            int count = alertRecordMapper.update(null, wrapper);

            result.put("success", true);
            result.put("message", "已关闭设备 " + deviceCode + " 的 " + count + " 条告警");
            result.put("closedCount", count);

            log.info("AI 批量关闭告警：设备 {}，数量 {}", deviceCode, count);

        } catch (Exception e) {
            log.error("批量关闭告警失败", e);
            result.put("success", false);
            result.put("message", "批量关闭失败：" + e.getMessage());
        }

        return result;
    }
}
