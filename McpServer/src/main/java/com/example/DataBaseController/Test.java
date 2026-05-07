package com.example.DataBaseController;


import com.example.McpServices.McpAnomalyAnalysisService;
import com.example.Service.AnalysisService.ReportExportService;
import com.example.Service.ServiceImpl.RagflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping
public class Test {

    @Autowired
    private ReportExportService reportExportService;
    @Autowired
    private RagflowService bailianKnowledgeService;
    @Autowired
    private McpAnomalyAnalysisService mcpAnomalyAnalysisService;

    @RequestMapping("/test")
    public String test(){
        String s = bailianKnowledgeService.searchKnowledgeBase("《深化工程建设标准化工作改革的意见》");
        return s;
    }

        @GetMapping("/gen")
        public String testGen() {
            try {
                String s = mcpAnomalyAnalysisService.exportAnomalyReport(1, 1);

                return "生成成功：" +s;
            } catch (Exception e) {
                e.printStackTrace();
                return "生成失败：" + e.getMessage();
            }
        }

}
