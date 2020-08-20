package com.macro.mall.grade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @Author: liuxiang
 * @Date: 2020/6/4
 * @Description:
 */
@EnableDiscoveryClient
@SpringBootApplication
public class MallGradeApplication {
    public static void main(String[] args) {
        SpringApplication.run(MallGradeApplication.class, args);
    }
}
