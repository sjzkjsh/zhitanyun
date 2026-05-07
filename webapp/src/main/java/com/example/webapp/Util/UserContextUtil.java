package com.example.webapp.Util;

import com.example.webapp.Entity.customer;
import com.example.webapp.Mapper.CustomerMapper;
import com.example.webapp.Service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用户上下文工具类
 * 统一获取当前登录用户信息，避免重复代码
 */
@Component
public class UserContextUtil {

    @Autowired
    private CustomerMapper customerMapper;

    /**
     * 获取当前登录用户（通过 ThreadLocal 中的 id）
     */
    public customer getCurrentCustomer() {
        Long id = LoginCustomerHolder.getLoginCustomer().getId();
        return customerMapper.selectById(id);
    }

    /**
     * 获取当前用户的设备编码
     */
    public String getCurrentDeviceCode() {
        customer c = getCurrentCustomer();
        return c != null ? c.getDeviceCode() : null;
    }

    /**
     * 获取当前用户的建筑编码
     */
    public String getCurrentBuildingCode() {
        customer c = getCurrentCustomer();
        return c != null ? c.getBuildingCode() : null;
    }

    /**
     * 获取当前用户 ID
     */
    public Long getCurrentUserId() {
        return LoginCustomerHolder.getLoginCustomer().getId();
    }
}
