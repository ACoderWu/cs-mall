package com.mall.pay.bootstrap;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @Author: Li Qing
 * @Create: 2020/5/16 17:28
 * @Version: 1.0
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.mall.pay")
@MapperScan(basePackages = "com.mall.pay.dal")
public class PayProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayProviderApplication.class, args);
    }
}
