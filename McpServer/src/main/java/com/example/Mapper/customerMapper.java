package com.example.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Entity.customer;
import com.example.Entity.customerVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.Date;

@Mapper
public interface customerMapper extends BaseMapper<customer> {


        @Select("<script>" +
                "SELECT id, name, email, phone, building_code, device_code, status, create_time " +
                "FROM customer " +
                "<where>" +
                "   <if test='name != null and name != \"\"'>AND name LIKE CONCAT('%', #{name}, '%')</if>" +
                "   <if test='email != null and email != \"\"'>AND email = #{email}</if>" +
                "   <if test='phone != null and phone != \"\"'>AND phone = #{phone}</if>" +
                "   <if test='buildingCode != null and buildingCode != \"\"'>AND building_code = #{buildingCode}</if>" +
                "   <if test='deviceCode != null and deviceCode != \"\"'>AND device_code = #{deviceCode}</if>" +
                "   <if test='status != null and status != \"\"'>AND status = #{status}</if>" +
                "   <if test='startTime != null and endTime != null'>" +
                "       AND create_time BETWEEN #{startTime} AND #{endTime}" +
                "   </if>" +
                "   <if test='startTime != null and endTime == null'>" +
                "       AND create_time &gt;= #{startTime}" +
                "   </if>" +
                "   <if test='startTime == null and endTime != null'>" +
                "       AND create_time &lt;= #{endTime}" +
                "   </if>" +
                "</where>" +
                "ORDER BY create_time DESC" +
                "</script>")
        Page<customerVo> selectPageWithCondition(Page<customerVo> page,
                                                 @Param("name") String name,
                                                 @Param("email") String email,
                                                 @Param("phone") String phone,
                                                 @Param("buildingCode") String buildingCode,
                                                 @Param("deviceCode") String deviceCode,
                                                 @Param("status") String status,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);

        @Select("select  COUNT(id) from customer")
        Integer selectCount();

        @Select("select COUNT(id) from customer where status='正常'")
        Integer selectCountByStatus();
        @Select("select COUNT(id) from customer where status='停用'")
        Integer selectCountByStatus1();
}
