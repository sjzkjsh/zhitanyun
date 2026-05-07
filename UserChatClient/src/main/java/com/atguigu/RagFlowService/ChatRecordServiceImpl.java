package com.atguigu.RagFlowService;

import com.atguigu.Result.ChatRecord;
import com.atguigu.UserMapper.ChatRecordMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ChatRecordServiceImpl  extends ServiceImpl<ChatRecordMapper, ChatRecord> implements ChatRecordService {
}
