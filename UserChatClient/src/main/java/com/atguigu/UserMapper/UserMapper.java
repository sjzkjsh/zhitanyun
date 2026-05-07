package com.atguigu.UserMapper;

import com.atguigu.Result.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    //根据id查询用户
    @Select("select * from user where id=#{userId} ")
    public User selectById(Long userId);
    //根据用户名查询用户
    @Select("select * from user where name=#{name} ")
    User queryUser(String name);


}
