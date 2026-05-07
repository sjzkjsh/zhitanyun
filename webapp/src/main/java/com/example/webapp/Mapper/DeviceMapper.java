package com.example.webapp.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.webapp.Entity.Devices;
import com.example.webapp.Entity.buildingDevice;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface DeviceMapper extends BaseMapper<Devices> {
    @Select("SELECT " +
            "b.building_name, " +
            "b.building_type, " +
            "d.device_code, " +
            "d.device_type, " +
            "d.device_status " +
            "FROM devices d " +
            "LEFT JOIN buildings b ON b.building_id = d.building_id " +
            "WHERE d.device_code = #{deviceCode} " +
            "AND b.building_code = #{buildingCode}")
    buildingDevice selectBuildingAndDevice(@Param("deviceCode") String deviceCode,
                                             @Param("buildingCode") String buildingCode);

}
