package com.itheima.cdl.controller;

import com.itheima.cdl.util.JwtUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/cdl/user")
public class LoginController {

    @GetMapping("/login")
    public Map<String,String> login(String username,String password){

        Map<String,Object> clamis = new HashMap<>();
        clamis.put("id",1102);
        clamis.put("username","username");
        //生成jwt字符串
        String token = JwtUtils.generateJwt(clamis);

        //结果返回
        Map<String,String> result = new HashMap<>();
        result.put("token",token);

        return result;
    }
}
