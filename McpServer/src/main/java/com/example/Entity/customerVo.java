package com.example.Entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class customerVo {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String buildingCode;
    private String deviceCode;
    private String status;
    private LocalDateTime createTime;

}
