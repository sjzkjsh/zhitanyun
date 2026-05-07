package com.atguigu.DatabaseController;

import com.atguigu.FeignInterface.PageFeign;

import com.example.Entity.ExcelEntity.ImportResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/energy")
public class ImportController {
    @Autowired
    PageFeign pageFeign;

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ImportResultVO importFile(@RequestPart("file") MultipartFile file){

        return pageFeign.importFile(file);
    }
}
