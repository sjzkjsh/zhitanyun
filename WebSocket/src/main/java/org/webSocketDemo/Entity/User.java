package org.webSocketDemo.Entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String password;
    private String email;
    private String phone;
    private String address;
    private String status;
    //登录状态，分为离线、在线
    private String nowStatus;
    private String avatarUrl;
    private Date createTime;

}
