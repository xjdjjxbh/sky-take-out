package com.sky.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

//@Component           //定时任务也要实例化，交给spring容器去管理
@Slf4j
public class MyTask {
    /**
     * 定时任务，每隔5秒执行一次
     */
    @Scheduled(cron = "0/5 * * * * ? ")
    public void executeTask(){
        log.info("开始执行定时任务:{}",new Date());
    }
}
