package com.mall.user;

import com.mall.user.dto.UserRegisterRequest;
import com.mall.user.dto.UserRegisterResponse;

public interface IRegisterService {

    UserRegisterResponse register(UserRegisterRequest registerRequest);
}
