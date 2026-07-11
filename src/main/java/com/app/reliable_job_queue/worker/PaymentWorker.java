package com.app.reliable_job_queue.worker;

import com.app.reliable_job_queue.model.Job;
import com.app.reliable_job_queue.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentWorker {

    private final QueueService queueService;

    @Scheduled(fixedDelayString = "${queue.poll-delay}")
    public void poll() {

        queueService.acquireJob("PAYMENT")
                .ifPresent(this::processAsync);

    }

    @Async("queueExecutor")
    public void processAsync(Job job) {

        try {

            System.out.println("Completing payment");

            queueService.completeJob(job.getId());

        } catch (Exception e) {

            queueService.handleFailure(job.getId(), e.getMessage());

        }

    }
}
