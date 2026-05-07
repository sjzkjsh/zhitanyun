package com.example.webapp.Service.ServiceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.webapp.Entity.WorkOrder;
import com.example.webapp.Mapper.WorkerOrderMapper;
import com.example.webapp.Service.WorkerOrderService;
import org.springframework.stereotype.Service;

@Service
public class WorkerOrderServiceImpl extends ServiceImpl<WorkerOrderMapper,WorkOrder> implements WorkerOrderService {
}
