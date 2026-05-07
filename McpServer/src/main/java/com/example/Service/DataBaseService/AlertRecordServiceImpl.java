package com.example.Service.DataBaseService;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.Entity.AlertRecord;
import com.example.Mapper.AlertRecordMapper;
import com.example.Service.AlertRecordService;
import org.springframework.stereotype.Service;

@Service
public class AlertRecordServiceImpl extends ServiceImpl<AlertRecordMapper, AlertRecord> implements AlertRecordService {


}
