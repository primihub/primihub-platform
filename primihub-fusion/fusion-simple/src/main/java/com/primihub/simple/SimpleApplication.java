package com.primihub.simple;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@MapperScan("com.primihub.**.repository")
@SpringBootApplication(scanBasePackages="com.primihub")
public class SimpleApplication {
    public static void main(String[] args) {
        SpringApplication.run(SimpleApplication.class,args);
    }
}
