package com.atguigu.DatabaseController;

import com.atguigu.FeignInterface.PageFeign;
import com.atguigu.Result.Result;
import com.example.Entity.CopEntity.CopResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cop")
public class CopController {

    @Autowired
    PageFeign pageFeign;
    @RequestMapping("/compute")
    Result<CopResult> copCompute(@RequestParam(required = false) Integer buildingId,
                                 @RequestParam(required = false)Integer deviceId){
        return pageFeign.copCompute(buildingId,deviceId);
    }
}
