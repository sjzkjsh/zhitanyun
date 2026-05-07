package com.example.McpServices;



import com.example.Entity.Buildings;
import com.example.Service.BuildingsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class McpBuildingService {
    @Autowired
    private BuildingsService buildingsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Tool(name = "query_buildings",
            description = "查询系统中所有建筑的基础信息。无参数。返回建筑列表，" +
                    "包含：building_id(主键 ID)、building_name、building_code、building_type、location(位置描述)。" +
                    "适用于了解系统有哪些建筑可用。")

    public String queryBuildings() {
        try {
            List<Buildings> queryBuildings = buildingsService.queryBuildings();
            if (queryBuildings == null || queryBuildings.isEmpty()) {
                return "未找到任何建筑信息";
            }
            return objectMapper.writeValueAsString(queryBuildings);
        } catch (Exception e) {
            return "{\"error\": \"序列化失败: " + e.getMessage() + "\"}";
        }
    }

    @Tool(name = "query_building_by_id",
            description = "根据建筑 ID 精确查询单个建筑的详细信息。参数 buildingId:建筑 ID。" +
                    "返回该建筑的完整信息：building_id、building_name(建筑名称)、building_code(建筑编号)、building_type(建筑类型)、location(位置)。" +
                    "适用于已知建筑 ID 来查询建筑信息的快速查询。")
public String queryBuildingsbyId(int buildingId) {
    try {
        Buildings queryBuildings = buildingsService.queryBuildings(buildingId);
        if (queryBuildings == null) {
            return "未找到 buildingId=" + buildingId + " 的建筑信息";
        }
        return objectMapper.writeValueAsString(queryBuildings);
    } catch (Exception e) {
        return "{\"error\": \"序列化失败: " + e.getMessage() + "\"}";
    }
}

@Tool(name = "query_building_by_name",
        description = "根据建筑中文名称模糊查询建筑信息。" +
                "参数 buildingName:建筑名称 (字符串，支持模糊匹配)。" +
                "返回匹配的建筑详细信息：building_id、building_name、building_code、building_type、location。" +
                "适用于只知道建筑名称来查询建筑信息的场景。")
public String queryBuildingsbyName(String buildingName) {
    try {
        Buildings queryBuildings = buildingsService.queryBuildings(buildingName);
        if (queryBuildings == null) {
            return "未找到名称为 " + buildingName + " 的建筑信息";
        }
        return objectMapper.writeValueAsString(queryBuildings);
    } catch (Exception e) {
        return "{\"error\": \"序列化失败: " + e.getMessage() + "\"}";
    }
}

@Tool(name = "query_building_by_code",
        description = "根据建筑编号精确查询建筑信息。" +
                "参数 buildingCode:建筑编号 。" +
                "返回该建筑的详细信息：building_id、building_name、building_code、building_type、location。" +
                "适用于通过标准化编号查询建筑的场景。")
public String queryBuildingsbyCode(String buildingCode) {
    try {
        Buildings queryBuildings = buildingsService.queryBuildingsbyCode(buildingCode);
        if (queryBuildings == null) {
            return "未找到编号为 " + buildingCode + " 的建筑信息";
        }
        return objectMapper.writeValueAsString(queryBuildings);
    } catch (Exception e) {
        return "{\"error\": \"序列化失败: " + e.getMessage() + "\"}";
    }
}

}
