package com.zyd.ecmall.controller;

import com.zyd.ecmall.entity.User;
import com.zyd.ecmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

        @Autowired
        private UserService userService;

        @GetMapping("/user")
        public User user(){
            return userService.getUser();
        }

//        public List<User> user() {
//
//            User user1 = new User();
//            user1.setId(1l);
//            user1.setName("中島");
//
//            User user2 = new User();
//            user2.setId(2l);
//            user2.setName("佐藤");
//
//            List<User> list = new ArrayList<>();
//
//            list.add(user1);
//            list.add(user2);
//
//            return list;
//
//        }


    }
