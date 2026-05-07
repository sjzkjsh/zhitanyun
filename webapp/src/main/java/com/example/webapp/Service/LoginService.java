package com.example.webapp.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.webapp.Entity.Vo.CaptchaVo;
import com.example.webapp.Entity.Vo.CustomerVo;
import com.example.webapp.Entity.Vo.LoginVo;
import com.example.webapp.Entity.customer;

public interface LoginService extends IService<customer> {
    CaptchaVo getCaptchaVo();

    String login(LoginVo loginVo);

    CustomerVo getLoginUserInfoById(Long userId);

    void register(customer user);
}
