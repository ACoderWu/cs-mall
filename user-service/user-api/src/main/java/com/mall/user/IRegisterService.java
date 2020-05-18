package com.mall.user;

import com.mall.user.dto.UserRegisterRequest;
import com.mall.user.dto.UserRegisterResponse;

/**
 * @Author: Li Qing
 * @Create: 2020/5/18 14:12
 * @Version: 1.0
 */
public interface IRegisterService {
    UserRegisterResponse register(UserRegisterRequest registerRequest);
}
