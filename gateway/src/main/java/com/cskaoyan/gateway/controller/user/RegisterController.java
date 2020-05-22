package com.cskaoyan.gateway.controller.user;

import com.mall.commons.result.ResponseData;
import com.mall.commons.result.ResponseUtil;
import com.mall.commons.tool.utils.CookieUtil;
import com.mall.user.IKaptchaService;
import com.mall.user.IRegisterService;
import com.mall.user.annotation.Anoymous;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.dto.KaptchaCodeRequest;
import com.mall.user.dto.KaptchaCodeResponse;
import com.mall.user.dto.UserRegisterRequest;
import com.mall.user.dto.UserRegisterResponse;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class RegisterController {
    @Reference(timeout = 3000, check = false)
    private IKaptchaService iKaptchaService;

    @Reference(timeout = 3000, check = false)
    private IRegisterService iRegisterService;

    @Anoymous
    @PostMapping("register")
    public ResponseData register(@RequestBody Map<String, String> map, HttpServletRequest request) {
        String userName = map.get("userName");
        String userPwd = map.get("userPwd");
        String captcha = map.get("captcha");
        String email = map.get("email");

        //验证验证码
        KaptchaCodeRequest kaptchaCodeRequest = new KaptchaCodeRequest();
        String uuid = CookieUtil.getCookieValue(request, "kaptcha_uuid");
        kaptchaCodeRequest.setUuid(uuid);
        kaptchaCodeRequest.setCode(captcha);
        KaptchaCodeResponse response = iKaptchaService.validateKaptchaCode(kaptchaCodeRequest);
        String code = response.getCode();
        if (!code.equals(SysRetCodeConstants.SUCCESS.getCode())) {
            return new ResponseUtil<>().setErrorMsg(response.getMsg());
        }

        //去向用户表中插入记录
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setEmail(email);
        registerRequest.setUserName(userName);
        registerRequest.setUserPwd(userPwd);

        UserRegisterResponse registerResponse = iRegisterService.register(registerRequest);
        if (registerResponse.getCode().equals(SysRetCodeConstants.SUCCESS.getCode())) {
            return new ResponseUtil<>().setData(null);
        }
        return new ResponseUtil<>().setErrorMsg(registerResponse.getMsg());
    }
}
