package com.example.webapp.Database;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.webapp.Entity.*;
import com.example.webapp.Mapper.BuildingMapper;
import com.example.webapp.Mapper.CustomerMapper;
import com.example.webapp.Mapper.energyMapper;
import com.example.webapp.Service.ServiceImpl.ComprehensiveAnalysisServiceImpl;
import com.example.webapp.Service.ServiceImpl.CopHealthAnalysisService;
import com.example.webapp.Service.ServiceImpl.CopServiceImpl;
import com.example.webapp.Service.ServiceImpl.ExportService;
import com.example.webapp.Util.LoginCustomerHolder;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class CopController {

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private BuildingMapper buildingMapper;
    @Autowired
    private energyMapper energyMapper;
    @Autowired
    private CopServiceImpl copService;
    @Autowired
    private CopHealthAnalysisService copHealthService;
    @Autowired
    private ComprehensiveAnalysisServiceImpl comprehensiveAnalysisService;
    @Autowired
    private ExportService exportService;

    @RequestMapping("/cop")
    public Result<InstantCopResult> getCop() {

        return Result.success(copService.calculateInstantCop());
    }

    @RequestMapping("/power")
    public Result<LatestEnergyDTO> getPower() {
        Long id = LoginCustomerHolder.getLoginCustomer().getId();
        LambdaQueryWrapper<customer> eq = new LambdaQueryWrapper<customer>().eq(customer::getId, id);
        customer customer = customerMapper.selectOne(eq);
        BuildingDeviceId id1 = buildingMapper.getId(customer.getDeviceCode(), customer.getBuildingCode());
        return Result.success(energyMapper.getLatestEnergyFields(id1.getBuildingId(), id1.getDeviceId()));
    }
    @GetMapping("/cop/health")
    public CopHealthResult getCopHealth() {
        InstantCopResult cop = copService.calculateInstantCop();
        return copHealthService.analyzeCopHealth(cop);
    }
    @GetMapping("/cop/all")
    public Result<ComprehensiveAnalysisResult> getAllCop() {
        Long id = LoginCustomerHolder.getLoginCustomer().getId();
        LambdaQueryWrapper<customer> eq = new LambdaQueryWrapper<customer>().eq(customer::getId, id);
        customer customer = customerMapper.selectOne(eq);
        BuildingDeviceId id1 = buildingMapper.getId(customer.getDeviceCode(), customer.getBuildingCode());
        int buildingId = id1.getBuildingId();
        int deviceId = id1.getDeviceId();
        return Result.success(comprehensiveAnalysisService.analyzeComprehensive( buildingId, deviceId));
    }

    @RequestMapping("/excel/report")
    public void exportComprehensiveReport(HttpServletResponse response) throws IOException {
        exportService.exportComprehensiveReport(response);
    }
}
