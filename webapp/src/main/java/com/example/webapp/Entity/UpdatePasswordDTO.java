package com.example.webapp.Entity;


import javax.validation.constraints.Size;
import lombok.Data;
 
@Data
public class UpdatePasswordDTO {
 
    private String oldPassword;
 
    @Size(min = 6, max = 20, message = "新密码长度必须在6-20位之间")
    private String newPassword;
 
    private String confirmPassword;
}