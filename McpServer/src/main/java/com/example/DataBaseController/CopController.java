package com.example.DataBaseController;


import com.example.Entity.CopEntity.CopResult;
import com.example.Entity.ReultEntity.Result;
import com.example.Mapper.EnergyReadingsMapper;
import com.example.Service.CopServiceImpl.CopServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cop")
public class CopController {

    @Autowired
    private CopServiceImpl copService;
    @Autowired
    private EnergyReadingsMapper energyReadingsMapper;

    @RequestMapping("/compute")
    public Result<CopResult> copCompute(@RequestParam(required = false) Integer buildingId,
                                        @RequestParam(required = false)Integer deviceId){
        return Result.success(copService.CopCompute(energyReadingsMapper.select(buildingId, deviceId)));
    }
}
