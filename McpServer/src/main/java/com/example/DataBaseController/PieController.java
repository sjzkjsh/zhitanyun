package com.example.DataBaseController;

import com.example.Entity.BuildingEnergyVo;
import com.example.Entity.PieEntity.EnergyPieResult;
import com.example.Entity.ReultEntity.Result;
import com.example.Service.EnergyReadingsService;
import com.example.Service.PieService.PieEnergyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/energy")
public class PieController {
    @Autowired
    private PieEnergyService pie;
    @Autowired
    private EnergyReadingsService energyReadingsService;

    @GetMapping("/pie")
    public Result<EnergyPieResult> getEnergyPie(
            @RequestParam(required = false) Integer buildingId,
            @RequestParam(required = false) Integer deviceId) {

        EnergyPieResult result = pie.getEnergyPie(buildingId, deviceId);
        return Result.success(result);
    }

    @GetMapping("/buildingEnergy")
    public Result<List<BuildingEnergyVo>> getBuildingEnergy(){
        return Result.success(energyReadingsService.QueryBuildingEnergy());
    }
}
