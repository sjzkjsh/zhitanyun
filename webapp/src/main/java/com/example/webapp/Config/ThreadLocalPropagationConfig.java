package com.example.webapp.Config;

import com.example.webapp.Entity.LoginCustomer;
import com.example.webapp.Util.LoginCustomerHolder;
import io.micrometer.context.ContextRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import com.example.webapp.Entity.LoginCustomer;
import com.example.webapp.Util.LoginCustomerHolder;
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
                        LoginCustomerHolder::getLoginCustomer,         // Supplier
                        loginCustomer -> LoginCustomerHolder.setLoginUser((LoginCustomer) loginCustomer), // Consumer (带强转)
                        LoginCustomerHolder::clear                     // Runnable
                );
    }
}