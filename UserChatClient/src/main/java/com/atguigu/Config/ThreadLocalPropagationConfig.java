package com.atguigu.Config;


import com.atguigu.Result.LoginUser;
import com.atguigu.Util.LoginUserHolder;
import io.micrometer.context.ContextRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThreadLocalPropagationConfig {

    @PostConstruct
    public void init() {
        ContextRegistry.getInstance()
                .registerThreadLocalAccessor(
                        "loginCustomer",                               // 唯一标识
                        LoginUserHolder::getLoginUser,         // Supplier
                        loginUser -> LoginUserHolder.setLoginUser((LoginUser) loginUser), // Consumer (带强转)
                        LoginUserHolder::clear                     // Runnable
                );
    }
}