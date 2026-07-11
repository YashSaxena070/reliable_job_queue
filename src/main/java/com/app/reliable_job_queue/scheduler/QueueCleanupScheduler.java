package com.app.reliable_job_queue.scheduler;


import com.app.reliable_job_queue.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueueCleanupScheduler {

    private final QueueService queueService;

    @Scheduled(cron = "0 0 2 * * *") //2am
    public void cleanupCompletedJobs(){
        queueService.cleanupCompletedJobs();
    }


}
