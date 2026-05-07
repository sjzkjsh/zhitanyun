package com.atguigu.Result;

import lombok.Data;

@Data
public class UserUpdateVo {
    private String name;        // 昵称/姓名
    private String phone;       // 手机号
    private String address;     // 地址
    private String oldPassword; // 旧密码（修改密码时必填）
    private String newPassword; // 新密码
    // 注意：不包含 id、status、role 等敏感/不可修改字段
}