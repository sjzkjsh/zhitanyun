package com.example.webapp.Database;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.webapp.Entity.LoginCustomer;
import com.example.webapp.Entity.Result;
import com.example.webapp.Entity.UpdatePasswordDTO;
import com.example.webapp.Entity.customer;
import com.example.webapp.Mapper.CustomerMapper;
import com.example.webapp.Util.JwtUtil;
import com.example.webapp.Util.LoginCustomerHolder;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
public class customerController {
    @Autowired
    private CustomerMapper mapper;

    @RequestMapping("/select")
    public Result<customer> getCustomer(@RequestHeader("Authorization") String token){
        LoginCustomer loginCustomer = LoginCustomerHolder.getLoginCustomer();
        LambdaQueryWrapper<customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(customer::getId,loginCustomer.getId());
        customer customer = mapper.selectOne(wrapper);
        return Result.success(customer);
    }
    @GetMapping("/info")
    public Result<customer> getCustomerInfo(){
        Long id = LoginCustomerHolder.getLoginCustomer().getId();
        LambdaQueryWrapper<customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(customer::getId,id);
        customer customer = mapper.selectOne(wrapper);
        return Result.success(customer);
    }

    @PatchMapping("/updateByName")
    public Result updateCustomerByName(String name){
        Long id = LoginCustomerHolder.getLoginCustomer().getId();
        LambdaUpdateWrapper<customer> customerLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        customerLambdaUpdateWrapper.eq(customer::getId,id).set(customer::getName,name);
        return mapper.update(null,customerLambdaUpdateWrapper) > 0 ? Result.success() : Result.error("更新失败");
    }
    @PatchMapping("/updateByPhone")
    public Result updateCustomerByPhone(String phone){
        Long id = LoginCustomerHolder.getLoginCustomer().getId();
        LambdaUpdateWrapper<customer> customerLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        customerLambdaUpdateWrapper.eq(customer::getId,id).set(customer::getPhone,phone);
        return mapper.update(null,customerLambdaUpdateWrapper) > 0 ? Result.success() : Result.error("更新失败");
    }
    @PatchMapping("/updateByEmail")
    public Result updateCustomerByEmail(String email){
        Long id = LoginCustomerHolder.getLoginCustomer().getId();
        LambdaUpdateWrapper<customer> customerLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        customerLambdaUpdateWrapper.eq(customer::getId,id).set(customer::getEmail,email);
        return mapper.update(null,customerLambdaUpdateWrapper) > 0 ? Result.success() : Result.error("更新失败");
    }
    @PatchMapping("/updateByPassword")
    public Result updateCustomerByPassword(@RequestBody UpdatePasswordDTO updatePasswordDTO){
        Long id = LoginCustomerHolder.getLoginCustomer().getId();
        LambdaQueryWrapper<customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(customer::getId,id);
        customer customer = mapper.selectOne(wrapper);
        if (!customer.getPassword().equals(DigestUtils.md5Hex(updatePasswordDTO.getOldPassword()))){
            return Result.error("旧密码错误");
        }
        // 应该改成：
        if(!updatePasswordDTO.getNewPassword().equals(updatePasswordDTO.getConfirmPassword())){
            return Result.error("新密码不一致");
        }
        String newPasswordMd5 = DigestUtils.md5Hex(updatePasswordDTO.getNewPassword());
        Boolean aBoolean = mapper.updatePassword(newPasswordMd5, id);
        if (aBoolean){
            return Result.success();
        }else return Result.error("更新失败");
    }

}
