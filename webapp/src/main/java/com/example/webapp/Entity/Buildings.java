package com.example.webapp.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Buildings {

    private int buildingId;//建筑id
    private String buildingName;//建筑名称
    private String buildingCode;//建筑编号
    private String buildingType;//建筑类型
    private String location;//位置
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;//创建时间
}
