package com.zyd.ecmall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  
public class GulimailPlusApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimailPlusApplication.class, args);
        System.out.println("ECサイトを起動しました！！！");
    }
}
