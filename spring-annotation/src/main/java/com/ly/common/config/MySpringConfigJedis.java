package com.ly.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;

/**
 * @author Administrator
 * @date 2018/12/5 11:31
 */
@Configuration
@ComponentScan(basePackages = {"com.ly.cache"})
public class MySpringConfigJedis {

    @Bean(name = "jedisPool")
    public JedisPool getJedisPool() {
        return new JedisPool("192.168.100.109", 6379);
    }

}
