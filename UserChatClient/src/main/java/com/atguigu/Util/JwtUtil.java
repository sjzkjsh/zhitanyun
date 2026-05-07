package com.atguigu.Util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {

    private static long tokenExpiration = 60 * 240 * 1000L;
    private static SecretKey tokenSignKey =
            Keys.hmacShaKeyFor("M0PKKI6pYGVWWfDZw90a0lTpGYX1d4AQ".getBytes());
    //签名秘钥，用字节数组转换成有效秘钥。

    /**
     * 根据userId和username获取token
     *
     * @param userId
     * @param username
     * @return
     */
    public static String createToken(Long userId, String username) {//创建token
        String token = Jwts.builder().
                setSubject("USER_INFO").//主题
                        setExpiration(new Date(System.currentTimeMillis() + tokenExpiration)).//设置过期时长
                        claim("userId", userId).//设置id
                        claim("username", username).//设置用户姓名
                        signWith(tokenSignKey).//设置密匙
                        compact();
        return token;
    }

    /**
     * 校验前端传入的jwt token合法性，解析出payload。
     *
     * @param token
     * @return
     */
    public static Claims parseToken(String token) {//解析token
        //判断token是否为null

        System.out.println("========== JwtUtil.parseToken ==========");
        System.out.println("传入 token: [" + token + "]");
        System.out.println("token 长度: " + token.length());

        if (token == null) {
            System.out.println("token 为 null，抛出未登录异常");
            throw new RuntimeException("未登录");
        }

        try {
            System.out.println("创建 JwtParser...");
            JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(tokenSignKey).build();
            System.out.println("JwtParser 创建成功");

            System.out.println("开始解析 token...");
            Jws<Claims> jwsClaims = jwtParser.parseClaimsJws(token);
            System.out.println("token 解析成功！");

            Claims body = jwsClaims.getBody();
            System.out.println("解析出的 Claims: " + body);
            System.out.println("userId: " + body.get("userId"));
            System.out.println("username: " + body.get("username"));

            return body;
        } catch (ExpiredJwtException e) {
            System.out.println("token 过期异常: " + e.getMessage());
            throw new RuntimeException("token过期");
        } catch (JwtException e) {
            System.out.println("token 违法异常: " + e.getMessage());
            e.printStackTrace();  // 打印完整堆栈
            throw new RuntimeException("token违法");
        } catch (Exception e) {
            System.out.println("其他异常: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("token解析失败: " + e.getMessage());
        }


    }
}