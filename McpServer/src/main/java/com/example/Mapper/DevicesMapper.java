package com.example.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Entity.DeviceBuildingVo;
import com.example.Entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DevicesMapper extends BaseMapper<Devices> {

    @Select("select building_id,device_id,device_code,device_status,device_type " +
            "from devices " +
            "where building_id=#{buildingId}")
    List<DeviceBuildingVo> queryDeviceBuildingVo(@Param("buildingId") Integer buildingId);

    @Select("<script> " +
            "select device_status ,COUNT(device_id) as count from devices " +
            "<where>" +
            "<if test='BuildingId != null'> and building_id=#{BuildingId} </if> " +
            "</where>" +
            "group by device_status " +
            "</script>" )
    List<DeviceStatusCountVo> queryDeviceStatusByBuildingId(@Param("BuildingId") Integer BuildingId);

    // ==================== 根据建筑查询 ====================

    // 根据建筑 id 查询设备和建筑信息
    @Select("SELECT b.building_name,b.building_code,b.building_type,b.building_id, d.device_id, d.device_code, d.building_id, d.device_type, " +
            " d.device_status, d.install_date, d.created_at " +
            "FROM devices d " +
            "LEFT JOIN buildings b ON d.building_id = b.building_id " +
            "WHERE d.building_id = #{buildingId}")
    List<BuildingDeviceVo> queryDevicesbyBuildingId(@Param("buildingId") int buildingId);

    // 根据建筑编号查询设备信息
    @Select("SELECT b.building_code,b.building_type,b.building_id,d.device_id, d.device_code, d.building_id, d.device_type, " +
            "d.device_status, d.install_date, d.created_at, b.building_name " +
            "FROM devices d " +
            "LEFT JOIN buildings b ON d.building_id = b.building_id " +
            "WHERE b.building_code = #{buildingCode}")
    List<BuildingDeviceVo> queryDevicesbyBuildingCode(@Param("buildingCode") String buildingCode);

    // 根据建筑名称查询设备信息
    @Select("SELECT b.building_code,b.building_type,b.building_id,d.device_id, d.device_code, d.building_id, d.device_type, " +
            "d.device_status, d.install_date, d.created_at, b.building_name " +
            "FROM devices d " +
            "LEFT JOIN buildings b ON d.building_id = b.building_id " +
            "WHERE b.building_name = #{buildingName}")
    List<BuildingDeviceVo> queryDevicesbyBuildingName(@Param("buildingName") String buildingName);

    // ==================== 查询所有/单个设备 ====================

    /**
     * 查询所有设备列表
     * 包含设备基础信息及关联的建筑名称
     *
     * @return 设备完整信息列表（含建筑名称、运行状态等）
     */
    @Select("SELECT b.building_name, b.building_code,b.building_type,b.building_id,d.device_id, d.device_code, d.building_id, d.device_type, " +
            "d.device_status, d.install_date, d.created_at " +
            "FROM devices d " +
            "LEFT JOIN buildings b ON d.building_id = b.building_id")
    List<BuildingDeviceVo> getAllDevices();

    // 分页查询所有设备列表
    @Select("<script>" +
            "SELECT b.building_id, b.building_name, b.building_type, b.building_code, b.location, " +
            "       d.device_id, d.device_code, d.device_type, d.device_status, d.created_at, d.install_date " +
            "FROM devices d " +
            "LEFT JOIN buildings b ON d.building_id = b.building_id " +
            "<where>" +
            "   <if test='deviceType != null and deviceType != \"\"'> " +
            "       AND d.device_type = #{deviceType} " +
            "   </if>" +
            "   <if test='deviceStatus != null and deviceStatus != \"\"'> " +
            "       AND d.device_status = #{deviceStatus} " +
            "   </if>" +
            "   <if test='buildingName != null and buildingName != \"\"'> " +
            "       AND b.building_name LIKE CONCAT('%', #{buildingName}, '%') " +
            "   </if>" +
            "   <if test='buildingCode != null and buildingCode != \"\"'> " +
            "       AND b.building_code = #{buildingCode} " +
            "   </if>" +
            "</where> " +
            "ORDER BY d.device_id DESC" +
            "</script>")
    Page<BuildingDeviceVo> getPageDevices(IPage<?> page,
                                          @Param("deviceType") String deviceType,
                                          @Param("buildingName") String buildingName,
                                          @Param("deviceStatus") String deviceStatus,
                                          @Param("buildingCode") String buildingCode);

    /**
     * 根据设备ID查询设备详情
     *
     * @param devicesId 设备唯一标识ID
     * @return 设备详细信息（含所属建筑名称）
     */
    @Select("SELECT b.building_code,b.building_type,b.building_id,d.device_id, d.device_code, d.building_id, d.device_type, " +
            "d.device_status, d.install_date, d.created_at, b.building_name " +
            "FROM devices d " +
            "LEFT JOIN buildings b ON d.building_id = b.building_id " +
            "WHERE d.device_id = #{devicesId}")
    List<BuildingDeviceVo> queryDeviceById(@Param("devicesId") int devicesId);

    /**
     * 根据设备编号查询设备详情
     * 设备编号为业务层面的唯一编码（如：DEV-2024-001）
     *
     * @param deviceCode 设备编号
     * @return 设备详细信息（含所属建筑名称）
     */
    @Select("SELECT b.building_code,b.building_type,b.building_id,d.device_id, d.device_code, d.building_id, d.device_type, " +
            "d.device_status, d.install_date, d.created_at, b.building_name " +
            "FROM devices d " +
            "LEFT JOIN buildings b ON d.building_id = b.building_id " +
            "WHERE d.device_code = #{deviceCode}")
    List<BuildingDeviceVo> queryDeviceByCode(@Param("deviceCode") String deviceCode);

    // 根据设备类型查询设备运行状态和设备信息
    @Select("SELECT b.building_code,b.building_type,b.building_id,d.device_id, d.device_code, d.building_id, d.device_type, " +
            "d.device_status, d.install_date, d.created_at, b.building_name " +
            "FROM devices d " +
            "LEFT JOIN buildings b ON d.building_id = b.building_id " +
            "WHERE d.device_type = #{deviceType}")
    List<BuildingDeviceVo> queryDeviceStatusbyType(@Param("deviceType") String deviceType);


    @Select("<script>" +
            "SELECT COUNT(*) FROM devices d " +
            "<where>" +
            "d.device_status = '异常' " +
            "<if test='startTime != null'> AND d.install_date &gt;= #{startTime} </if>" +
            "<if test='endTime != null'> AND d.install_date &lt;= #{endTime} </if>" +
            "</where>" +
            "</script>")
    int queryDeviceStatusCount(@Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);

    @Select("select d.device_id from devices d")
    List<Integer> queryAllDeviceIds();


    @Select("select d.device_code from devices d")
    List<String> queryAllDeviceCode();

    @Select("select d.device_type from devices d")
    List<String> queryAllDeviceType();

    // 统计设备类型
    @Select("SELECT " +
            "d.device_type AS deviceType, " +
            "COUNT(d.device_id) AS deviceCount, " +
            "IFNULL(SUM(er.power_consumption + er.ac_power_consumption + er.water_consumption), 0) AS totalEnergy " +
            "FROM devices d " +
            "LEFT JOIN energy_readings er ON d.device_id = er.device_id " +
            "GROUP BY d.device_type " +
            "ORDER BY d.device_type ")
    List<DeviceEnergyVo> queryDeviceTypeCount();

    @Select("select device_type as deviceType, COUNT(device_id) as deviceCount from devices group by device_type")
    List<DeviceEnergyVo> queryDeviceCount();
    @Select("<script>" +
            "SELECT d.device_id, d.device_code, d.device_type, d.device_status, " +
            "d.install_date, d.created_at, b.building_id, b.building_name, " +
            "b.building_code, b.building_type, b.location " +
            "FROM devices d " +
            "LEFT JOIN buildings b ON d.building_id = b.building_id " +
            "<where>" +
            "   <if test='deviceType != null and deviceType != \"\"'> " +
            "       AND d.device_type = #{deviceType} " +
            "   </if>" +
            "</where> " +
            "ORDER BY d.device_id DESC" +
            "</script>")
    Page<BuildingDeviceVo> pageByDeviceType(Page<?> page, @Param("deviceType") String deviceType);

    @Select("SELECT " +
            "d.device_status AS deviceType, " +
            "COUNT(d.device_id) AS deviceCount, " +
            "IFNULL(SUM(er.power_consumption + er.ac_power_consumption + er.water_consumption), 0) AS totalEnergy " +
            "FROM devices d " +
            "LEFT JOIN energy_readings er ON d.device_id = er.device_id " +
            "GROUP BY d.device_status " +
            "ORDER BY d.device_status ")
    List<DeviceEnergyVo> queryDeviceStatusAndEnergy();

    @Select("SELECT " +
            "d.device_status AS deviceType, " +
            "COUNT(d.device_id) AS deviceCount " +
            "FROM devices d " +
            "LEFT JOIN energy_readings er ON d.device_id = er.device_id " +
            "GROUP BY d.device_status " +
            "ORDER BY d.device_status ")
    List<DeviceEnergyVo> queryDeviceStatus();

    @Select("<script>" +
            "SELECT d.device_code, d.device_type, d.device_status, " +
            "       b.building_name, b.building_code, b.building_type, " +
            "       er.power_consumption, er.water_flow_rate, er.water_consumption, er.ac_power_consumption " +
            "FROM devices d " +
            "LEFT JOIN ( " +
            "    SELECT device_id, MAX(monitoring_time) AS latest_time " +
            "    FROM energy_readings " +
            "    GROUP BY device_id " +
            ") latest ON d.device_id = latest.device_id " +
            "LEFT JOIN energy_readings er ON latest.device_id = er.device_id AND latest.latest_time = er.monitoring_time " +
            "LEFT JOIN buildings b ON d.building_id = b.building_id " +
            "<where>" +
            "   <if test='deviceStatus != null and deviceStatus != \"\"'> " +
            "       AND d.device_status = #{deviceStatus} " +
            "   </if>" +
            "</where> " +
            "ORDER BY d.device_id DESC" +
            "</script>")
    Page<energyDeviceVo> pageByDeviceStatus(Page<?> page, @Param("deviceStatus") String deviceStatus);






}