package com.example.webapp.Entity.Vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VO {
    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户姓名")
    private String name;

    @Schema(description = "用户头像")
    private String avatarUrl;

    // 以下为新增字段，对应 customer 实体
    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "楼栋编码")
    private String buildingCode;

    @Schema(description = "设备编码")
    private String deviceCode;

    @Schema(description = "账号状态")
    private String status;

    @Schema(description = "注册时间")
    private Date createTime;
}
