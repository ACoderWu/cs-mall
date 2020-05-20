package com.cskaoyan.gateway.controller.user;

import com.alibaba.fastjson.JSON;
import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.commons.tool.utils.CookieUtil;
import com.mall.user.IKaptchaService;
import com.mall.user.ILoginService;
import com.mall.user.annotation.Anoymous;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.dto.KaptchaCodeRequest;
import com.mall.user.dto.KaptchaCodeResponse;
import com.mall.user.dto.UserLoginRequest;
import com.mall.user.dto.UserLoginResponse;
import com.mall.user.intercepter.TokenIntercepter;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.omg.PortableInterceptor.Interceptor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class LoginController {

    @Reference
    private IKaptchaService iKaptchaService;

    @Reference
    private ILoginService iLoginService;


    @PostMapping("/login")
    @Anoymous
    public ResponseData login(@RequestBody Map<String, String> map, HttpServletRequest request, HttpServletResponse response) {
        String userName = map.get("userName");
        String userPwd = map.get("userPwd");
        String captcha = map.get("captcha");

        //验证验证码
        KaptchaCodeRequest kaptchaCodeRequest = new KaptchaCodeRequest();
        String uuid = CookieUtil.getCookieValue(request, "kaptcha_uuid");
        kaptchaCodeRequest.setUuid(uuid);
        kaptchaCodeRequest.setCode(captcha);
        KaptchaCodeResponse kaptchaCodeResponse = iKaptchaService.validateKaptchaCode(kaptchaCodeRequest);
        String code = kaptchaCodeResponse.getCode();
        if (!code.equals(SysRetCodeConstants.SUCCESS.getCode())) {
            return new ResponseUtil<>().setErrorMsg(kaptchaCodeResponse.getMsg());
        }

        //验证用户名和密码
        UserLoginRequest userLoginRequest = new UserLoginRequest();
        userLoginRequest.setPassword(userPwd);
        userLoginRequest.setUserName(userName);

        UserLoginResponse userLoginResponse = iLoginService.login(userLoginRequest);
        if (userLoginResponse.getCode().equals(SysRetCodeConstants.SUCCESS.getCode())) {
            //设置cookie
            Cookie cookie = CookieUtil.genCookie(TokenIntercepter.ACCESS_TOKEN, userLoginResponse.getToken(), "/", 24 * 60 * 60);
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
            return new ResponseUtil<>().setData(userLoginResponse);
        }
        return new ResponseUtil<>().setErrorMsg(userLoginResponse.getMsg());
    }

    @GetMapping("/login")
    public ResponseData login(HttpServletRequest request, HttpServletResponse response) {
        String userInfo = (String) request.getAttribute(TokenIntercepter.USER_INFO_KEY);
        Object object = JSON.parse(userInfo);
        return new ResponseUtil<>().setData(object);
    }

    @GetMapping("/logout")
    public ResponseData logOut(HttpServletRequest request, HttpServletResponse response){
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for (Cookie cookie : cookies) {
                if(cookie.getName().equals(TokenIntercepter.ACCESS_TOKEN)){
                    cookie.setValue(null);
                    cookie.setMaxAge(0);//让该cookie立即过期
                    cookie.setPath("/");
                    response.addCookie(cookie);
                }
            }
        }
        return new ResponseUtil<>().setData(null);
    }
}