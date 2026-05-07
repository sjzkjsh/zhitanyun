package com.example.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoVo {
    private Double avgCarbon;
    private LocalDateTime timeHour; // 或者使用String，如果时间格式需要处理
}
