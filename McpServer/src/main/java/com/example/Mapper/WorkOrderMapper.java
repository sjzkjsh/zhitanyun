package com.example.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Entity.*;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

public interface WorkOrderMapper extends BaseMapper<WorkOrder> {
    /**
     * 分页查询工单列表（动态条件）
     */
    @Select("<script>" +
            "SELECT wo.id, wo.order_no, wo.type, wo.description, wo.location, " +
            "       b.building_name, d.device_code, wo.priority, wo.status, " +
            "       wo.submit_time, wo.expected_deadline, wo.completed_time, " +
            "       u.name AS handler_name, wo.remark " +
            "FROM work_order wo " +
            "LEFT JOIN buildings b ON wo.building_id = b.building_id " +
            "LEFT JOIN devices d ON wo.equipment_id = d.device_id " +
            "LEFT JOIN user u ON wo.handler_id = u.id " +
            "WHERE 1=1 " +
            "<if test='status != null and status != \"\"'> AND wo.status = #{status} </if>" +
            "<if test='priority != null and priority != \"\"'> AND wo.priority = #{priority} </if>" +
            "<if test='type != null and type != \"\"'> AND wo.type = #{type} </if>" +
            "<if test='orderNo != null and orderNo != \"\"'> AND wo.order_no LIKE CONCAT('%', #{orderNo}, '%') </if>" +
            "<if test='buildingId != null'> AND wo.building_id = #{buildingId} </if>" +
            "<if test='handlerId != null'> AND wo.handler_id = #{handlerId} </if>" +
            "<if test='startTime != null'> AND wo.submit_time &gt;= #{startTime} </if>" +   // >= 不用转义，但为了统一可以写 &gt;=
            "<if test='endTime != null'> AND wo.submit_time &lt;= #{endTime} </if>" +
            "<if test='overdue != null and overdue'> AND wo.expected_deadline &lt; NOW() AND wo.status NOT IN ('已完成','已关闭') </if>" +
            "ORDER BY wo.submit_time DESC " +
            "</script>")
    Page<WorkOrderListVO> pageQuery(Page<?> page,   // 必须添加 Page 参数
                                    @Param("status") String status,
                                    @Param("priority") String priority,
                                    @Param("type") String type,
                                    @Param("orderNo") String orderNo,
                                    @Param("buildingId") Long buildingId,
                                    @Param("handlerId") Long handlerId,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime,
                                    @Param("overdue") Boolean overdue);


    @Select("select * from work_order where id=#{id}")
    WorkOrder getWorkOrder(int id);

    @Select("SELECT type AS fault_type, COUNT(*) AS count FROM work_order GROUP BY type")
    List<FaultTypeStatVO> countByType();

    @Select("SELECT status AS status, COUNT(*) AS count FROM work_order GROUP BY status")
    List<FaultStatusVO> countByStatus();

    /**
     * 分页查询超时工单列表
     * @return 超时工单列表
     */
    @Select("SELECT * FROM work_order " +
            "WHERE status IN ('待处理','处理中') " +
            "AND expected_deadline IS NOT NULL " +
            "AND expected_deadline < NOW() " +
            "ORDER BY expected_deadline ASC " )
    Page<WorkOrder> selectOverdueOrders(Page<?> page);

    /**
     * 统计超时工单总数
     */
    @Select("SELECT COUNT(*) FROM work_order " +
            "WHERE status IN ('待处理','处理中') " +
            "AND expected_deadline IS NOT NULL " +
            "AND expected_deadline < NOW()")
    long countOverdueOrders();

        @Select("<script>" +
                "SELECT wo.id, wo.order_no, wo.type, wo.description, wo.location, " +
                "       b.building_name, d.device_code, wo.priority, wo.status, " +
                "       wo.submit_time, wo.expected_deadline, wo.completed_time, " +
                "       u.name AS handler_name, wo.remark " +
                "FROM work_order wo " +
                "LEFT JOIN buildings b ON wo.building_id = b.building_id " +
                "LEFT JOIN devices d ON wo.equipment_id = d.device_id " +
                "LEFT JOIN user u ON wo.handler_id = u.id " +
                "where wo.status= #{workerStatus}  " +
                "ORDER BY wo.submit_time DESC " +
                "</script>")
        Page<WorkOrderListVO> WorkerOrderByStatus(Page<Object> page1,@Param("workerStatus") String workerStatus);
        @Select("SELECT " +
            "  wo.status AS statusName, " +
            "  DATE(wo.submit_time) AS createDate, " +
            "  COUNT(*) AS count " +
            "FROM work_order wo " +
            "GROUP BY wo.status, DATE(wo.submit_time) " +
            "ORDER BY wo.status, createDate DESC")
    List<WorkOrderStatusCountVO> countByStatusAndDate();

    @Select("<script>" +
            "SELECT order_no, type, status, expected_deadline " +
            "FROM work_order " +
            "<where>" +
            "  <if test='buildingId != null'> AND building_id = #{buildingId} </if>" +
            "  <if test='deviceId != null'> AND equipment_id = #{deviceId} </if>" +
            "  AND status IN ('待处理', '处理中') " +
            "</where> " +
            "ORDER BY expected_deadline DESC" +
            "</script>")
    List<WorkOrder> GetOrderByBuildingDevice(@Param("buildingId") Long buildingId,
                                             @Param("deviceId") Long deviceId);
}
