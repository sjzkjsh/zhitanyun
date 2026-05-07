package com.example.webapp.Service.ServiceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.webapp.Entity.Buildings;
import com.example.webapp.Mapper.BuildingMapper;
import com.example.webapp.Service.BuildingService;
import org.springframework.stereotype.Service;

@Service
public class BuildingServiceImpl extends ServiceImpl<BuildingMapper, Buildings> implements BuildingService {
}
