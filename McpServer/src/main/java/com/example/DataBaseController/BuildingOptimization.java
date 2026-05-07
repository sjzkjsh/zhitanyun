package com.example.DataBaseController;

import com.example.McpServices.BuildingOptimizationMcpTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

@RequestMapping
@RestController
public class BuildingOptimization {

    @Autowired
    private BuildingOptimizationMcpTool buildingOptimizationMcpTool;

    @RequestMapping("/building_optimization")
    public Mono<Map<String,Object>> buildingOptimization(@RequestParam(required = false) Integer buildingId,
                                                         @RequestParam(required = false) Integer deviceId) {
        return  Mono.fromCallable(() ->buildingOptimizationMcpTool.generateOptimizationStrategy(buildingId, deviceId))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
