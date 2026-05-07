package com.atguigu.ChatController;

import com.atguigu.RagFlowService.ChatRecordService;
import com.atguigu.Result.ChatRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
	@RequestMapping("/api/chat")
	public class ChatController {
	    @Autowired
	    private ChatRecordService chatRecordService; // 记得要写 Service 和 Mapper
	    @PostMapping("/save")
	    public String saveMessage(@RequestBody ChatRecord chatRecord) {
	        // 此时 chatRecord 对象里已经自动填充了
	        // senderId, senderName, content 等字段 (因为 9092 传过来的 JSON 字段名一样)
	        // 设置创建时间
	        chatRecord.setCreateTime(LocalDateTime.now());
	        // 保存到数据库
	        chatRecordService.save(chatRecord);
	        return "success";
	    }
	}