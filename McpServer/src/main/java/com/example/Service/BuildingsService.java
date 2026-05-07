package com.example.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.Entity.Buildings;

import java.util.List;

public interface BuildingsService extends IService<Buildings> {

    //查询所有建筑信息
    List<Buildings> queryBuildings();
    //根据建筑id查询建筑信息
    Buildings queryBuildings(int buildingId);
    //根据建筑名称查询建筑信息

     Buildings queryBuildings(String buildingName);
    //根据建筑编号查询建筑信息
     Buildings queryBuildingsbyCode(String buildingCode);
     //汇总建筑数量
     int countBuildings();
}
