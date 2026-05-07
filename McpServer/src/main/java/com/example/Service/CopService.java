package com.example.Service;


import com.example.Entity.CopEntity.CopResult;
import com.example.Entity.energyReadings;

import java.util.List;

public interface CopService {

    //计算COP根据设备id和时间段
    CopResult CopCompute(List<energyReadings> readings);

}
