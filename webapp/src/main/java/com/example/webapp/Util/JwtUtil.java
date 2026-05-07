package com.example.webapp.Util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {

    private static long tokenExpiration = 60 * 60 * 1000L;//令牌过期时长 1h
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
    //解析token
    public static Claims parseToken(String token) {//解析token
        //判断token是否为null
        if (token == null) {
            throw new RuntimeException("未登录");

        }

        try {
            //jwt解析器
            JwtParser jwtParser = Jwts.parserBuilder()
                    .setSigningKey(tokenSignKey)
                    .build();//为jwt解析器设置签名秘钥。
            //返回payload
            //return jwtParser.parseClaimsJws(token).getBody();//解析token验证签名后，返回payload，即userId、username
            Jws<Claims> jwsClaims = jwtParser.parseClaimsJws(token);//解析token，得到jws（带有签名的jwt）
            return jwsClaims.getBody();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("token过期");
        } catch (JwtException e) {
            throw new RuntimeException("token违法");
        }
    }
}