package com.example.webapp.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.webapp.Entity.BuildingDeviceId;
import com.example.webapp.Entity.Buildings;
import org.apache.ibatis.annotations.Select;

public interface BuildingMapper extends BaseMapper<Buildings> {


    @Select("select b.building_id,d.device_id " +
            "from devices d " +
            "left join buildings b on b.building_id=d.building_id " +
            "where d.device_code=#{deviceCode} and b.building_code=#{buildingCode}")
    BuildingDeviceId getId(String deviceCode,String buildingCode);
}
