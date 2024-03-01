package com.yupi.springbootinit.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedisLimiterManagerTest {

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Test
    void doLimiter() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            redisLimiterManager.doLimiter("123");
            Thread.sleep(1000);
            System.out.println("成功");
        }
    }
}