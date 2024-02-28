package com.yupi.springbootinit.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class AiManagerTest {

    @Resource
    private AiManager aiManager;

    @Test
    void doChat() {
        aiManager.doChat(1762739391161651201L,"日期 人数 \n" +
                "1号 10人 \n" +
                "2号 20人 \n" +
                "3号 30人 \n" +
                "4号 40人");
    }
}