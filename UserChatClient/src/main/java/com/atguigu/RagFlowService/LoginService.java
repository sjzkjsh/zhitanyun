package com.atguigu.RagFlowService;

import com.atguigu.Result.*;
import com.baomidou.mybatisplus.extension.service.IService;

public interface LoginService extends IService<User> {


    CaptchaVo getCaptchaVo();

    String login(LoginVo loginVo);

    SystemUserInfoVo getLoginUserInfoById(Long userId);

    void register(User user);

    boolean updateUserInfo( UserUpdateVo updateVo);
}
