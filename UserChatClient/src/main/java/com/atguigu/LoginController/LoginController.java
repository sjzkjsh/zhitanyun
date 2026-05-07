package com.atguigu.LoginController;

import cn.hutool.captcha.CaptchaUtil;
import com.atguigu.RagFlowService.LoginService;
import com.atguigu.Result.*;
import com.atguigu.UserMapper.UserMapper;
import com.atguigu.Util.LoginUserHolder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class LoginController {

    @Autowired
    private LoginService loginService;
    @Autowired
    private UserMapper userMapper;

    //获取图片验证码
    @RequestMapping("/login/captcha")
    public Result<CaptchaVo> getCaptchaVo(){
        CaptchaVo captchaVo=loginService.getCaptchaVo();

        return Result.success(captchaVo);
    }
    @RequestMapping("/login")
    public Result<String> login(@RequestBody LoginVo loginVo){

        String token = loginService.login(loginVo);

        return Result.success(token);
    }

    @Operation(summary = "获取登陆用户个人信息")
    @GetMapping("/info")
    public Result<User> info() {
        //不需要从token中解析出id、username了，直接从threadlocal中获取loginUser即可。
        Long userId = LoginUserHolder.getLoginUser().getUserId();
        User user = loginService.getById(userId);
        return Result.success(user);
    }

    @PutMapping("/update")
    public Result<String> update(@RequestBody UserUpdateVo updateVo) {
        try {
            boolean success = loginService.updateUserInfo(updateVo);
            return success ? Result.success("更新成功") : Result.error("更新失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("系统异常，请稍后重试");
        }
    }
    @GetMapping("/selectUser")
    public Result<List<User>> selectUser(@RequestParam(required = false) Long id,
                                         @RequestParam(required = false) String name,
                                         @RequestParam(required = false) String phone,
                                         @RequestParam(required = false) String address,
                                         @RequestParam(required = false) String status){
        Long userId = LoginUserHolder.getLoginUser().getUserId();

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(id != null, User::getId, id)
                .like(name != null, User::getName, name)
                .like(phone != null, User::getPhone, phone)
                .like(address != null, User::getAddress, address)
                .eq(status != null, User::getStatus, status);
        if (userId != null) {
            wrapper.ne(User::getId, userId);
        }
        return Result.success(loginService.list(wrapper));
    }


    @Operation(summary = "注册")
    @RequestMapping("/register")
    public Result<String> register(@RequestBody User user) {
        loginService.register(user);
        return Result.success("注册成功");
    }
}
