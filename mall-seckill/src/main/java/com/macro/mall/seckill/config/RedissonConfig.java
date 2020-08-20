/*
package com.macro.mall.seckill.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

*/
/**
 * @Author: liuxiang
 * @Date: 2020/5/4
 * @Description:
 *//*

@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient() throws IOException {
        // 本例子使用的是yaml格式的配置文件，读取使用Config.fromYAML，如果是Json文件，则使用Config.fromJSON
        RedissonClient redisson = Redisson.create(
                Config.fromYAML(new ClassPathResource("redisson-config.yml").getInputStream()));
       // Config config = Config.fromYAML(RedissonConfig.class.getClassLoader().getResource("redisson-config.yml"));
        return redisson;
    }
}
*/
