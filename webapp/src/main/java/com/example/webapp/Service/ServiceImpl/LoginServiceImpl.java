package com.example.webapp.Service.ServiceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.webapp.Entity.*;
import com.example.webapp.Entity.Vo.CaptchaVo;
import com.example.webapp.Entity.Vo.CustomerVo;
import com.example.webapp.Entity.Vo.LoginVo;
import com.example.webapp.Mapper.CustomerMapper;
import com.example.webapp.Service.LoginService;
import com.example.webapp.Util.JwtUtil;
import com.wf.captcha.SpecCaptcha;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl extends ServiceImpl<CustomerMapper, customer> implements LoginService {


        @Autowired
        private StringRedisTemplate stringRedisTemplate;
        @Autowired
        private CustomerMapper Mapper;


        @Override
        public CaptchaVo getCaptchaVo() {
            //创建验证码图片
            SpecCaptcha specCaptcha=new SpecCaptcha(130,48,4);
            specCaptcha.setCharType(SpecCaptcha.TYPE_ONLY_NUMBER);
            String code=specCaptcha.text().toLowerCase();//忽略大小写
            String key = RedisConstant.ADMIN_LOGIN_PREFIX + UUID.randomUUID();//根据规则拼接key
            stringRedisTemplate.opsForValue().set(key, code,
                    RedisConstant.ADMIN_LOGIN_CAPTCHA_TTL_SEC,
                    TimeUnit.SECONDS);//存入redis中，带有过期时间。
            //构造vo并返回。
            String image = specCaptcha.toBase64();//对图片进行base64编码
            return new CaptchaVo(image, key);//返回image+key
        }


        @Override
        public String login(LoginVo loginVo) {
            //判断验证码是否为空
            if(!StringUtils.hasText(loginVo.getCaptchaCode())){
                throw new RuntimeException("验证码不能为空");
            }
            //判断验证码是否正确
            String s = stringRedisTemplate.opsForValue().get(loginVo.getCaptchaKey());
            if(s==null){
                throw new RuntimeException("验证码已过期");
            }
            if(!s.equals(loginVo.getCaptchaCode().toLowerCase())){
                throw new RuntimeException("验证码错误");
            }
            //判断用户是否存在
            customer user=Mapper.queryUser(loginVo.getUsername());
            if(user==null){
                throw new RuntimeException("用户不存在");
            }
            //检验用户状态
            if (user.getStatus()=="禁用"){
                throw new RuntimeException("用户被禁用");
            }
            //检查密码是否正确
            if(!user.getPassword().equals(DigestUtils.md5Hex(loginVo.getPassword()))){
                throw new RuntimeException("密码错误");
            }
            return JwtUtil.createToken(user.getId(), user.getName());
        }


        @Override
        public CustomerVo getLoginUserInfoById(Long userId) {
            //根据userId查询用户
             customer user = Mapper.selectById(userId);
            //构造返回对象CustomerVo
            CustomerVo customerVo = new CustomerVo();
            customerVo.setName(user.getName());
            customerVo.setAvatarUrl(user.getAvatarUrl());
            //返回
            return customerVo;
        }

        @Override
        public void register(customer  user) {
            // 1. 校验用户名、密码等是否为空、格式是否正确
            if (!StringUtils.hasText(user.getName())) {
                throw new RuntimeException("用户名不能为空");
            }
            // 2. 检查用户名是否已存在
            customer existUser = Mapper.queryUser(user.getName());
            if (existUser != null) {
                throw new RuntimeException("用户名已存在");
            }

            // 3. 密码加密（不能明文存储）
            String encryptedPassword = DigestUtils.md5Hex(user.getPassword()); // 建议使用 BCrypt
            // 4. 构建实体对象
            customer use = new customer();
            use.setName(user.getName());
            use.setPassword(encryptedPassword);
            use.setPhone(user.getPhone());
            use.setBuildingCode(user.getBuildingCode());
            use.setDeviceCode(user.getDeviceCode());
            use.setEmail(user.getEmail());
            use.setCreateTime(new Date());
            use.setStatus("正常");
            // 5. 保存到数据库
            Mapper.insert(use);
        }


}
