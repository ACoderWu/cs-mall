package com.mall.user.services;

import com.alibaba.fastjson.JSON;
import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.converter.UserConverterMapper;
import com.mall.user.dal.entitys.Member;
import com.mall.user.dal.persistence.MemberMapper;
import com.mall.user.dto.CheckAuthRequest;
import com.mall.user.dto.CheckAuthResponse;
import com.mall.user.dto.UserLoginRequest;
import com.mall.user.dto.UserLoginResponse;
import com.mall.user.utils.JwtTokenUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: jia.xue
 * @Email: xuejia@cskaoyan.onaliyun.com
 * @Description
 **/
@Service
@Component
public class LoginServiceImpl implements ILoginService {

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private UserConverterMapper userConverterMapper;
    @Override
    public UserLoginResponse login(UserLoginRequest request) {
        UserLoginResponse response = new UserLoginResponse();
        // 验证用户名和密码
        request.requestCheck();
        Example example = new Example(Member.class);
        example.createCriteria().andEqualTo("username",request.getUserName());
        List<Member> memberList = memberMapper.selectByExample(example);

        if (CollectionUtils.isEmpty(memberList)){
            response.setCode(SysRetCodeConstants.USERORPASSWORD_ERRROR.getCode());
            response.setMsg(SysRetCodeConstants.USERORPASSWORD_ERRROR.getMessage());
            return response;
        }

        Member member = memberList.get(0);

        // TODO 判断用户否已经激活



        String password = request.getPassword();
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!md5Password.equals(member.getPassword())){
            response.setCode(SysRetCodeConstants.USERORPASSWORD_ERRROR.getCode());
            response.setMsg(SysRetCodeConstants.USERORPASSWORD_ERRROR.getMessage());
            return response;
        }

        // 产生JWT token
        Map map = new HashMap<String,Object>();
        map.put("uid",member.getId());
        map.put("file",member.getFile());
        map.put("username",member.getUsername());
        String token = JwtTokenUtils.builder().msg(JSON.toJSONString(map)).build().creatJwtToken();

        response = userConverterMapper.converter(member);
        response.setToken(token);
        response.setCode(SysRetCodeConstants.SUCCESS.getCode());
        response.setMsg(SysRetCodeConstants.SUCCESS.getMessage());
        return response;

    }

    /**
     * 验证token
     * @param checkAuthRequest
     * @return
     */
    @Override
    public CheckAuthResponse validToken(CheckAuthRequest checkAuthRequest) {
        CheckAuthResponse response = new CheckAuthResponse();
        checkAuthRequest.requestCheck();
        String userInfo = JwtTokenUtils.builder().token(checkAuthRequest.getToken()).build().freeJwt();
        if (StringUtils.isEmpty(userInfo)) {
            response.setCode(SysRetCodeConstants.TOKEN_VALID_FAILED.getCode());
            response.setMsg(SysRetCodeConstants.TOKEN_VALID_FAILED.getMessage());
            return  response;
        }

        response.setUserinfo(userInfo);
        response.setMsg(SysRetCodeConstants.SUCCESS.getMessage());
        response.setCode(SysRetCodeConstants.SUCCESS.getCode());
        return response;
    }
}