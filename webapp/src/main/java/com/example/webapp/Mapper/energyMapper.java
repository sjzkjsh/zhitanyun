package com.example.webapp.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.webapp.Entity.*;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface energyMapper extends BaseMapper<energyReadings> {


    @Select("SELECT " +
            "MAX(CASE WHEN month_flag = 1 THEN totalPower END) AS lastMonthPower, " +
            "MAX(CASE WHEN month_flag = 1 THEN totalWater END) AS lastMonthWater, " +
            "MAX(CASE WHEN month_flag = 1 THEN totalAcPower END) AS lastMonthAcPower, " +
            "MAX(CASE WHEN month_flag = 1 THEN totalTce END) AS lastMonthTce, " +
            "MAX(CASE WHEN month_flag = 2 THEN totalPower END) AS prevMonthPower, " +
            "MAX(CASE WHEN month_flag = 2 THEN totalWater END) AS prevMonthWater, " +
            "MAX(CASE WHEN month_flag = 2 THEN totalAcPower END) AS prevMonthAcPower, " +
            "MAX(CASE WHEN month_flag = 2 THEN totalTce END) AS prevMonthTce " +
            "FROM ( " +
            "   SELECT " +
            "       CASE " +
            "           WHEN er.monitoring_time >= DATE_FORMAT(max_time.max_monitor, '%Y-%m-01') " +
            "                AND er.monitoring_time < DATE_ADD(DATE_FORMAT(max_time.max_monitor, '%Y-%m-01'), INTERVAL 1 MONTH) THEN 1 " +
            "           WHEN er.monitoring_time >= DATE_SUB(DATE_FORMAT(max_time.max_monitor, '%Y-%m-01'), INTERVAL 1 MONTH) " +
            "                AND er.monitoring_time < DATE_FORMAT(max_time.max_monitor, '%Y-%m-01') THEN 2 " +
            "       END AS month_flag, " +
            "       COALESCE(SUM(er.power_consumption), 0) AS totalPower, " +
            "       COALESCE(SUM(er.water_consumption), 0) AS totalWater, " +
            "       COALESCE(SUM(er.ac_power_consumption), 0) AS totalAcPower, " +
            "       COALESCE(SUM(er.power_consumption + er.ac_power_consumption) * 0.0001229 + SUM(er.water_consumption) * 0.0000857, 0) AS totalTce " +
            "   FROM energy_readings er " +
            "   JOIN buildings b ON er.building_id = b.building_id " +
            "   JOIN devices d ON er.device_id = d.device_id " +
            "   JOIN (SELECT MAX(er2.monitoring_time) AS max_monitor " +
            "           FROM energy_readings er2 " +
            "           JOIN buildings b2 ON er2.building_id = b2.building_id " +
            "           JOIN devices d2 ON er2.device_id = d2.device_id " +
            "           WHERE b2.building_code = #{buildingCode} AND d2.device_code = #{deviceCode}) max_time " +
            "   WHERE b.building_code = #{buildingCode} " +
            "     AND d.device_code = #{deviceCode} " +
            "     AND er.monitoring_time >= DATE_SUB(DATE_FORMAT(max_time.max_monitor, '%Y-%m-01'), INTERVAL 1 MONTH) " +
            "     AND er.monitoring_time < DATE_ADD(DATE_FORMAT(max_time.max_monitor, '%Y-%m-01'), INTERVAL 1 MONTH) " +
            "   GROUP BY month_flag " +
            ") AS t")
    MonthComparisonDTO getMonthComparison(@Param("buildingCode") String buildingCode,
                                          @Param("deviceCode") String deviceCode);

    /**
     * 查询今年的月度能耗趋势（从今年1月到上个月底）
     */
    @Select("SELECT " +
            "er.monitoring_time AS month, " +
            "COALESCE(SUM(er.power_consumption), 0) AS totalPower, " +
            "COALESCE(SUM(er.water_consumption), 0) AS totalWater, " +
            "COALESCE(SUM(er.ac_power_consumption), 0) AS totalAcPower, " +
            "COALESCE(SUM(er.power_consumption + er.ac_power_consumption) * 0.0001229 + SUM(er.water_consumption) * 0.0000857, 0) AS totalTce " +
            "FROM energy_readings er " +
            "JOIN buildings b ON er.building_id = b.building_id " +
            "JOIN devices d ON er.device_id = d.device_id " +
            "WHERE b.building_code = #{buildingCode} " +
            "  AND d.device_code = #{deviceCode} " +
            "GROUP BY month " +
            "ORDER BY month ASC")
    List<MonthlyTrendDTO> getCurrentYearMonthlyTrend(@Param("buildingCode") String buildingCode,
                                                     @Param("deviceCode") String deviceCode);
    // 获取指定设备在指定建筑下的所有月度数据
    @Select("SELECT " +
            "COALESCE(SUM(er.power_consumption), 0) AS totalPower, " +
            "COALESCE(SUM(er.water_consumption), 0) AS totalWater, " +
            "COALESCE(SUM(er.ac_power_consumption), 0) AS totalAcPower, " +
            "COALESCE(SUM(er.power_consumption + er.ac_power_consumption) * 0.0001229 + SUM(er.water_consumption) * 0.0000857, 0) AS totalTce " +
            "FROM energy_readings er " +
            "JOIN buildings b ON er.building_id = b.building_id " +
            "JOIN devices d ON er.device_id = d.device_id " +
            "WHERE b.building_code = #{buildingCode} " +
            "AND d.device_code = #{deviceCode} " +
            "AND er.monitoring_time >= CONCAT(#{yearMonth}, '-01 00:00:00') " +
            "AND er.monitoring_time < DATE_ADD(CONCAT(#{yearMonth}, '-01 00:00:00'), INTERVAL 1 MONTH)")
    MonthlySummaryDTO getMonthlySummary(@Param("buildingCode") String buildingCode,
                                        @Param("deviceCode") String deviceCode,
                                        @Param("yearMonth") String yearMonth);

    // 查询平均环境温度
    @Select("SELECT AVG(env_temp) AS final_avg " +
            "FROM ( " +
            "    SELECT er.env_temp " +
            "    FROM energy_readings er " +
            "    JOIN buildings b ON er.building_id = b.building_id " +
            "    JOIN devices d ON er.device_id = d.device_id " +
            "    WHERE b.building_code = #{buildingCode} " +
            "      AND d.device_code = #{deviceCode} " +
            "    ORDER BY er.monitoring_time DESC " +
            "    LIMIT 7 " +
            ") temp")
    Double getEnvTempLatest(@Param("buildingCode") String buildingCode,
                            @Param("deviceCode") String deviceCode);
    // 获取平均人员密度
    @Select("SELECT AVG(humidity) AS final_avg " +
            "FROM ( " +
            "    SELECT er.humidity " +
            "    FROM energy_readings er " +
            "    JOIN buildings b ON er.building_id = b.building_id " +
            "    JOIN devices d ON er.device_id = d.device_id " +
            "    WHERE b.building_code = #{buildingCode} " +
            "      AND d.device_code = #{deviceCode} " +
            "    ORDER BY er.monitoring_time DESC " +
            "    LIMIT 7 " +
            ") temp")
    Double gethumidityLatest(@Param("buildingCode") String buildingCode,
                             @Param("deviceCode") String deviceCode);
    // 获取平均人员密度
    @Select("SELECT AVG(occupancy_density) AS final_avg " +
            "FROM ( " +
            "    SELECT er.occupancy_density " +
            "    FROM energy_readings er " +
            "    JOIN buildings b ON er.building_id = b.building_id " +
            "    JOIN devices d ON er.device_id = d.device_id " +
            "    WHERE b.building_code = #{buildingCode} " +
            "      AND d.device_code = #{deviceCode} " +
            "    ORDER BY er.monitoring_time DESC " +
            "    LIMIT 7 " +
            ") temp")
    Double getPersonnelDensityLatest(String buildingCode, String deviceCode);


    @Select("<script>" +
            "SELECT " +
            "device_id, building_id, monitoring_time, power_consumption, water_consumption, " +
            "water_flow_rate, ac_power_consumption, ac_outlet_temp, ac_inlet_temp, " +
            "env_temp, humidity, occupancy_density, power_load " +
            "FROM energy_readings " +
            "WHERE device_id = #{deviceId} AND building_id = #{buildingId} " +
            "<if test='startTime != null and endTime != null'> " +
            "  AND monitoring_time BETWEEN #{startTime} AND #{endTime} " +
            "  ORDER BY monitoring_time ASC " +
            "</if> " +
            "<if test='startTime == null or endTime == null'> " +
            "  ORDER BY monitoring_time DESC LIMIT 1 " +
            "</if> " +
            "</script>")
    List<energyReadings> queryEnergyReadings(@Param("deviceId") int deviceId,
                                            @Param("buildingId") int buildingId,
                                            @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);
    // Mapper 方法
    @Select("SELECT power_consumption, ac_power,ac_power_consumption, ac_inlet_temp, ac_outlet_temp, " +
            "water_flow_rate, water_consumption " +
            "FROM energy_readings " +
            "WHERE building_id = #{buildingId} AND device_id = #{deviceId} " +
            "ORDER BY monitoring_time DESC LIMIT 1")
    LatestEnergyDTO getLatestEnergyFields(@Param("buildingId") int buildingId,
                                          @Param("deviceId") int deviceId);

    @Select("SELECT env_temp FROM energy_readings " +
            "WHERE building_id = #{buildingId} AND device_id = #{deviceId} " +
            "ORDER BY monitoring_time DESC LIMIT 1")
    Double getEnvTempLatestByDevice(@Param("buildingId") int buildingId,
                                    @Param("deviceId") int deviceId);

    @Select("SELECT humidity FROM energy_readings " +
            "WHERE building_id = #{buildingId} AND device_id = #{deviceId} " +
            "ORDER BY monitoring_time DESC LIMIT 1")
    Double getHumidityLatestByDevice(@Param("buildingId") int buildingId,
                                     @Param("deviceId") int deviceId);

    @Select("SELECT occupancy_density FROM energy_readings " +
            "WHERE building_id = #{buildingId} AND device_id = #{deviceId} " +
            "ORDER BY monitoring_time DESC LIMIT 1")
    Double getOccupancyDensityLatestByDevice(@Param("buildingId") int buildingId,
                                             @Param("deviceId") int deviceId);
}
