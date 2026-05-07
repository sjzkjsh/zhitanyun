package com.atguigu.DatabaseController;

import com.atguigu.FeignInterface.PageFeign;
import jakarta.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class Download {
    @Autowired
    PageFeign pageFeign;
    @GetMapping("/download/{fileName}")
    public ResponseEntity<org.springframework.core.io.Resource> downloadReport(@PathVariable String fileName){
        return pageFeign.downloadReport(fileName);
    }
}
