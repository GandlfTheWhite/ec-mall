package com.zyd.ecmall.service;

import com.zyd.ecmall.entity.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    public User getUser(){

        User user = new User();

        user.setId(1L);

        user.setName("张三");

        return user;

    }

}