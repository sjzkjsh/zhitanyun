package com.example.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Entity.CoVo;
import com.example.Entity.*;
import com.example.Entity.AnalysisEntity.DailyStats;
import com.example.Entity.PieEntity.BuildingEnergy;
import com.example.Entity.PieEntity.DeviceDetailVO;
import com.example.Entity.PieEntity.DeviceEnergy;
import com.example.Entity.DeviceEnergyBuildingVO;
import com.example.Entity.AnalysisEntity.TimeBoundary;
import com.example.Entity.EnergyEntity.AcPowerTrendVO;
import com.example.Entity.EnergyEntity.EnergyTrendVO;
import com.example.Entity.EnergyEntity.WaterFlowRateVo;
import com.example.Entity.EnergyEntity.WaterTrendVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface EnergyReadingsMapper extends BaseMapper<energyReadings> {

    // ==================== 按建筑查询 ====================

    @Select({
            "<script>",
            "select power_consumption, water_consumption, ac_power_consumption, ",
            "ac_outlet_temp, ac_inlet_temp, env_temp, humidity,monitoring_time, occupancy_density, water_flow_rate ",
            "from energy_readings ",
            "where building_id = #{buildingId} ",
            "<if test='singleTime != null'>",
            "and monitoring_time = #{singleTime} ",
            "</if>",
            "<if test='startTime != null and endTime != null'>",
            "and monitoring_time between #{startTime} and #{endTime} ",
            "</if>",
            "<if test='singleTime == null and startTime == null and endTime == null'>",
            "order by monitoring_time desc limit 1",  // 改为最新，不是随机！
            "</if>",
            "</script>"
    })
    List<energyReadings> queryEnergyconsumptionbybuildingId(
            @Param("buildingId") int buildingId,
            @Param("singleTime") LocalDateTime singleTime,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Select({
            "<script>",
            "select power_consumption, water_consumption, ac_power_consumption, ",
            "ac_outlet_temp, ac_inlet_temp, env_temp, humidity,monitoring_time, occupancy_density, water_flow_rate ",
            "from energy_readings ",
            "where building_type = #{buildingType} ",
            "<if test='singleTime != null'>",
            "and monitoring_time = #{singleTime} ",
            "</if>",
            "<if test='startTime != null and endTime != null'>",
            "and monitoring_time between #{startTime} and #{endTime} ",
            "</if>",
            "<if test='singleTime == null and startTime == null and endTime == null'>",
            "order by monitoring_time desc limit 1",  // 最新
            "</if>",
            "</script>"
    })
    List<energyReadings> queryEnergyconsumptionbybuildingType(
            @Param("buildingType") String buildingType,
            @Param("singleTime") LocalDateTime singleTime,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Select({
            "<script>",
            "select power_consumption, water_consumption, ac_power_consumption, ",
            "ac_outlet_temp, ac_inlet_temp, env_temp, humidity,monitoring_time, occupancy_density, water_flow_rate ",
            "from energy_readings ",
            "where building_code = #{buildingCode} ",
            "<if test='singleTime != null'>",
            "and monitoring_time = #{singleTime} ",
            "</if>",
            "<if test='startTime != null and endTime != null'>",
            "and monitoring_time between #{startTime} and #{endTime} ",
            "</if>",
            "<if test='singleTime == null and startTime == null and endTime == null'>",
            "order by monitoring_time desc limit 1",
            "</if>",
            "<if test='singleTime != null or startTime != null or endTime != null'>",
            "order by monitoring_time desc",
            "</if>",
            "</script>"
    })
    List<energyReadings> queryEnergyconsumptionbyBuildingcode(
            @Param("buildingCode") String buildingCode,
            @Param("singleTime") LocalDateTime singleTime,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Select({
            "<script>",
            "select power_consumption, water_consumption, ac_power_consumption, ",
            "ac_outlet_temp, ac_inlet_temp, env_temp, humidity,monitoring_time, occupancy_density, water_flow_rate ",
            "from energy_readings ",
            "where building_name = #{buildingName} ",
            "<if test='singleTime != null'>",
            "and monitoring_time = #{singleTime} ",
            "</if>",
            "<if test='startTime != null and endTime != null'>",
            "and monitoring_time between #{startTime} and #{endTime} ",
            "</if>",
            "<if test='singleTime == null and startTime == null and endTime == null'>",
            "order by monitoring_time desc limit 1",
            "</if>",
            "<if test='singleTime != null or startTime != null or endTime != null'>",
            "order by monitoring_time desc",
            "</if>",
            "</script>"
    })
    List<energyReadings> queryEnergyconsumptionbyBuildingname(
            @Param("buildingName") String buildingName,
            @Param("singleTime") LocalDateTime singleTime,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    // ==================== 设备查询 ====================

    @Select({
            "<script>",
            "SELECT ",
            "b.building_id, b.building_name, b.building_code, b.location, ",
            "d.device_id, d.device_type, d.device_status, d.install_date, d.device_code, ",
            "er.power_consumption, er.water_consumption, er.ac_power_consumption, ",
            "er.ac_outlet_temp, er.ac_inlet_temp, er.env_temp,er.monitoring_time, er.humidity, er.occupancy_density, er.water_flow_rate, ",
            "er.monitoring_time ",
            "FROM energy_readings er ",
            "LEFT JOIN devices d ON er.device_id = d.device_id ",
            "LEFT JOIN buildings b ON d.building_id = b.building_id ",
            "WHERE d.device_id = #{deviceId} ",
            "<if test='singleTime != null'>",
            "AND er.monitoring_time = #{singleTime} ",
            "</if>",
            "<if test='startTime != null and endTime != null'>",
            "AND er.monitoring_time BETWEEN #{startTime} AND #{endTime} ",
            "</if>",
            "<if test='singleTime == null and startTime == null and endTime == null'>",
            "ORDER BY er.monitoring_time DESC LIMIT #{randomCount, jdbcType=INTEGER}",  // 倒序，最新在前
            "</if>",
            "<if test='singleTime != null or startTime != null or endTime != null'>",
            "ORDER BY er.monitoring_time DESC",
            "</if>",
            "</script>"
    })
    List<DeviceEnergyBuildingVO> queryEnergyconsumption(
            @Param("deviceId") int deviceId,
            @Param("singleTime") LocalDateTime singleTime,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("randomCount") Integer randomCount);


    @Select("SELECT "
            +"r.reading_id,"
            + "    b.building_id AS buildingId, "
            + "    b.building_code AS buildingCode, "
            + "    b.building_name AS buildingName, "
            + "    b.location AS location ,"
            + "    d.device_id AS deviceId, "
            + "    d.device_type AS deviceType, "
            + "    DATE_FORMAT(d.install_date, '%Y-%m-%d') AS installTime, "
            + "    d.device_status AS deviceStatus, "
            + "    d.device_code AS deviceCode, "
            + "    r.power_consumption AS powerConsumption, "
            + "    r.water_consumption AS waterConsumption, "
            + "    r.water_flow_rate, "
            + "    r.ac_power_consumption AS acPowerConsumption, "
            + "    r.ac_outlet_temp AS acOutletTemp, "
            + "    r.ac_inlet_temp AS acInletTemp, "
            + "    r.env_temp AS envTemp, "
            + "    r.humidity AS humidity, "
            + "    r.occupancy_density AS occupancyDensity, "
            + "    r.monitoring_time AS monitoringTime "
            + "FROM energy_readings r "
            +"LEFT JOIN devices d ON r.device_id = d.device_id "
            +"LEFT JOIN buildings b ON r.building_id = b.building_id "
            + "WHERE d.device_status=#{deviceStatus} LIMIT 50")
    List<DeviceEnergyBuildingVO> queryByDeviceStatus(
            @Param("deviceStatus") String deviceStatus);

    // ==================== 简单查询====================

    @Select("select power_consumption, water_consumption, ac_power,ac_power_consumption, " +
            "ac_outlet_temp, ac_inlet_temp, env_temp, humidity,monitoring_time, occupancy_density, water_flow_rate " +
            "from energy_readings " +
            "where device_id = #{deviceId} and monitoring_time between #{start} AND #{end} " +
            "order by monitoring_time desc")  // 倒序
    List<energyReadings> queryEnergyconsumptionbyDeviceId(
            @Param("deviceId") int deviceId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // ==================== 环境温度查询 ====================

    @Select("SELECT env_temp FROM energy_readings " +
            "WHERE monitoring_time <= #{time} " +
            "ORDER BY monitoring_time DESC LIMIT 1")
    Double getEnvTempByTime(@Param("time") LocalDateTime time);

    @Select("SELECT occupancy_density FROM energy_readings " +
            "WHERE monitoring_time <= #{time} " +
            "ORDER BY monitoring_time DESC LIMIT 1")
    Double getOccupancyByTime(@Param("time") LocalDateTime time);

    // ==================== 范围查询 ====================

    @Select("""
        SELECT 
            er.monitoring_time as monitoringTime,
            er.device_id as deviceId,
            d.device_code as deviceCode,
            er.building_id as buildingId,
            b.building_code as buildingCode,
            er.power_consumption as powerConsumption,
            er.water_consumption as waterConsumption,
            er.monitoring_time,
            er.water_flow_rate,
            er.ac_power_consumption as acPowerConsumption,
            er.ac_outlet_temp as acOutletTemp,
            er.ac_inlet_temp as acInletTemp,
            er.env_temp as envTemp,
            er.humidity,
            er.occupancy_density as occupancyDensity
        FROM energy_readings er
        LEFT JOIN devices d ON er.device_id = d.device_id
        LEFT JOIN buildings b ON er.building_id = b.building_id
        WHERE er.device_id = #{deviceId}
          AND er.monitoring_time BETWEEN #{start} AND #{end}
        ORDER BY er.monitoring_time DESC
        """)
    List<DeviceEnergyBuildingVO> selectByTimeRange(
            @Param("buildingId") Integer buildingId,
            @Param("deviceId") Integer deviceId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // ==================== 时间边界查询 ====================

    @Select("""
        SELECT 
            MIN(monitoring_time) as earliest,
            MAX(monitoring_time) as latest,
            COUNT(*) as total
        FROM energy_readings 
        WHERE device_id = #{deviceId}
        """)
    TimeBoundary selectTimeBoundary(@Param("deviceId") Integer deviceId);

    //返回最大（最新）时间
    @Select("SELECT MAX(monitoring_time) FROM energy_readings WHERE device_id = #{deviceId}")
    LocalDateTime selectLastTime(@Param("deviceId") Integer deviceId);

    // ==================== 日统计 ====================

    @Select("""
    SELECT 
        DATE(monitoring_time) as date,
        COUNT(*) as recordCount,
        AVG(power_consumption) as avgPower,
        MAX(power_consumption) as maxPower,
        STDDEV(power_consumption) as powerStd,
        SUM(CASE WHEN power_consumption > #{threshold} THEN 1 ELSE 0 END) as thresholdExceedCount
    FROM energy_readings
    WHERE device_id = #{deviceId}
      AND monitoring_time BETWEEN #{start} AND #{end}
    GROUP BY DATE(monitoring_time)
    ORDER BY date
    """)
    List<DailyStats> selectDailyStats(
            @Param("deviceId") Integer deviceId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("threshold") Double threshold);  // 添加 threshold 参数

    // ==================== 按时间查询====================

    @Select("SELECT power_consumption, water_consumption, ac_power,ac_power_consumption, " +
            "ac_outlet_temp, ac_inlet_temp, env_temp, humidity," +
            " occupancy_density, water_flow_rate,monitoring_time " +
            "FROM energy_readings " +
            "WHERE device_id = #{deviceId} AND monitoring_time BETWEEN #{start} AND #{end} " +
            "ORDER BY monitoring_time DESC")
    List<energyReadings> selectByTime(
            @Param("deviceId") Integer deviceId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Select("SELECT power_consumption, water_consumption, ac_power,ac_power_consumption, " +
            "ac_outlet_temp, ac_inlet_temp, env_temp," +
            " humidity,monitoring_time, occupancy_density, water_flow_rate " +
            "FROM energy_readings " +
            "WHERE device_id = #{deviceId} AND monitoring_time = #{monitorTime}")
    List<energyReadings> selectByOnlyTime(
            @Param("deviceId") Integer deviceId,
            @Param("monitorTime") LocalDateTime monitorTime);

    // ==================== 分页查询 ====================

    @Select("<script>"
            + "SELECT "
            +"r.reading_id,"
            + "    b.building_id AS buildingId, "
            + "    b.building_code AS buildingCode, "
            + "    b.building_name AS buildingName, "
            + "    b.location AS location ,"
            + "    d.device_id AS deviceId, "
            + "    d.device_type AS deviceType, "
            + "    DATE_FORMAT(d.install_date, '%Y-%m-%d') AS installTime, "
            + "    d.device_status AS deviceStatus, "
            + "    d.device_code AS deviceCode, "
            + "    r.water_flow_rate, " +
            "       r.power_load," +
            "       r.ac_power,"
            + "    r.power_consumption AS powerConsumption, "
            + "    r.water_consumption AS waterConsumption, "
            + "    r.ac_power_consumption AS acPowerConsumption, "
            + "    r.ac_outlet_temp AS acOutletTemp, "
            + "    r.ac_inlet_temp AS acInletTemp, "
            + "    r.env_temp AS envTemp, "
            + "    r.humidity AS humidity, "
            + "    r.occupancy_density AS occupancyDensity, "
            + "    r.monitoring_time AS monitoringTime " +

             "FROM energy_readings r "
            + "INNER JOIN devices d ON r.device_id = d.device_id "
            + "INNER JOIN buildings b ON r.building_id = b.building_id "
            + "<where> "
            + "    <if test='buildingId != null'> AND r.building_id = #{buildingId} </if> "
            + "    <if test='deviceId != null'> AND r.device_id = #{deviceId} </if> "
            + "    <if test='startTime != null'> AND r.monitoring_time &gt;= #{startTime} </if> "
            + "    <if test='endTime != null'> AND r.monitoring_time &lt;= #{endTime} </if> "
            + "    <if test='buildingType != null and buildingType != \"\"'> AND b.building_type = #{buildingType} </if> "
            + "    <if test='deviceStatus != null and deviceStatus != \"\"'> AND d.device_status = #{deviceStatus} </if> "
            + "    <if test='deviceCode != null and deviceCode != \"\"'> AND d.device_code LIKE CONCAT('%', #{deviceCode}, '%') </if> "
            + "</where> "
            + "ORDER BY r.monitoring_time DESC"
            + "</script>")
    Page<DeviceEnergyBuildingVO> selectVOWithConditions(
            IPage<?> page,
            @Param("buildingId") Integer buildingId,
            @Param("deviceId") Integer deviceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("buildingType") String buildingType,
            @Param("deviceStatus") String deviceStatus,
            @Param("deviceCode") String deviceCode);

    // ==================== 导出查询 ====================

    @Select("<script>"
            + "SELECT "
            +"r.reading_id,"
            + "    b.building_id AS buildingId, "
            + "    b.building_code AS buildingCode, "
            + "    b.building_name AS buildingName, "
            + "    b.location AS location ,"
            + "    d.device_id AS deviceId, "
            + "    d.device_type AS deviceType, "
            + "    DATE_FORMAT(d.install_date, '%Y-%m-%d') AS installTime, "
            + "    d.device_status AS deviceStatus, "
            + "    d.device_code AS deviceCode, "
            + "    r.power_consumption AS powerConsumption, "
            + "    r.water_consumption AS waterConsumption, "
            + "    r.water_flow_rate, "
            + "    r.power_load ," +
            "       r.ac_power, "
            + "    r.ac_power_consumption AS acPowerConsumption, "
            + "    r.ac_outlet_temp AS acOutletTemp, "
            + "    r.ac_inlet_temp AS acInletTemp, "
            + "    r.env_temp AS envTemp, "
            + "    r.humidity AS humidity, "
            + "    r.occupancy_density AS occupancyDensity, "
            + "    r.monitoring_time AS monitoringTime "
            + "FROM energy_readings r "
            + "INNER JOIN devices d ON r.device_id = d.device_id "
            + "INNER JOIN buildings b ON r.building_id = b.building_id "
            + "<where> "
            + "    <if test='buildingId != null'> AND r.building_id = #{buildingId} </if> "
            + "    <if test='deviceId != null'> AND r.device_id = #{deviceId} </if> "
            + "    <if test='startTime != null'> AND r.monitoring_time &gt;= #{startTime} </if> "
            + "    <if test='endTime != null'> AND r.monitoring_time &lt;= #{endTime} </if> "
            + "    <if test='buildingType != null and buildingType != \"\"'> AND b.building_type = #{buildingType} </if> "
            + "    <if test='deviceStatus != null and deviceStatus != \"\"'> AND d.device_status = #{deviceStatus} </if> "
            + "    <if test='deviceCode != null and deviceCode != \"\"'> AND d.device_code LIKE CONCAT('%', #{deviceCode}, '%') </if> "
            + "</where> "
            + "ORDER BY r.monitoring_time DESC"
            + "</script>")
    List<DeviceEnergyBuildingVO> queryVOForExport(
            @Param("buildingId") Integer buildingId,
            @Param("deviceId") Integer deviceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("buildingType") String buildingType,
            @Param("deviceStatus") String deviceStatus,
            @Param("deviceCode") String deviceCode);

    // ==================== 汇总查询 ====================

    @Select("SELECT SUM(power_consumption) FROM energy_readings " +
            "WHERE monitoring_time >= CONCAT(#{year}, '-01-01') " +
            "AND monitoring_time <  CONCAT(#{year}+1, '-01-01')")
    BigDecimal sumPowerByYear(@Param("year") Integer year);

    @Select("SELECT SUM(power_consumption) FROM energy_readings")
    Double sumPower();

    @Select("SELECT COALESCE(SUM(water_consumption), 0) " +
            "FROM energy_readings " +
            "WHERE monitoring_time >= #{startTime} " +
            "AND monitoring_time < #{endTime}")
    BigDecimal sumWaterConsumption(
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);

    @Select("SELECT SUM(water_consumption) FROM energy_readings")
    Double sumWater();



    // ===================== 折线图查询 =====================

    @Select("<script>" +
            "SELECT monitoring_time, power_consumption " +
            "FROM energy_readings " +
            "<where>" +
            "   <if test='buildingId != null'> AND building_id = #{buildingId} </if>" +
            "   <if test='deviceId != null'> AND device_id = #{deviceId} </if>" +
            "</where>" +
            "ORDER BY monitoring_time" +
            "</script>")
    List<EnergyTrendVO> getEnergyTrend(@Param("buildingId") Integer buildingId,
                                       @Param("deviceId") Integer deviceId);
    @Select({
            "<script>",
            "SELECT b.building_name, b.building_type, d.device_code, d.device_type, d.device_status, ",
            "       er.power_consumption, er.monitoring_time ",
            "FROM energy_readings er ",
            "LEFT JOIN buildings b ON b.building_id = er.building_id ",
            "LEFT JOIN devices d ON d.device_id = er.device_id ",
            "WHERE 1 = 1 ",
            "<if test='deviceType != null and deviceType != \"\"'>",
            "  AND d.device_type = #{deviceType} ",
            "</if>",
            "<if test='deviceStatus != null and deviceStatus != \"\"'>",
            "  AND d.device_status = #{deviceStatus} ",
            "</if>",
            "<if test='buildingId != null'>",
            "  AND b.building_id = #{buildingId} ",
            "</if>",
            "<if test='deviceId != null'>",
            "  AND d.device_id = #{deviceId} ",
            "</if>",
            "LIMIT 50",
            "</script>"
    })
    List<EnergyReadingVO> queryEnergyReadings(
            @Param("deviceType") String deviceType,
            @Param("deviceStatus") String deviceStatus,
            @Param("buildingId") Integer buildingId,
            @Param("deviceId") Integer deviceId
    );
    @Select("<script>" +
            "SELECT building_name " +
            ",SUM( power_consumption ) AS  powerConsumption " +
            "FROM energy_readings er " +
            " left join buildings  b on er.building_id = b.building_id " +
            "GROUP BY er.building_id "+
            "</script>")
    List<BuildingEnergyTrendVO> getEnergyTrendGroupByBuilding();
    @Select("<script>" +
            "SELECT monitoring_time, water_consumption " +
            "FROM energy_readings " +
            "<where>" +
            "   <if test='buildingId != null'> AND building_id = #{buildingId} </if>" +
            "   <if test='deviceId != null'> AND device_id = #{deviceId} </if>" +
            "</where>" +
            "ORDER BY monitoring_time" +
            "</script>")
    List<WaterTrendVO> getWaterTrend(@Param("buildingId") Integer buildingId,
                                     @Param("deviceId") Integer deviceId);
    @Select({
            "<script>",
            "SELECT b.building_name, b.building_type, d.device_code, d.device_type, d.device_status, ",
            "       er.water_consumption, er.monitoring_time ",
            "FROM energy_readings er ",
            "LEFT JOIN buildings b ON b.building_id = er.building_id ",
            "LEFT JOIN devices d ON d.device_id = er.device_id ",
            "WHERE 1 = 1 ",
            "<if test='deviceType != null and deviceType != \"\"'>",
            "  AND d.device_type = #{deviceType} ",
            "</if>",
            "<if test='deviceStatus != null and deviceStatus != \"\"'>",
            "  AND d.device_status = #{deviceStatus} ",
            "</if>",
            "<if test='buildingId != null'>",
            "  AND b.building_id = #{buildingId} ",
            "</if>",
            "<if test='deviceId != null'>",
            "  AND d.device_id = #{deviceId} ",
            "</if>",
            "LIMIT 50",
            "</script>"
    })
    List<BuildingWaterPower> queryWaterPower(
            @Param("deviceType") String deviceType,
            @Param("deviceStatus") String deviceStatus,
            @Param("buildingId") Integer buildingId,
            @Param("deviceId") Integer deviceId
    );
    @Select("<script>" +
            "SELECT building_name" +
            ",SUM( water_consumption ) AS  waterConsumption " +
            "FROM energy_readings er " +
            "LEFT JOIN buildings  b on er.building_id = b.building_id " +
            "GROUP BY er.building_id "+
            "</script>")
    List<BuildingWaterTrendVO> getWaterTrendGroupByBuilding();

   // ============== 折线图查询 ==============
    @Select("<script>" +
            "SELECT monitoring_time, ac_power_consumption " +
            "FROM energy_readings " +
            "<where>" +
            "   <if test='buildingId != null'> AND building_id = #{buildingId} </if>" +
            "   <if test='deviceId != null'> AND device_id = #{deviceId} </if>" +
            "</where>" +
            "ORDER BY monitoring_time" +
            "</script>")
    List<AcPowerTrendVO> getAcPowerTrend(@Param("buildingId") Integer buildingId,
                                         @Param("deviceId") Integer deviceId);
    @Select({
            "<script>",
            "SELECT b.building_name, b.building_type, d.device_code, d.device_type, d.device_status, ",
            "       er.ac_power_consumption, er.monitoring_time " +
            "FROM energy_readings er ",
            "LEFT JOIN buildings b ON b.building_id = er.building_id ",
            "LEFT JOIN devices d ON d.device_id = er.device_id ",
            "WHERE 1 = 1 ",
            "<if test='deviceType != null and deviceType != \"\"'>",
            "  AND d.device_type = #{deviceType} ",
            "</if>",
            "<if test='deviceStatus != null and deviceStatus != \"\"'>",
            "  AND d.device_status = #{deviceStatus} ",
            "</if>",
            "<if test='buildingId != null'>",
            "  AND b.building_id = #{buildingId} ",
            "</if>",
            "<if test='deviceId != null'>",
            "  AND d.device_id = #{deviceId} ",
            "</if>",
            "LIMIT 50",
            "</script>"
    })
    List<BuildingEnergyPower> queryEnergyPower(
            @Param("deviceType") String deviceType,
            @Param("deviceStatus") String deviceStatus,
            @Param("buildingId") Integer buildingId,
            @Param("deviceId") Integer deviceId
    );
    @Select("<script>" +
            "SELECT building_name" +
            ",SUM( ac_power_consumption ) AS acPowerConsumption " +
            "FROM energy_readings er " +
            "left join buildings  b on er.building_id = b.building_id " +
            "GROUP BY er.building_id "+
            "</script>")
    List<BuildingAcPower> getAcPowerTrendGroupByBuilding();

    @Select("<script>" +
            "SELECT monitoring_time, water_flow_rate " +
            "FROM energy_readings " +
            "<where>" +
            "   <if test='buildingId != null'> AND building_id = #{buildingId} </if>" +
            "   <if test='deviceId != null'> AND device_id = #{deviceId} </if>" +
            "</where>" +
            "ORDER BY monitoring_time" +
            "</script>")
    List<WaterFlowRateVo> getWaterFlow(@Param("buildingId") Integer buildingId,
                                       @Param("deviceId") Integer deviceId);
    @Select({
            "<script>",
            "SELECT b.building_name, b.building_type, d.device_code, d.device_type, d.device_status, ",
            "       er.water_flow_rate, er.monitoring_time ",
            "FROM energy_readings er ",
            "LEFT JOIN buildings b ON b.building_id = er.building_id ",
            "LEFT JOIN devices d ON d.device_id = er.device_id ",
            "WHERE 1 = 1 ",
            "<if test='deviceType != null and deviceType != \"\"'>",
            "  AND d.device_type = #{deviceType} ",
            "</if>",
            "<if test='deviceStatus != null and deviceStatus != \"\"'>",
            "  AND d.device_status = #{deviceStatus} ",
            "</if>",
            "<if test='buildingId != null'>",
            "  AND b.building_id = #{buildingId} ",
            "</if>",
            "<if test='deviceId != null'>",
            "  AND d.device_id = #{deviceId} ",
            "</if>",
            "LIMIT 50",
            "</script>"
    })
    List<BuildingWaterFlowRate> queryWaterFlowRate(
            @Param("deviceType") String deviceType,
            @Param("deviceStatus") String deviceStatus,
            @Param("buildingId") Integer buildingId,
            @Param("deviceId") Integer deviceId
    );
    @Select("<script>" +
            "SELECT building_name" +
            ",SUM( water_flow_rate ) AS waterFlowRate " +
            "FROM energy_readings er " +
            "left join buildings  b on er.building_id = b.building_id " +
            "GROUP BY er.building_id "+
            "</script>")
    List<BuildingWaterFlowRateVO> getWaterFlowRateGroupByBuilding();

    // ===================== 饼图查询 =====================
    // ========== 第一层：建筑占比 ==========
    @Select("SELECT b.building_code, b.building_name, " +
            "SUM(IFNULL(r.water_consumption,0) + " +
            "IFNULL(r.power_consumption,0) + " +
            "IFNULL(r.ac_power_consumption,0)) as value " +
            "FROM buildings b " +
            "LEFT JOIN energy_readings r ON b.building_id = r.building_id " +
            "GROUP BY b.building_code")
    List<BuildingEnergy> queryBuildingEnergy();

    // ========== 第二层：设备占比 ==========
    @Select("SELECT d.device_type, d.device_code, " +
            "SUM(IFNULL(e.water_consumption,0) + " +
            "IFNULL(e.power_consumption,0) + " +
            "IFNULL(e.ac_power_consumption,0)) as value " +
            "FROM devices d " +
            "LEFT JOIN energy_readings e ON d.device_id = e.device_id " +
            "WHERE d.building_id = #{buildingId} " +
            "GROUP BY d.device_code")
    List<DeviceEnergy> queryDeviceEnergy(@Param("buildingId") Integer buildingId);

    // ========== 第三层：设备三能耗 ==========
    @Select("SELECT d.device_id, d.device_code, d.device_type, " +
            "SUM(IFNULL(e.water_consumption, 0)) as waterConsumption, " +
            "SUM(IFNULL(e.power_consumption, 0)) as powerConsumption, " +
            "SUM(IFNULL(e.ac_power_consumption, 0)) as acPowerConsumption " +
            "FROM devices d " +
            "LEFT JOIN energy_readings e ON d.device_id = e.device_id " +
            "WHERE d.device_id = #{deviceId} AND e.building_id = #{buildingId} " +
            "GROUP BY d.device_code")
    DeviceDetailVO selectDeviceDetail(@Param("buildingId") Integer buildingId,
                                      @Param("deviceId") Integer deviceId);

    @Select("SELECT building_name FROM buildings WHERE building_id = #{id}")
    String selectBuildingName(@Param("id") Integer id);
    // 查询平均环境温度
    @Select("<script>" +
            "SELECT AVG(env_temp) AS final_avg " +
            "FROM ( " +
            "    SELECT env_temp " +
            "    FROM energy_readings " +
            "    <where>" +
            "        <if test='buildingId != null'> AND building_id = #{buildingId} </if> " +
            "        <if test='deviceId != null'> AND device_id = #{deviceId} </if> " +
            "    </where> " +
            "    ORDER BY monitoring_time DESC " +
            "    LIMIT 7 " +
            ") temp " +
            "</script>")
    Double getEnvTempLatest(@Param("buildingId") Integer buildingId,
                            @Param("deviceId") Integer deviceId);
    //获取平均湿度
    @Select("<script>" +
            "SELECT AVG(humidity) AS final_avg " +
            "FROM ( " +
            "    SELECT humidity " +
            "    FROM energy_readings " +
            "    <where>" +
            "        <if test='buildingId != null'> AND building_id = #{buildingId} </if> " +
            "        <if test='deviceId != null'> AND device_id = #{deviceId} </if> " +
            "    </where> " +
            "    ORDER BY monitoring_time DESC " +
            "    LIMIT 7 " +
            ") temp " +
            "</script>")
    Double getPersonLatest(@Param("buildingId") Integer buildingId,
                            @Param("deviceId") Integer deviceId);
    // 获取平均人员密度
    @Select("<script>" +
            "SELECT AVG(occupancy_density) AS final_avg " +
            "FROM ( " +
            "    SELECT occupancy_density " +
            "    FROM energy_readings " +
            "    <where>" +
            "        <if test='buildingId != null'> AND building_id = #{buildingId} </if> " +
            "        <if test='deviceId != null'> AND device_id = #{deviceId} </if> " +
            "    </where> " +
            "    ORDER BY monitoring_time DESC " +
            "    LIMIT 7 " +
            ") temp " +
            "</script>")
    Double gethumidityLatest(@Param("buildingId") Integer buildingId,
                             @Param("deviceId") Integer deviceId);

    //平均负载
    @Select("<script>" +
            "SELECT AVG(power_load) AS final_avg " +
            "FROM ( " +
            "    SELECT power_load " +
            "    FROM energy_readings " +
            "    <where>" +
            "        <if test='buildingId != null'> AND building_id = #{buildingId} </if> " +
            "        <if test='deviceId != null'> AND device_id = #{deviceId} </if> " +
            "    </where> " +
            "    ORDER BY monitoring_time DESC " +
            "    LIMIT 7 " +
            ") temp " +
            "</script>")
    Double getPowerLoad(@Param("buildingId") Integer buildingId,
                            @Param("deviceId") Integer deviceId);




    /**
     * 查询所有触发阈值异常的 建筑ID + 设备ID（去重）
     */
    /**
     * 分页查询所有超标阈值的 建筑ID+设备ID
     */
    /**
     * 分页查询所有超标设备 + 具体超标指标（默认10条/页）
     */

    @Select("<script>" +
            "SELECT a.id, a.device_id AS deviceId, a.device_code AS deviceCode, " +
            "b.building_name AS buildingName, d.device_type AS deviceType, " +
            "a.building_id AS buildingId, a.metric_name AS metricName, " +
            "a.abnormal_value AS abnormalValue, a.min_value AS minVal, " +
            "a.max_value AS maxVal, a.unit, a.alert_type AS alertType, " +
            "a.alert_level AS alertLevel, a.status, a.handled_by AS handledBy, " +
            "a.handled_at AS handledAt, a.remark, a.created_at AS createdAt " +
            "FROM alert_record a " +
            "LEFT JOIN devices d ON a.device_id = d.device_id " +
            "left join buildings b on b.building_id=b.building_id " +
            "WHERE 1=1 " +
            "<if test='buildingId != null'> AND a.building_id = #{buildingId} </if>" +
            "<if test='deviceId != null'> AND a.device_id = #{deviceId} </if>" +
            "<if test='status != null'> AND a.status = #{status} </if>" +
            "<if test='metricName!=null'> and a.metric_name=#{metricName}</if>" +
            "<if test='alertType != null'> AND a.alert_type = #{alertType} </if>" +
            "<if test='startTime != null'> AND a.created_at &gt;= #{startTime} </if>" +
            "<if test='endTime != null'> AND a.created_at &lt;= #{endTime} </if>" +
            "ORDER BY a.created_at DESC" +
            "</script>")
    Page<AlertVo> selectAbnormalBuildingDeviceByPage(IPage<AlertVo> page,@Param("buildingId") Integer buildingId,
                                                     @Param("metricName") String metricName,
                                                     @Param("deviceId") Integer deviceId,@Param("status") Integer status,
                                                     @Param("alertType") AlertRecord.AlertType alertType,@Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime);

    //
    @Select("SELECT " +
            "    b.building_name AS buildingName,\n" +
            "    IFNULL(SUM(er.power_consumption + er.ac_power_consumption + er.water_consumption), 0) AS totalEnergy\n" +
            "FROM buildings b\n" +
            "LEFT JOIN devices d ON b.building_id = d.building_id\n" +
            "LEFT JOIN energy_readings er ON d.device_id = er.device_id\n" +
            "GROUP BY b.building_id, b.building_name " +
            "ORDER BY totalEnergy DESC ")
    List<BuildingEnergyVo> getBuildingEnergyStatistics();

    /**
     * 获取所有设备的最新一条能耗记录（关联建筑信息）
     */
    @Select("SELECT " +
            "    d.device_id AS device_id, " +
            "    d.device_code, " +
            "    b.building_id, " +
            "    b.building_code, " +
            "    b.building_name, " +
            "    er.power_consumption, " +
            "    er.ac_power_consumption, " +
            "    er.water_consumption, " +
            "    er.env_temp, " +
            "    er.humidity, " +
            "    er.occupancy_density, " +
            "    er.monitoring_time " +
            "FROM devices d " +
            "LEFT JOIN buildings b ON d.building_id = b.building_id " +
            "LEFT JOIN ( " +
            "    SELECT device_id, power_consumption, ac_power_consumption, water_consumption, " +
            "           env_temp, humidity, occupancy_density, monitoring_time, " +
            "           ROW_NUMBER() OVER (PARTITION BY device_id ORDER BY monitoring_time DESC) AS rn " +
            "    FROM energy_readings " +
            ") er ON d.device_id = er.device_id AND er.rn = 1 " +
            "WHERE d.device_status = '正常'")  // 只查运行中设备，可根据需要调整
    List<DeviceLatestReadingVO> selectAllDevicesLatestReading();


    @Select("WITH latest_readings AS ( " +
            "    SELECT " +
            "        device_id, " +
            "        monitoring_time, " +
            "        power_consumption, " +
            "        water_consumption, " +
            "        ac_power_consumption, " +
            "        LAG(power_consumption) OVER (PARTITION BY device_id ORDER BY monitoring_time DESC) AS prev_power, " +
            "        LAG(water_consumption) OVER (PARTITION BY device_id ORDER BY monitoring_time DESC) AS prev_water, " +
            "        LAG(ac_power_consumption) OVER (PARTITION BY device_id ORDER BY monitoring_time DESC) AS prev_ac_power, " +
            "        ROW_NUMBER() OVER (PARTITION BY device_id ORDER BY monitoring_time DESC) AS rn " +
            "    FROM energy_readings " +
            ") " +
            "SELECT " +
            "    d.device_code, " +
            "    b.building_code, " +
            "    b.building_name, " +
            "    lr.monitoring_time AS latest_time, " +
            "    lr.power_consumption AS latest_power, " +
            "    lr.water_consumption AS latest_water, " +
            "    lr.ac_power_consumption AS latest_ac_power, " +
            "    CASE WHEN lr.prev_power IS NOT NULL AND lr.prev_power != 0 " +
            "         THEN ROUND((lr.power_consumption - lr.prev_power) / lr.prev_power * 100, 2) " +
            "         ELSE NULL END AS power_change_percent, " +
            "    CASE WHEN lr.prev_water IS NOT NULL AND lr.prev_water != 0 " +
            "         THEN ROUND((lr.water_consumption - lr.prev_water) / lr.prev_water * 100, 2) " +
            "         ELSE NULL END AS water_change_percent, " +
            "    CASE WHEN lr.prev_ac_power IS NOT NULL AND lr.prev_ac_power != 0 " +
            "         THEN ROUND((lr.ac_power_consumption - lr.prev_ac_power) / lr.prev_ac_power * 100, 2) " +
            "         ELSE NULL END AS ac_power_change_percent " +
            "FROM latest_readings lr " +
            "JOIN devices d ON lr.device_id = d.device_id " +
            "LEFT JOIN buildings b ON d.building_id = b.building_id " +
            "WHERE lr.rn = 1")
    List<DeviceLatestCompareVO> getLatestWithPreviousComparison();


    @Select("<script>" +
            "SELECT " +
            "  AVG(t.water_flow_rate) AS waterFlowRate, " +
            "  AVG(t.ac_power) AS acPower, " +
            "  AVG(t.ac_inlet_temp) AS acInletTemp, " +
            "  AVG(t.ac_outlet_temp) AS acOutletTemp " +
            "FROM ( " +
            "  SELECT " +
            "    water_flow_rate, " +
            "    ac_power, " +
            "    ac_inlet_temp, " +
            "    ac_outlet_temp, " +
            "    ROW_NUMBER() OVER (PARTITION BY device_id ORDER BY monitoring_time DESC) AS rn " +
            "  FROM energy_readings " +
            "  <where>" +
            "    <if test='buildingId != null'> AND building_id = #{buildingId} </if> " +
            "    <if test='deviceId != null'> AND device_id = #{deviceId} </if> " +
            "  </where> " +
            ") t " +
            "WHERE t.rn = 1 " +
            "</script>")
    List<energyReadings> select(Integer buildingId, Integer deviceId);


    @Select("<script>" +
            "SELECT " +
            "    AVG(power_consumption * 0.5) AS avgCarbon ," +
            "    monitoring_time  AS timeHour " +
            "FROM energy_readings " +
            "<where>" +
            "   <if test='buildingId != null'> AND building_id = #{buildingId} </if>" +
            "   <if test='deviceId != null'> AND device_id = #{deviceId} </if>" +
            "   <if test='startTime != null'> AND monitoring_time &gt;= #{startTime} </if>" +
            "   <if test='endTime != null'> AND monitoring_time &lt;= #{endTime} </if> " +
            "</where>" +
            "GROUP BY monitoring_time " +
            "ORDER BY monitoring_time " +
            "</script>")
    List<CoVo> getCarbonEmissionTrend(
            @Param("buildingId") Integer buildingId,
            @Param("deviceId") Integer deviceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 获取每个设备最新 topN 条能耗读数（按 monitoring_time 降序）
     * @param topN 每个设备取多少条
     * @return 包含所有设备最新数据的列表
     */
    @Select("SELECT * FROM (" +
            "  SELECT *, ROW_NUMBER() OVER (PARTITION BY device_id ORDER BY monitoring_time DESC) AS rn " +
            "  FROM energy_readings" +
            ") t WHERE t.rn <= #{topN}")
    List<energyReadings> selectLatestNByDevice(@Param("topN") int topN);
}