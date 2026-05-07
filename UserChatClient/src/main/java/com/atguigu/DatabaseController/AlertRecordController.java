package com.atguigu.DatabaseController;

import com.atguigu.FeignInterface.PageFeign;
import com.atguigu.Result.Result;
import com.example.Entity.AlertRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/alertRecord")
public class AlertRecordController {
    @Autowired
    PageFeign pageFeign;

    @RequestMapping("/update")
    public Result<String> update(@RequestBody AlertRecord alertRecord){
        return pageFeign.update(alertRecord);
    }
    @RequestMapping("/select")
    public Result<List<AlertRecord>> select(){
        return pageFeign.select();
    }

}
