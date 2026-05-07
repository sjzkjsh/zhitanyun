package com.atguigu.DatabaseController;


import com.atguigu.FeignInterface.PageFeign;
import com.example.Entity.DocumentDTO;


import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api")
public class DocumentController {



    @Autowired
    private PageFeign pageFeign;
    @GetMapping("/documents")
    public List<DocumentDTO> listDocuments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category){
        return pageFeign.listDocuments(keyword,category);
    }

    @GetMapping("/pdf/{id}")
    public ResponseEntity<InputStreamResource> getPdf(@PathVariable Long id, HttpServletRequest request){
        return pageFeign.getPdf(id,request);
    }

}
