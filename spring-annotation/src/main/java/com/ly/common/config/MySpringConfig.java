package com.ly.common.config;

import com.ly.dto.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Administrator
 * @date 2018/11/27 17:24
 */
@Configuration
@ComponentScan(basePackages = {"com.ly", "com.yf"})
public class MySpringConfig {


    @Bean(name = {"user001", "user002"}, initMethod = "init", destroyMethod = "destory")
    public User getUser() {
        return new User();
    }

}
