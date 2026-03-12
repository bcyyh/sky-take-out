package com.sky.task;


import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

//@Component //此类也需要实例化 交给容器管理
@Slf4j
public class MyTask {
    /**
     * 定时任务 每五秒触发一次
     */
    @Scheduled(cron = "0/5 * * * * ? ")
    public void executeTask(){
        log.info("定时任务开始执行:{}",new Date());
    }
}
