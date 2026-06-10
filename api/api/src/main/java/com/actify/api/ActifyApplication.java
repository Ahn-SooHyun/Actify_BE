package com.actify.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableDiscoveryClient // Eureka Discovery 서비스 자동 등록 활성화
@SpringBootApplication
public class ActifyApplication {
    public static void main(String[] args) {
        SpringApplication.run(ActifyApplication.class, args);
    }



}
