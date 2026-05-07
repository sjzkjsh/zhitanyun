package com.example.DataBaseController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Entity.ReultEntity.Result;
import com.example.Entity.customer;
import com.example.Entity.customerVo;
import com.example.Mapper.customerMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;

@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private customerMapper mapper;

    @RequestMapping("/customer")
    public Result<Page<customerVo>> customer(@RequestParam(required = false,defaultValue = "1") int  page
    , @RequestParam(defaultValue = "15",required = false) int  size,
                                             @RequestParam(required = false) String name,
                                             @RequestParam(required = false) String email,
                                             @RequestParam(required = false) String phone,
                                             @RequestParam(required = false) String buildingCode,
                                             @RequestParam(required = false) String deviceCode,
                                             @RequestParam(required = false) String status,
                                             @RequestParam(required = false)LocalDateTime startTime,
                                             @RequestParam(required = false)LocalDateTime endTime
                                           ) {

        Page customerPage = new Page(page,size);
        return Result.success(mapper.selectPageWithCondition(customerPage, name, email, phone, buildingCode, deviceCode, status, startTime,endTime));
    }

    //回显
    @RequestMapping("/selectByid/{id}")
    public Result<customer> selectCustomerById(@PathVariable("id") String id){
        return Result.success(mapper.selectById(id));
    }
    //修改
    @RequestMapping("/updateByid")
    public Result<Integer> updateCustomerById(@RequestBody  customer cust){
        return Result.success(mapper.updateById(cust));
    }

    //统计总数
    @RequestMapping("/selectCount")
    public Result<Integer> selectCount(){
        return Result.success(mapper.selectCount());
    }
    //统计正常
    @RequestMapping("/selectCountByStatus")
    public Result<Integer> selectCountByStatus(){
        return Result.success(mapper.selectCountByStatus());
    }
    //统计异常
    @RequestMapping("/selectStatus")
    public Result<Integer> selectStatus(){
        return Result.success(mapper.selectCountByStatus1());
    }

}
