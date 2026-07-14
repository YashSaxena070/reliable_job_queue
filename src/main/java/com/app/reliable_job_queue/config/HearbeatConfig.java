package com.app.reliable_job_queue.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class HearbeatConfig {
    @Bean
    public ScheduledExecutorService heartbeatExecutor() {
        return Executors.newScheduledThreadPool(2);
    }
}
