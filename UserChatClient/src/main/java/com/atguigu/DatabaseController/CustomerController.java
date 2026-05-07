package com.atguigu.DatabaseController;

import com.atguigu.FeignInterface.PageFeign;

import com.atguigu.Result.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Entity.customer;
import com.example.Entity.customerVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/customer")
public class CustomerController {
    @Autowired
    private PageFeign mapper;
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
                                             @RequestParam(required = false)LocalDateTime endTime) {
        return mapper.customer(page, size, name, email, phone, buildingCode, deviceCode, status, startTime, endTime);
    }

    //回显
    @RequestMapping("/selectByid/{id}")
    public Result<customer> selectCustomerById(@PathVariable("id")  String id){
        return mapper.selectCustomerById(id);
    }
    //修改
    @RequestMapping("/updateByid")
    public Result<Integer> updateCustomerById(@RequestBody customer cust){
        return mapper.updateCustomerById(cust);
    }
    //统计总数
    @RequestMapping("/selectCount")
    Result<Integer> selectCount(){
        return mapper.selectCount();
    }
    //统计正常
    @RequestMapping("/selectCountByStatus")
    Result<Integer> selectCountByStatus(){
        return mapper.selectCountByStatus();
    }
    //统计异常
    @RequestMapping("/selectStatus")
    Result<Integer> selectStatus(){
        return mapper.selectStatus();
    }
}
