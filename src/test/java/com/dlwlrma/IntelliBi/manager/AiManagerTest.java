package com.dlwlrma.IntelliBi.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;


@SpringBootTest
class AiManagerTest {

    @Resource
    private AiManager aiManager;

    @Test
    void doChat() {
        String result = aiManager.doChat(1695303741311295489L,"分析需求:\n" +
                "分析网站增长趋势:\n" +
                "原始数据:\n" +
                "日期,用户数:\n" +
                "1号,20\n" +
                "2号,40\n" +
                "3号,50\n");
        System.out.println(result);
    }
}