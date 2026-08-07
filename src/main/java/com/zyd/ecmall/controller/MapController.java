package com.zyd.ecmall.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class MapController {

        @GetMapping("/map")
        public Map<String,Object> map() {

//            User user1 = new User();
//            user1.setId(1l);
//            user1.setName("中島");
//
//            User user2 = new User();
//            user2.setId(2l);
//            user2.setName("佐藤");

            Map<String, Object> map = new HashMap<>();

            map.put("name","中島");
            map.put("age",18);

            return map;

        }


    }
