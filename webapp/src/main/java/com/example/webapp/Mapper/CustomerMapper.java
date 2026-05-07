package com.example.webapp.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.webapp.Entity.customer;
import org.apache.ibatis.annotations.Select;

public interface CustomerMapper extends BaseMapper<customer> {

    @Select("select * from customer where name = #{name}")
    customer queryUser(String name);
    @Select("select * from customer where id = #{id}")
    customer queryUserById(Long id);

    @Select("update customer set password = #{newPasswordMd5} where id = #{id}")
    Boolean updatePassword(String newPasswordMd5, Long id);
}
