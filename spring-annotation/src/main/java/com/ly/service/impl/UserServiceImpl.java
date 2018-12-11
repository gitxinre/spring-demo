package com.ly.service.impl;

import com.ly.dto.User;
import com.ly.service.UserService;
import org.springframework.stereotype.Service;

/**
 * @author Administrator
 * @date 2018/11/29 17:48
 */
@Service
public class UserServiceImpl implements UserService {

    public Integer div(Integer a, Integer b) {
        return  a-b;
    }

    @Override
    public User getUserById(String userId) {
        User user = new User();
        user.setName("孟凡龙");
        user.setId("20181210001mfl");
        return user;
    }
}
