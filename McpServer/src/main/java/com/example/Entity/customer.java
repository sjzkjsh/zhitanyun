package com.example.Entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;


@Data
public class customer {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String password;
    private String email;
    private String phone;
    private String buildingCode;
    private String deviceCode;
    private String status;
    private String avatarUrl;
    private LocalDateTime createTime;
}
