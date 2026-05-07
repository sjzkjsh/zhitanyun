package org.webSocketDemo.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.webSocketDemo.Entity.customer;

@Mapper
public interface CustomerMapper extends BaseMapper<customer> {}