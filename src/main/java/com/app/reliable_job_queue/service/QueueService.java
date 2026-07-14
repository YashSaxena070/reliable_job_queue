package com.app.reliable_job_queue.service;

import com.app.reliable_job_queue.dto.QueueStatsResponse;
import com.app.reliable_job_queue.enums.RetryPolicy;
import com.app.reliable_job_queue.model.Job;
import com.app.reliable_job_queue.enums.JobStatus;
import com.app.reliable_job_queue.repository.JobRepository;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Getter
@Setter
@Data
public class QueueService {

    private final JobRepository jobRepository;

    @Value("${queue.lease-duration}")
    private int leaseDuration;

    @Value("${queue.retry.linear-delay}")
    private long linearDelay;

    @Value("${queue.retry.exponential-base}")
    private int expoBase;

    @Transactional
    public Job submitJob(String payload, String queueName, int priority, int maxAttempts, int delaySeconds, RetryPolicy retryPolicy, int timeoutSeconds, boolean isExtendLease ) {
        Job job = Job.builder()
                .payload(payload)
                .idempotencyKey(UUID.randomUUID().toString())
                .queueName(queueName)
                .isExtendLease(isExtendLease)
                .status(JobStatus.PENDING)
                .priority(priority)
                .startedAt(null)
                .timeoutSeconds(timeoutSeconds)
                .attempts(0)
                .maxAttempts(maxAttempts)
                .retryPolicy(retryPolicy)
                .runAt(LocalDateTime.now().plusSeconds(delaySeconds))
                .lockedUntil(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
        return (Job) jobRepository.save(job);
    }

    @Transactional
    public Optional<Job> acquireJob(String queueName) {
        LocalDateTime now = LocalDateTime.now();

        // Added <Job> generic type here
        List<Job> available = jobRepository.findNextAvailableJob(queueName,now, PageRequest.of(0, 1));

        if (available.isEmpty()) {
            return Optional.empty();
        }

        Job job = available.get(0);
        job.setStartedAt(LocalDateTime.now());
        job.setStatus(JobStatus.PROCESSING);
        job.setAttempts(job.getAttempts() + 1);
        job.setLockedUntil(LocalDateTime.now().plusSeconds(leaseDuration));

        return Optional.of(jobRepository.save(job));
    }

    @Transactional
    public void completeJob(Long jobId) {
        jobRepository.findById(jobId).ifPresent(job ->{
            job.setStatus(JobStatus.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());
            job.setLockedUntil(null);
            jobRepository.save(job);
        });
    }

    @Transactional
    public void handleFailure(Long jobId, String errorMessage) {

        jobRepository.findById(jobId).ifPresent(job -> {

            job.setLastError(errorMessage);
            job.setLastFailedAt(LocalDateTime.now());
            if (job.getAttempts() >= job.getMaxAttempts()) {
                job.setStatus(JobStatus.DLQ);
                job.setLockedUntil(null);
            } else {
                long retryDelay = switch (job.getRetryPolicy()) {

                    case NONE -> 0;

                    case LINEAR -> job.getAttempts() * linearDelay;

                    case EXPONENTIAL -> (long) Math.pow(expoBase, job.getAttempts()) * 2;
                };

                if (job.getRetryPolicy() == RetryPolicy.NONE) {

                    job.setStatus(JobStatus.DLQ);
                    job.setLockedUntil(null);

                } else{
                    job.setRunAt(LocalDateTime.now().plusSeconds(retryDelay));
                    job.setLockedUntil(LocalDateTime.now());
                    job.setStatus(JobStatus.PENDING);
                }
            }
            jobRepository.save(job);
        });
    }

    @Transactional
    public void cleanupCompletedJobs() {
        LocalDateTime cutOff = LocalDateTime.now().minusDays(7);

        jobRepository.deleteCompletedJobsOlderThan(cutOff);

    }

    public void heartbeat(Long jobId){
        jobRepository.findById(jobId).ifPresent(job ->{
            if(job.getStatus() == JobStatus.PROCESSING){
                job.setLockedUntil(
                        LocalDateTime.now().plusSeconds(leaseDuration)
                );

                jobRepository.save(job);
            }
        });
    }

    public QueueStatsResponse getQueueStats() {

        return new QueueStatsResponse(

                jobRepository.countByStatus(JobStatus.PENDING),

                jobRepository.countByStatus(JobStatus.PROCESSING),

                jobRepository.countByStatus(JobStatus.COMPLETED),

                jobRepository.countByStatus(JobStatus.DLQ)

        );
    }
}