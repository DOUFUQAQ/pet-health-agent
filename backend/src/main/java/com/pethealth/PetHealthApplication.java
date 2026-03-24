package com.pethealth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 智能宠物健康管家系统 - 主启动类
 * 
 * @author Pet Health Team
 * @version 1.0.0
 */
@SpringBootApplication
@MapperScan("com.pethealth.mapper")
@EnableAsync
@EnableScheduling
public class PetHealthApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetHealthApplication.class, args);
        System.out.println("""
                
                ====================================
                  智能宠物健康管家系统启动成功！
                  Pet Health Management System
                  Version: 1.0.0
                  Port: 8080
                  Context-Path: /api
                ====================================
                """);
    }
}
