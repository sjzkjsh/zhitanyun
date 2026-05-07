package com.example.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.Entity.AlertQueryVO;
import com.example.Entity.AlertRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AlertRecordMapper extends BaseMapper<AlertRecord> {

    @Select("<script>" +
            "SELECT a.building_id AS buildingId, a.device_id AS deviceId, a.device_code AS deviceCode, " +
            "d.device_type AS deviceType, a.metric_name AS metricName, a.abnormal_value AS abnormalValue, " +
            "a.alert_type AS alertType, a.alert_level AS alertLevel, a.status, a.created_at AS createdAt " +
            "FROM alert_record a " +
            "LEFT JOIN devices d ON a.device_id = d.device_id " +
            "WHERE 1=1 " +
            "<if test='buildingId != null'> AND a.building_id = #{buildingId} </if>" +
            "<if test='deviceId != null'> AND a.device_id = #{deviceId} </if>" +
            "ORDER BY a.created_at DESC" +
            "</script>")
    List<AlertQueryVO> queryAlertsByBuildingAndDevice(@Param("buildingId") Integer buildingId,
                                                      @Param("deviceId") Integer deviceId);

}
