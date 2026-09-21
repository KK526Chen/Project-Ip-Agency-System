package com.ipagency;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.ipagency.mapper")
@SpringBootApplication
public class IpAgencyApplication {
    public static void main(String[] args) {
        SpringApplication.run(IpAgencyApplication.class, args);
    }
}
