package com.atguigu.DatabaseController;

import com.atguigu.FeignInterface.PageFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping
public class BuildingOptimization {

    @Autowired
    PageFeign pageFeign;
    @RequestMapping("/building_optimization")
   Map<String,Object> buildingOptimization(@RequestParam(required = false) Integer buildingId,
                                                  @RequestParam(required = false) Integer deviceId){
        return pageFeign.buildingOptimization(buildingId,deviceId);
    }
}
