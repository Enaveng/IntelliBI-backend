package com.dlwlrma.IntelliBi.job.BiScheduledJob;


import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dlwlrma.IntelliBi.model.entity.AiFrequency;
import com.dlwlrma.IntelliBi.service.AiFrequencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class RefreshAiFrequency {

    @Resource
    private AiFrequencyService aiFrequencyService;

    @Scheduled(cron = "0 0 0 * * ?")  //每天零点执行刷新任务
    public void AiFrequencyJob() {
        //将所有用户的AI调用次数重置
        UpdateWrapper<AiFrequency> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("remainFrequency", 3);
        aiFrequencyService.update(updateWrapper);
        log.info("定时任务执行..");
    }
}
