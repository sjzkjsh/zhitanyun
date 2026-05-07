package com.example.webapp.Util;


import com.example.webapp.Entity.LoginCustomer;



public class LoginCustomerHolder {

    public static ThreadLocal<LoginCustomer> threadLocal = new ThreadLocal<>();//threadlocal

    public static void setLoginUser(LoginCustomer loginCustomer ) {
        threadLocal.set(loginCustomer);
    }

    public static LoginCustomer getLoginCustomer() {
        return threadLocal.get();
    }

    public static void clear() {
        threadLocal.remove();
    }
}