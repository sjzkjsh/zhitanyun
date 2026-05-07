package com.example.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.Entity.BuildingVo;
import com.example.Entity.Buildings;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BuildingsMapper extends BaseMapper<Buildings> {
    @Select("select building_id,building_name,building_code from buildings " )
    List<BuildingVo> queryBuildingIdAndName();

    // 查询所有建筑信息
    @Select("SELECT b.building_id, b.building_name, b.building_code, " +
            "b.building_type, b.location, b.created_at " +
            "FROM buildings b")
    List<Buildings> getAllBuildings();

    // 根据建筑 id 查询
    @Select("SELECT b.building_id, b.building_name, b.building_code, " +
            "b.building_type, b.location, b.created_at " +
            "FROM buildings b " +
            "WHERE b.building_id = #{buildingId}")
    Buildings queryBuildingById(@Param("buildingId") int buildingId);

    // 根据建筑名称查询
    @Select("SELECT b.building_id, b.building_name, b.building_code, " +
            "b.building_type, b.location, b.created_at " +
            "FROM buildings b " +
            "WHERE b.building_name = #{buildingName}")
    Buildings queryBuildingByName(@Param("buildingName") String buildingName);

    // 根据建筑编号查询
    @Select("SELECT b.building_id, b.building_name, b.building_code, " +
            "b.building_type, b.location, b.created_at " +
            "FROM buildings b " +
            "WHERE b.building_code = #{buildingCode}")
    Buildings queryBuildingByCode(@Param("buildingCode") String buildingCode);

    //汇总建筑数量
    @Select("SELECT COUNT(*) AS building_count " +
            "from buildings")
    int countBuildings();

    @Select("select b.building_id from buildings b")
    List<Integer> getAllBuildingId();
    @Select("select b.building_code from buildings b")
    List<String> getAllBuildingCode();
    @Select("select b.building_name from buildings b")
    List<String> getAllBuildingName();

}