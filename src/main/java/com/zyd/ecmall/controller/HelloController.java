package com.zyd.gulimail.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello(){

        return "Hello Spring Boot!";

    }



}