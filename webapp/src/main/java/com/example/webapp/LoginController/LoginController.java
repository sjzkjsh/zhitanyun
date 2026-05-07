package com.example.webapp.LoginController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.webapp.Entity.*;
import com.example.webapp.Entity.Vo.CaptchaVo;
import com.example.webapp.Entity.Vo.CustomerVo;
import com.example.webapp.Entity.Vo.LoginVo;
import com.example.webapp.Entity.Vo.VO;
import com.example.webapp.Service.LoginService;
import com.example.webapp.Util.LoginCustomerHolder;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private LoginService loginService;


    //获取图片验证码
    @RequestMapping("/login/captcha")
    public Result<CaptchaVo> getCaptchaVo(){
        CaptchaVo captchaVo=loginService.getCaptchaVo();
        return Result.success(captchaVo);
    }
    //登录
    @RequestMapping("/login")
    public Result<String> login(@RequestBody LoginVo loginVo){

        String token = loginService.login(loginVo);
        return Result.success(token);
    }
    //获取用户信息
    @Operation(summary = "获取登陆用户个人信息")
    @GetMapping("/info")
    public Result<CustomerVo> info() {
        //不需要从token中解析出id、username了，直接从threadlocal中获取loginUser即可。
        Long userId = LoginCustomerHolder.getLoginCustomer().getId();
        CustomerVo systemUserInfoVo = loginService.getLoginUserInfoById(userId);
        return Result.success(systemUserInfoVo);
    }
    @GetMapping("/Vo")
    public Result<VO> getVO() {
        Long id = LoginCustomerHolder.getLoginCustomer().getId();
        LambdaQueryWrapper<customer> voLambdaQueryWrapper = new LambdaQueryWrapper<>();
        voLambdaQueryWrapper.eq(customer::getId,id);
        customer one = loginService.getOne(voLambdaQueryWrapper);
        VO vo = new VO();
        BeanUtils.copyProperties(one, vo);
        return Result.success(vo);
    }

    @Operation(summary = "注册")
    @RequestMapping("/register")
    public Result<String> register(@RequestBody customer  user) {
        loginService.register(user);
        return Result.success("注册成功");
    }
}
