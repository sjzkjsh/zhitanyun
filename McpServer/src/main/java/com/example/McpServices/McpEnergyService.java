package com.example.McpServices;

import com.example.Entity.DeviceEnergyBuildingVO;
import com.example.Entity.energyReadings;
import com.example.Mapper.EnergyReadingsMapper;
import com.example.Service.EnergyReadingsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class McpEnergyService {


        private static final Logger log = LoggerFactory.getLogger(McpEnergyService.class);
        private static final DateTimeFormatter TIME_FORMATTER =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        @Autowired
        private EnergyReadingsMapper energyReadingsMapper;

        @Autowired
        private EnergyReadingsService energyReadingsService;


        @Tool(name = "query_by_deviceStatus",
        description = "根据设备状态查询信息")
        public String queryByDeviceStatus(String  status){

            return energyReadingsMapper.queryByDeviceStatus(status).toString();
        }

        /**
         * 查询设备最新能耗 - 简化逻辑，统一格式
         */
        @Tool(name = "get_device_latest_energy", description = """
        【查询设备最新能耗数据】
        
        根据设备id获取设备最新有数据的时间点，返回该时刻的完整能耗数据。
        无需传入时间参数。
        
        参数：
        - deviceId: 设备ID（整数，必填）
        
        返回：包含时间戳、用电量、用水量、空调用电、温度、湿度等
        """)
        public String getDeviceLatestEnergy(
                @ToolParam(description = "设备ID，整数，必填", required = true) int deviceId) {

            log.info("【工具调用】get_device_latest_energy, deviceId={}", deviceId);

            try {
                // 1. 查最新时间
                LocalDateTime latestTime = energyReadingsMapper.selectLastTime(deviceId);
                if (latestTime == null) {
                    return jsonResult("deviceId", deviceId, null, "设备无数据记录");
                }

                // 2. 查该时刻数据（用Service方法）
                List<energyReadings> data = energyReadingsService.queryConsumptionByDevicesId(
                        deviceId,
                        latestTime.withMinute(0).withSecond(0),
                        latestTime.withMinute(59).withSecond(59));

                if (data.isEmpty()) {
                    return jsonResult("deviceId", deviceId, null, "该时刻无数据");
                }

                // 3. 格式化返回（单条）
                return jsonResult("deviceId", deviceId,
                        formatReading(data.get(0)), "ok");

            } catch (Exception e) {
                log.error("【工具异常】", e);
                return jsonResult("deviceId", deviceId, null, e.getMessage());
            }
        }

        /**
         * 查询建筑最新能耗 - 修复硬编码时间，自动查最新
         */
        @Tool(name = "get_building_latest_energy", description = """
        【查询建筑最新能耗数据】
        
        根据建筑id查找数据库中最新的一条记录，无需指定时间。
        
        参数：
        - buildingId: 建筑ID（整数，必填）
        """)
        public String getBuildingLatestEnergy(
                @ToolParam(description = "建筑ID，整数，必填", required = true) int buildingId) {

            log.info("【工具调用】get_building_latest_energy, buildingId={}", buildingId);

            try {
                // 不传时间，让Mapper返回最新一条（根据你之前的Mapper逻辑：不传时间时order by desc limit 1）
                List<energyReadings> data = energyReadingsService.queryEnergyconsumptionbybuilding(
                        buildingId, null, null, null);

                if (data == null || data.isEmpty()) {
                    return jsonResult("buildingId", buildingId, null, "建筑无数据");
                }

                return jsonResult("buildingId", buildingId,
                        formatReading(data.get(0)), "ok");

            } catch (Exception e) {
                log.error("【工具异常】", e);
                return jsonResult("buildingId", buildingId, null, e.getMessage());
            }
        }

        /**
         * 查询设备完整档案 - 支持时间参数
         */
        @Tool(name = "get_device_full_profile", description = """
        【查询设备完整能耗档案】
        
        返回设备关联建筑、设备信息的完整记录。
        
        参数：
        - deviceId: 设备ID（整数，必填）
        - time: 精确查询时间点，格式yyyy-MM-dd HH:mm:ss（可选，不传则返回最新）
        """)
        public String getDeviceFullProfile(
                @ToolParam(description = "设备ID", required = true) int deviceId,
                @ToolParam(description = "精确时间点，格式yyyy-MM-dd HH:mm:ss") String time) {

            log.info("【工具调用】get_device_full_profile, deviceId={}, time={}", deviceId, time);

            try {
                LocalDateTime singleTime = parseTime(time);

                // 如果没传时间，查最新时刻
                if (singleTime == null) {
                    singleTime = energyReadingsMapper.selectLastTime(deviceId);
                    if (singleTime == null) {
                        return jsonResult("deviceId", deviceId, null, "设备无数据");
                    }
                }

                // 查完整档案（用Service方法）
                List<DeviceEnergyBuildingVO> data = energyReadingsService.getqueryEnergyconsumption(
                        deviceId, singleTime, null, null, 1);

                if (data == null || data.isEmpty()) {
                    return jsonResult("deviceId", deviceId, null, "该时刻无数据");
                }

                return jsonResult("deviceId", deviceId,
                        formatDeviceVO(data.get(0)), "ok");

            } catch (Exception e) {
                log.error("【工具异常】", e);
                return jsonResult("deviceId", deviceId, null, e.getMessage());
            }
        }

        /**
         * 查询设备时间范围能耗 - 增加限制，防止数据过多
         */
        @Tool(name = "get_device_energy_range", description = """
        【查询设备指定时间范围的能耗】
        
        参数：
        - deviceId: 设备ID（整数，必填）
        - startTime: 开始时间，格式yyyy-MM-dd HH:mm:ss（必填）
        - endTime: 结束时间，格式yyyy-MM-dd HH:mm:ss（必填）
        - limit: 最大返回条数（可选，默认10，最大50）
        """)
        public String getDeviceEnergyRange(
                @ToolParam(description = "设备ID", required = true) int deviceId,
                @ToolParam(description = "开始时间，格式yyyy-MM-dd HH:mm:ss", required = true) String startTime,
                @ToolParam(description = "结束时间，格式yyyy-MM-dd HH:mm:ss", required = true) String endTime,
                @ToolParam(description = "返回条数限制，默认10") Integer limit) {

            log.info("【工具调用】get_device_energy_range, deviceId={}, start={}, end={}",
                    deviceId, startTime, endTime);

            try {
                LocalDateTime start = parseTime(startTime);
                LocalDateTime end = parseTime(endTime);

                if (start == null || end == null) {
                    return "{\"error\": \"时间格式错误，请使用yyyy-MM-dd HH:mm:ss\"}";
                }

                // 限制查询范围，防止内存溢出
                if (start.plusDays(7).isBefore(end)) {
                    return "{\"error\": \"查询范围不能超过7天，请缩小时间范围\"}";
                }

                int maxLimit = (limit != null && limit > 0 && limit <= 50) ? limit : 10;

                List<energyReadings> data = energyReadingsService.queryConsumptionByDevicesId(
                        deviceId, start, end);

                if (data == null || data.isEmpty()) {
                    return jsonResult("deviceId", deviceId, null, "该时段无数据");
                }

                // 截取前N条
                List<energyReadings> limitedData = data.size() > maxLimit ?
                        data.subList(0, maxLimit) : data;

                StringBuilder dataStr = new StringBuilder("[");
                for (int i = 0; i < limitedData.size(); i++) {
                    if (i > 0) dataStr.append(", ");
                    dataStr.append(formatReading(limitedData.get(i)));
                }
                dataStr.append("]");

                String note = data.size() > maxLimit ?
                        String.format(", \"note\": \"共%d条，显示前%d条\"", data.size(), maxLimit) : "";

                return String.format(
                        "{\"deviceId\": %d, \"startTime\": \"%s\", \"endTime\": \"%s\", \"count\": %d, \"data\": %s%s}",
                        deviceId, startTime, endTime, data.size(), dataStr.toString(), note);

            } catch (Exception e) {
                log.error("【工具异常】", e);
                return jsonResult("deviceId", deviceId, null, e.getMessage());
            }
        }

        // ==================== 新增建筑查询工具（4个）====================

        /**
         * 按建筑ID查能耗 - 支持时间范围
         */
        @Tool(name = "get_building_energy_by_id", description = """
        【根据建筑ID查询能耗数据】
        
        精确查询指定建筑的能耗和环境数据。
        
        时间参数（3种模式）：
        1. 不传时间：返回最新一条数据
        2. 传startTime+endTime：返回时间范围内的数据（最多50条）
        
        参数：
        - buildingId: 建筑ID（整数，必填）
        - startTime: 开始时间，格式yyyy-MM-dd HH:mm:ss（可选）
        - endTime: 结束时间，格式yyyy-MM-dd HH:mm:ss（可选）
        """)
        public String getBuildingEnergyById(
                @ToolParam(description = "建筑ID", required = true) int buildingId,
                @ToolParam(description = "开始时间") String startTime,
                @ToolParam(description = "结束时间") String endTime) {

            log.info("【工具调用】get_building_energy_by_id, buildingId={}", buildingId);

            try {
                LocalDateTime start = parseTime(startTime);
                LocalDateTime end = parseTime(endTime);

                List<energyReadings> data = energyReadingsService.queryEnergyconsumptionbybuilding(
                        buildingId, null, start, end);

                return formatBuildingListResult("buildingId", buildingId, data, start, end);

            } catch (Exception e) {
                log.error("【工具异常】", e);
                return jsonResult("buildingId", buildingId, null, e.getMessage());
            }
        }

        /**
         * 按建筑名称查能耗（模糊匹配）
         */
        @Tool(name = "get_building_energy_by_name", description = """
        【根据建筑名称查询能耗数据】
        
        支持模糊匹配
        
        参数：
        - buildingName: 建筑名称（字符串，必填）
        - startTime: 开始时间（可选）
        - endTime: 结束时间（可选）
        """)
        public String getBuildingEnergyByName(
                @ToolParam(description = "建筑名称，支持模糊查询", required = true) String buildingName,
                @ToolParam(description = "开始时间") String startTime,
                @ToolParam(description = "结束时间") String endTime) {

            log.info("【工具调用】get_building_energy_by_name, name={}", buildingName);

            try {
                LocalDateTime start = parseTime(startTime);
                LocalDateTime end = parseTime(endTime);

                List<energyReadings> data = energyReadingsService.queryEnergyconsumptionbyBuildingname(
                        buildingName, null, start, end);

                return formatBuildingListResult("buildingName", buildingName, data, start, end);

            } catch (Exception e) {
                log.error("【工具异常】", e);
                return jsonResult("buildingName", buildingName, null, e.getMessage());
            }
        }

        /**
         * 按建筑类型查能耗
         */
        @Tool(name = "get_building_energy_by_type", description = """
        【根据建筑类型查询能耗数据】
        
        查询某类建筑的所有能耗记录
        
        ⚠️ 注意：可能返回大量数据，建议指定时间范围。
        
        参数：
        - buildingType: 建筑类型（字符串，必填）
        - startTime: 开始时间（可选，但建议填写）
        - endTime: 结束时间（可选）
        """)
        public String getBuildingEnergyByType(
                @ToolParam(description = "建筑类型", required = true) String buildingType,
                @ToolParam(description = "开始时间") String startTime,
                @ToolParam(description = "结束时间") String endTime) {

            log.info("【工具调用】get_building_energy_by_type, type={}", buildingType);

            try {
                LocalDateTime start = parseTime(startTime);
                LocalDateTime end = parseTime(endTime);

                List<energyReadings> data = energyReadingsService.queryEnergyconsumptionbybuilding(
                        buildingType, null, start, end);

                return formatBuildingListResult("buildingType", buildingType, data, start, end);

            } catch (Exception e) {
                log.error("【工具异常】", e);
                return jsonResult("buildingType", buildingType, null, e.getMessage());
            }
        }

        /**
         * 按建筑编号查能耗
         */
        @Tool(name = "get_building_energy_by_code", description = """
        【根据建筑编号查询能耗数据】
        
        按建筑唯一编码查询
        
        参数：
        - buildingCode: 建筑编号（字符串，必填）
        - startTime: 开始时间（可选）
        - endTime: 结束时间（可选）
        """)
        public String getBuildingEnergyByCode(
                @ToolParam(description = "建筑编号，如：BUILD-001", required = true) String buildingCode,
                @ToolParam(description = "开始时间") String startTime,
                @ToolParam(description = "结束时间") String endTime) {

            log.info("【工具调用】get_building_energy_by_code, code={}", buildingCode);

            try {
                LocalDateTime start = parseTime(startTime);
                LocalDateTime end = parseTime(endTime);

                List<energyReadings> data = energyReadingsService.queryEnergyconsumptionbyBuildingcode(
                        buildingCode, null, start, end);

                return formatBuildingListResult("buildingCode", buildingCode, data, start, end);

            } catch (Exception e) {
                log.error("【工具异常】", e);
                return jsonResult("buildingCode", buildingCode, null, e.getMessage());
            }
        }

        // ==================== 私有工具方法（抽取复用）====================

        /**
         * 格式化单条能耗记录为JSON
         */
        private String formatReading(energyReadings r) {
            if (r == null) return "{}";
            return String.format(
                    "{\"monitoringTime\": \"%s\", \"powerConsumption\": %.2f, \"waterConsumption\": %.2f, " +
                            "\"acPowerConsumption\": %.2f, \"acOutletTemp\": %.2f, \"acInletTemp\": %.2f, " +
                            "\"envTemp\": %.2f, \"humidity\": %.2f, \"occupancyDensity\": %.2f, \"waterFlowRate\": %.2f}",
                    r.getMonitoringTime() != null ? r.getMonitoringTime().format(TIME_FORMATTER) : "null",
                    safeDouble(r.getPowerConsumption()),
                    safeDouble(r.getWaterConsumption()),
                    safeDouble(r.getAcPowerConsumption()),
                    safeDouble(r.getAcOutletTemp()),
                    safeDouble(r.getAcInletTemp()),
                    safeDouble(r.getEnvTemp()),
                    safeDouble(r.getHumidity()),
                    safeDouble(r.getOccupancyDensity()),
                    safeDouble(r.getWaterFlowRate())
            );
        }

        /**
         * 格式化设备完整档案VO为JSON
         */
        private String formatDeviceVO(DeviceEnergyBuildingVO vo) {
            if (vo == null) return "{}";
            return String.format(
                    "{\"deviceId\": %d, \"deviceCode\": \"%s\", \"deviceType\": \"%s\", \"deviceStatus\": \"%s\", " +
                            "\"buildingId\": %d, \"buildingName\": \"%s\", \"buildingCode\": \"%s\", " +
                            "\"monitoringTime\": \"%s\", \"powerConsumption\": %.2f, \"acPowerConsumption\": %.2f, " +
                            "\"acOutletTemp\": %.2f, \"acInletTemp\": %.2f, \"envTemp\": %.2f, \"humidity\": %.2f, " +
                            "\"occupancyDensity\": %.2f}",
                    vo.getDeviceId(),
                    safeStr(vo.getDeviceCode()),
                    safeStr(vo.getDeviceType()),
                    safeStr(vo.getDeviceStatus()),
                    vo.getBuildingId(),
                    safeStr(vo.getBuildingName()),
                    safeStr(vo.getBuildingCode()),
                    vo.getMonitoringTime() != null ? vo.getMonitoringTime().toString() : "",
                    safeDouble(vo.getPowerConsumption()),
                    safeDouble(vo.getAcPowerConsumption()),
                    safeDouble(vo.getAcOutletTemp()),
                    safeDouble(vo.getAcInletTemp()),
                    safeDouble(vo.getEnvTemp()),
                    safeDouble(vo.getHumidity()),
                    safeDouble(vo.getOccupancyDensity())
            );
        }

        /**
         * 格式化建筑列表查询结果（多条）
         */
        private String formatBuildingListResult(String key, Object value, List<energyReadings> data,
                                                LocalDateTime start, LocalDateTime end) {
            if (data == null || data.isEmpty()) {
                return jsonResult(key, value, null, "无数据");
            }

            // 最多显示5条摘要
            int showCount = Math.min(5, data.size());
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < showCount; i++) {
                if (i > 0) sb.append(", ");
                sb.append(formatReading(data.get(i)));
            }
            sb.append("]");

            String timeRange = (start != null && end != null) ?
                    String.format(", \"startTime\": \"%s\", \"endTime\": \"%s\"",
                            start.format(TIME_FORMATTER), end.format(TIME_FORMATTER)) : "";

            String note = data.size() > 5 ?
                    String.format(", \"note\": \"共%d条，显示前5条\"", data.size()) : "";

            return String.format(
                    "{\"%s\": \"%s\", \"count\": %d%s, \"data\": %s%s}",
                    key, value, data.size(), timeRange, sb.toString(), note
            );
        }

        /**
         * 统一返回结果格式
         */
        private String jsonResult(String key, Object value, Object data, String status) {
            if (data == null) {
                return String.format("{\"%s\": \"%s\", \"status\": \"error\", \"error\": \"%s\"}",
                        key, value, status);
            }
            return String.format("{\"%s\": \"%s\", \"status\": \"%s\", \"data\": %s}",
                    key, value, status, data);
        }

        private LocalDateTime parseTime(String time) {
            if (time == null || time.isEmpty()) return null;
            try {
                return LocalDateTime.parse(time, TIME_FORMATTER);
            } catch (Exception e) {
                log.warn("时间解析失败: {}", time);
                return null;
            }
        }

        private double safeDouble(Double d) {
            return d != null ? d : 0.0;
        }

        private String safeStr(String s) {
            return s != null ? s : "";
        }

}