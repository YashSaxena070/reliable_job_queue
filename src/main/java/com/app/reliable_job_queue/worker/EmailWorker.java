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
import java.util.concurrent.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailWorker {

    private final QueueService queueService;
    private final ProcessedOperationRepository processedOperationRepository;
    private final ScheduledExecutorService heartbeatExecutor;

    @Scheduled(fixedDelayString = "${queue.poll-delay}")
    public void poll() {

        queueService
                .acquireJob("EMAIL")
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

            sendEmail(job);
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
    private void sendEmail(Job job) throws InterruptedException {

        log.info("Sending email for " + job.getPayload());
        if(job.getPayload().contains("fail")) {
            throw new RuntimeException("SMTP Server Down");
        }

        Thread.sleep(500);

    }

}
