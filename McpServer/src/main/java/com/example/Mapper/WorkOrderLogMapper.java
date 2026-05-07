package com.example.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Entity.WorkOrderLog;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

public interface WorkOrderLogMapper extends BaseMapper<WorkOrderLog> {


    @Select("<script>" +
            "SELECT id, order_id, action, operator_id, content, created_at FROM work_order_log " +
            "<where>" +
            "   <if test='orderId != null'>AND order_id = #{orderId}</if>" +
            "   <if test='action != null and action != \"\"'>AND action = #{action}</if>" +
            "   <if test='operatorId != null'>AND operator_id = #{operatorId}</if>" +
            "   <if test='startTime != null and endTime != null'>" +
            "       AND created_at BETWEEN #{startTime} AND #{endTime}" +
            "   </if>" +
            "   <if test='startTime != null and endTime == null'>" +
            "       AND created_at &gt;= #{startTime}" +
            "   </if>" +
            "   <if test='startTime == null and endTime != null'>" +
            "       AND created_at &lt;= #{endTime}" +
            "   </if>" +
            "</where>" +
            "ORDER BY created_at DESC" +
            "</script>")
    Page<WorkOrderLog> select(Page<WorkOrderLog> page,
                                               @Param("orderId") Long orderId,
                                               @Param("action") String action,
                                               @Param("operatorId") Long operatorId,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);
}
