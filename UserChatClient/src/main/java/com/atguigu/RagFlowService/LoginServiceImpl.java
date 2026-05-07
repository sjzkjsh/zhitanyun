package com.atguigu.RagFlowService;

import com.atguigu.Result.*;
import com.atguigu.UserMapper.UserMapper;
import com.atguigu.Util.JwtUtil;
import com.atguigu.Util.LoginUserHolder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wf.captcha.SpecCaptcha;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl extends ServiceImpl<UserMapper, User> implements LoginService{

    @Autowired
    private StringRedisTemplate  stringRedisTemplate;
    @Autowired
    private UserMapper Mapper;


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
        User user=Mapper.queryUser(loginVo.getUsername());
        if(user==null){
            throw new RuntimeException("用户不存在");
        }
        //检验用户状态
        if ("禁用".equals(user.getStatus())){
            throw new RuntimeException("用户被禁用");
        }
        //检查密码是否正确
        if(!user.getPassword().equals(DigestUtils.md5Hex(loginVo.getPassword()))){
            throw new RuntimeException("密码错误");
        }
        String username = loginVo.getUsername();
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getName, username);
        wrapper.set(User::getNowStatus, "在线");
        Mapper.update(null, wrapper);
        return JwtUtil.createToken(user.getId(), user.getName());
    }


    @Override
    public SystemUserInfoVo getLoginUserInfoById(Long userId) {
        //根据userId查询用户
        User user = Mapper.selectById(userId);
        //构造返回对象systemUserInfoVo
        SystemUserInfoVo systemUserInfoVo = new SystemUserInfoVo();
        systemUserInfoVo.setName(user.getName());
        systemUserInfoVo.setAvatarUrl(user.getAvatarUrl());
        //返回
        return systemUserInfoVo;
    }

    @Override
    public void register(User user) {
        // 1. 校验用户名、密码等是否为空、格式是否正确
        if (!StringUtils.hasText(user.getName())) {
            throw new RuntimeException("用户名不能为空");
        }
        // 2. 检查用户名是否已存在
        User existUser = Mapper.queryUser(user.getName());
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        // 3. 密码加密（不能明文存储）
        String encryptedPassword = DigestUtils.md5Hex(user.getPassword()); // 建议使用 BCrypt
        // 4. 构建实体对象
        User use = new User();
        use.setName(user.getName());
        use.setPassword(encryptedPassword);
        use.setPhone(user.getPhone());
        use.setAddress(user.getAddress());
        use.setEmail(user.getEmail());
        use.setCreateTime(new Date());
        use.setStatus("正常");
        // 5. 保存到数据库
        Mapper.insert(use);
    }

    @Override
    public boolean updateUserInfo(UserUpdateVo updateVo) {
        // 1. 直接从 LoginUserHolder 获取当前登录用户的完整信息
        Long userId = LoginUserHolder.getLoginUser().getUserId();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getId, userId);
        User loginUser = Mapper.selectOne(wrapper);
        if (loginUser == null) {
            throw new RuntimeException("用户未登录");
        }

        // 2. 构建更新条件（用主键精确更新）
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, loginUser.getId());

        // 3. 按需更新普通字段
        if (StringUtils.hasText(updateVo.getName())) {
            updateWrapper.set(User::getName, updateVo.getName());
        }
        if (StringUtils.hasText(updateVo.getPhone())) {
            updateWrapper.set(User::getPhone, updateVo.getPhone());
        }
        if (StringUtils.hasText(updateVo.getAddress())) {
            updateWrapper.set(User::getAddress, updateVo.getAddress());
        }

        // 4. 处理密码修改（用 loginUser 中的密码做校验）
        if (StringUtils.hasText(updateVo.getNewPassword())) {
            if (!StringUtils.hasText(updateVo.getOldPassword())) {
                throw new RuntimeException("修改密码需要提供原密码");
            }

            // 直接用 loginUser 里的密码 MD5 值校验
            String oldPwdMd5 = DigestUtils.md5Hex(updateVo.getOldPassword());
            if (!oldPwdMd5.equals(loginUser.getPassword())) {
                throw new RuntimeException("原密码错误");
            }

            // 新密码加密后更新
            String newPwdMd5 = DigestUtils.md5Hex(updateVo.getNewPassword());
            updateWrapper.set(User::getPassword, newPwdMd5);
        }

        // 5. 执行更新
        int rows = Mapper.update(null, updateWrapper);
        return rows > 0;
    }
}
