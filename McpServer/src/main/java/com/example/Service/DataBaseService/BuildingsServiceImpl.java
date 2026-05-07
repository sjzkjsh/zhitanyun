package com.example.Service.DataBaseService;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.Entity.Buildings;
import com.example.Mapper.BuildingsMapper;
import com.example.Repository.BloomFilterHelper;
import com.example.Service.BuildingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuildingsServiceImpl extends ServiceImpl<BuildingsMapper, Buildings> implements BuildingsService {

    @Autowired
    private BuildingsMapper buildingsMapper;


    @Autowired
    private BloomFilterHelper bloomFilterHelper;


    @Cacheable(
            value = "buildings",
            key = "'all'",
            unless = "#result == null || #result.isEmpty()"
    )
    @Override
    public List<Buildings> queryBuildings() {
        return buildingsMapper.getAllBuildings();
    }

    //根据建筑 id 查询建筑信息
    @Cacheable(value = "buildings",
            key = "#buildingId",
            unless = "#result == null")
    @Override
    public Buildings queryBuildings(int buildingId) {
        try {
            if(!bloomFilterHelper.mightContain(BloomFilterHelper.BLOOM_FILTER_BUILDING, buildingId)){
                return null;
            }
        } catch (Exception e) {
            // 如果布隆过滤器异常，直接查询数据库
            return buildingsMapper.queryBuildingById(buildingId);
        }
        return buildingsMapper.queryBuildingById(buildingId);
    }

    @Cacheable(value = "buildings",
            key = "#buildingName",
            unless = "#result == null")
    @Override
    public Buildings queryBuildings(String buildingName) {
        if(!bloomFilterHelper.mightContain(BloomFilterHelper.BLOOM_FILTER_BUILDING, buildingName)){
            return null;
        }
            return buildingsMapper.queryBuildingByName(buildingName);
    }

    @Cacheable(value = "buildings",
            key = "#buildingCode",
            unless = "#result == null")
    @Override
    public Buildings queryBuildingsbyCode(String buildingCode) {
        if(!bloomFilterHelper.mightContain(BloomFilterHelper.BLOOM_FILTER_BUILDING, buildingCode)){
            return null;
        }
        return buildingsMapper.queryBuildingByCode(buildingCode);
    }

    @Cacheable(value = "buildings",
            key = "'count'",
            unless = "#result == 0")
    @Override
    public int countBuildings() {
        return buildingsMapper.countBuildings();
    }
}
