package com.example.webapp.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.webapp.Entity.WorkOrder;
import com.example.webapp.Entity.Vo.WorkerOrderVo;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface WorkerOrderMapper extends BaseMapper<WorkOrder> {


    @Select("select order_no,type,description,priority,status,submit_time,expected_deadline " +
            "from work_order " +
            "where building_id=#{buildingId} and equipment_id=#{deviceId} ")
    List<WorkerOrderVo> getWorkOrder(int buildingId, int deviceId);
}
