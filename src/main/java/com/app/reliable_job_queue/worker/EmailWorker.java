package com.app.reliable_job_queue.worker;

import com.app.reliable_job_queue.model.Job;
import com.app.reliable_job_queue.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailWorker {

    private final QueueService queueService;

    @Scheduled(fixedDelayString = "${queue.poll-delay}")
    public void poll() {

        queueService
                .acquireJob("EMAIL")
                .ifPresent(this::processAsync);

    }

    @Async("queueExecutor")
    private void processAsync(Job job) {

        try{
            System.out.println("Sending Email...");
            queueService.completeJob((job.getId()));
            System.out.println("[Worker Thread] Successfully completed Job #" + job.getId());

        } catch (Exception e){
            System.err.println("[Worker Thread] Failed executing Job #" + job.getId() + ": " + e.getMessage());
            queueService.handleFailure(job.getId(), e.getMessage());
        }

    }
}
