package com.example.Service.DataBaseService;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.Entity.energyReadings;
import com.example.Mapper.UpdateEnergyMapper;
import com.example.Service.UpdateEnergyService;
import org.springframework.stereotype.Service;



@Service
public class UpdateEnergyServiceImpl extends ServiceImpl<UpdateEnergyMapper, energyReadings> implements UpdateEnergyService {


}
