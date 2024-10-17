package com.itheima.cdl.interceptor;

import com.itheima.cdl.util.JwtUtils;
import com.itheima.cdl.util.ThreadLocalUtil;
import io.jsonwebtoken.Claims;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TokenInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");
        if(request.getServletPath().contains("login")){
            return true;
        }
        //判断token是否为空
        if(null == token || "".equals(token)){
            System.out.println("token失效或为空");
            return false;
        }
        //解析token
        Claims claims = JwtUtils.parseJWT(token);
        Object id = claims.get("id");
        //存入threadLocal
        ThreadLocalUtil.setData((Integer) id);

        return true;

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //清除threadLocal中的数据
        ThreadLocalUtil.clean();
    }
}
