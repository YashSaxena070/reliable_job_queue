package com.app.reliable_job_queue.worker;

import com.app.reliable_job_queue.model.Job;
import com.app.reliable_job_queue.model.ProcessedOperation;
import com.app.reliable_job_queue.repository.ProcessedOperationRepository;
import com.app.reliable_job_queue.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationWorker {

    private final QueueService queueService;
    private final ProcessedOperationRepository processedOperationRepository;
    private final ScheduledExecutorService heartbeatExecutor;

    @Scheduled(fixedDelayString = "${queue.poll-delay}")
    public void poll() {

        queueService.acquireJob("NOTIFICATION")
                .ifPresent(this::processAsync);

    }

    @Async("queueExecutor")
    public void processAsync(Job job) {

        if(processedOperationRepository.existsById(job.getIdempotencyKey())) {

            queueService.completeJob(job.getId());

            return;
        }
        ScheduledFuture<?> heartbeatTask = null;

        if (job.isExtendLease()) {
            heartbeatTask = heartbeatExecutor.scheduleAtFixedRate(
                    () -> queueService.heartbeat(job.getId()),
                    10,
                    10,
                    TimeUnit.SECONDS
            );
        }

        try{
            sendNotification(job);
            processedOperationRepository.save(
                    new ProcessedOperation(
                            job.getIdempotencyKey(),
                            LocalDateTime.now()
                    )
            );
            queueService.completeJob((job.getId()));

            log.info("[Worker Thread] Successfully execute "+ job.getId());

        } catch (Exception e){
            log.error(
                    "Failed Job {}",
                    job.getId(),
                    e
            );
            queueService.handleFailure(job.getId(), e.getMessage());
        }
        finally {

            if (heartbeatTask != null) {
                heartbeatTask.cancel(false);
            }

        }

    }

    private void sendNotification(Job job) throws InterruptedException {
        log.info("Sending notification for " + job.getPayload());

        Thread.sleep(500);
    }
}