package com.mall.user.services;

import com.mall.user.constants.SysRetCodeConstants;
import com.mall.user.dal.entitys.Member;
import com.mall.user.dal.entitys.UserVerify;
import com.mall.user.dal.persistence.MemberMapper;
import com.mall.user.dal.persistence.UserVerifyMapper;
import com.mall.user.dto.UserVerifyRequest;
import com.mall.user.dto.UserVerifyResponse;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @author: jia.xue
 * @Email: xuejia@cskaoyan.onaliyun.com
 * @Description
 **/
@Service
@Component
public class UserVerifyServiceImpl implements IUserVerifyService {

    @Autowired
    private UserVerifyMapper userVerifyMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Override
    @Transactional
    public UserVerifyResponse verify(UserVerifyRequest request) {
        UserVerifyResponse userVerifyResponse = new UserVerifyResponse();

        request.requestCheck();
        //根据uuid去查询userVerify这个表
        Example example = new Example(UserVerify.class);
        example.createCriteria().andEqualTo("uuid",request.getUuid());
        List<UserVerify> userVerifyList = userVerifyMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(userVerifyList)) {
            userVerifyResponse.setCode(SysRetCodeConstants.USER_INFOR_INVALID.getCode());
            userVerifyResponse.setMsg(SysRetCodeConstants.USER_INFOR_INVALID.getMessage());
            return userVerifyResponse;
        }
        //把两个username进行比对
        UserVerify userVerify = userVerifyList.get(0);
        String userName = request.getUserName();
        if (!userName.equals(userVerify.getUsername())) {
            userVerifyResponse.setCode(SysRetCodeConstants.USER_INFOR_INVALID.getCode());
            userVerifyResponse.setMsg(SysRetCodeConstants.USER_INFOR_INVALID.getMessage());
            return userVerifyResponse;
        }
        userVerify.setIsVerify("Y");
//        example.clear();
//        example.createCriteria().andEqualTo("uuid",request.getUuid());

        //比对成功 改userverify的激活字段
        int effectedRows = userVerifyMapper.updateByExample(userVerify, example);
        if (effectedRows < 1){
            userVerifyResponse.setCode(SysRetCodeConstants.USER_INFOR_UPDATE_FAIL.getCode());
            userVerifyResponse.setMsg(SysRetCodeConstants.USER_INFOR_UPDATE_FAIL.getMessage());
            return userVerifyResponse;
        }


        Example exampleMember = new Example(Member.class);
        exampleMember.createCriteria().andEqualTo("username",request.getUserName());

        List<Member> members = memberMapper.selectByExample(exampleMember);
        if (CollectionUtils.isEmpty(members)){
            userVerifyResponse.setCode(SysRetCodeConstants.USER_INFOR_UPDATE_FAIL.getCode());
            userVerifyResponse.setMsg(SysRetCodeConstants.USER_INFOR_UPDATE_FAIL.getMessage());
            return userVerifyResponse;
        }
        //通过username去查询member 改member表的激活字段
        Member member = members.get(0);

        member.setIsVerified("Y");
        int effectedRows2 = memberMapper.updateByExample(member, exampleMember);
        if (effectedRows2 < 1){
            userVerifyResponse.setCode(SysRetCodeConstants.USER_INFOR_UPDATE_FAIL.getCode());
            userVerifyResponse.setMsg(SysRetCodeConstants.USER_INFOR_UPDATE_FAIL.getMessage());
            return userVerifyResponse;
        }
        userVerifyResponse.setCode(SysRetCodeConstants.SUCCESS.getCode());
        userVerifyResponse.setMsg(SysRetCodeConstants.SUCCESS.getMessage());

        return userVerifyResponse;
    }
}