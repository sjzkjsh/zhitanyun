package com.atguigu.ChatController;

import com.atguigu.RagFlowService.BailianKnowledgeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class Test {

    @Autowired
    private BailianKnowledgeService bailianKnowledgeService;

    @RequestMapping("/test")
    public String test(){
        String s = bailianKnowledgeService.searchKnowledgeBase("《深化工程建设标准化工作改革的意见》");
        return s;
    }
}
