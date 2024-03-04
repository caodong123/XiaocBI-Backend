package com.yupi.springbootinit.config;


import io.swagger.models.auth.In;
import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties("spring.redis")   //自动赋值
public class RedissonConfig {

    private String host;

    private String port;

    private Integer password;

    private Integer database;

    @Bean
    public RedissonClient getRedissonClient(){
        // 创建Redisson客户端连接
        Config config = new Config();
        config.useSingleServer()   //添加单机redis配置
                        .setAddress("redis://"+host+":"+port)
                                .setDatabase(database);

        RedissonClient redisson = Redisson.create(config);

       return redisson;
    }
}
