package com.example.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Entity.AnalysisEntity.ThresholdRange;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ThresholdRangeMapper extends BaseMapper<ThresholdRange> {
    @Select("<script>" +
            "SELECT * FROM threshold_range " +
            "<where>" +
            "   <if test='buildingId != null'>AND building_id = #{buildingId}</if>" +
            "   <if test='deviceId != null'>AND device_id = #{deviceId}</if>" +
            "   <if test='metricName != null and metricName != \"\"'>AND metric_name = #{metricName}</if>" +
            "   <if test='startTime != null and endTime != null'>" +
            "       AND (effective_from IS NULL OR effective_from &lt;= #{endTime}) " +
            "       AND (effective_to IS NULL OR effective_to &gt;= #{startTime})" +
            "   </if>" +
            "   <if test='startTime != null and endTime == null'>" +
            "       AND (effective_to IS NULL OR effective_to &gt;= #{startTime})" +
            "   </if>" +
            "   <if test='startTime == null and endTime != null'>" +
            "       AND (effective_from IS NULL OR effective_from &lt;= #{endTime})" +
            "   </if>" +
            "</where>" +
            "ORDER BY effective_from DESC" +
            "</script>")
    Page<ThresholdRange> selectPageWithTimeRange(Page<ThresholdRange> page,
                                                 @Param("buildingId") Integer buildingId,
                                                 @Param("deviceId") Integer deviceId,
                                                 @Param("metricName") String metricName,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);

    @Select("<script>" +
            "SELECT * FROM threshold_range " +
            "WHERE metric_name IN " +
            "   <foreach collection='metrics' item='metric' open='(' separator=',' close=')'>" +
            "       #{metric}" +
            "   </foreach>" +
            "   AND effective_from &lt;= #{time} " +
            "   AND (effective_to IS NULL OR effective_to &gt;= #{time}) " +
            "   <if test='buildingId != null or deviceId != null'>" +
            "       AND ( " +
            "           <if test='deviceId != null'>" +
            "               device_id = #{deviceId} " +
            "           </if>" +
            "           <if test='deviceId == null and buildingId != null'>" +
            "               (building_id = #{buildingId} AND device_id IS NULL) " +
            "           </if>" +
            "       )" +
            "   </if>" +
            "   <if test='buildingId == null and deviceId == null'>" +
            "       AND building_id IS NULL AND device_id IS NULL " +
            "   </if>" +
            "ORDER BY " +
            "   CASE " +
            "       WHEN device_id IS NOT NULL THEN 1 " +
            "       WHEN building_id IS NOT NULL AND device_id IS NULL THEN 2 " +
            "       ELSE 3 " +
            "   END, " +
            "   updated_at DESC" +
            "</script>")
    List<ThresholdRange> selectBatchByMetrics(@Param("metrics") List<String> metrics,
                                              @Param("time") LocalDateTime time,
                                              @Param("buildingId") Integer buildingId,
                                              @Param("deviceId") Integer deviceId);

}