package com.atguigu.RagFlowService;

import com.atguigu.Result.LoginUser;
import com.atguigu.Util.JwtUtil;
import com.atguigu.Util.LoginUserHolder;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 前端登录后，后续请求都将JWT，放置于HTTP请求的Header中，其Header的key为`access-token`。
        String token = request.getHeader("access-token");

        System.out.println("========== AuthenticationInterceptor ==========");
        System.out.println("请求路径: " + request.getRequestURI());
        System.out.println("收到 token: " + token);
        System.out.println("token 长度: " + (token != null ? token.length() : 0));
        // 解析该token，如果成功则放行，如果失败，则拦截。因为在parseToken中抛出异常，所以这里不需要显式拦截。
        Claims claims = JwtUtil.parseToken(token);

        Long userId = claims.get("userId", Long.class);//从token中解析出userId
        String username = claims.get("username", String.class);//从token中解析出username

        LoginUserHolder.setLoginUser(new LoginUser(userId, username));//将loginUser放入threadlocal中。

        // 放行。
        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //释放线程资源。
        LoginUserHolder.clear();
    }
}
