package com.app.reliable_job_queue.Controller;

import com.app.reliable_job_queue.enums.RetryPolicy;
import com.app.reliable_job_queue.model.Job;

import com.app.reliable_job_queue.repository.JobRepository;
import com.app.reliable_job_queue.service.QueueService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/queue")
public class QueueController {

    private final QueueService queueService;
    private final JobRepository jobRepository;

    public QueueController(QueueService queueService, JobRepository jobRepository) {
        this.queueService = queueService;
        this.jobRepository = jobRepository;
    }

    @PostMapping("/submit")
    public Job addJob(@RequestParam String payload,
                      @RequestParam(defaultValue = "1") int priority,
                      @RequestParam(defaultValue = "3") int maxAttempts,
                      @RequestParam(defaultValue = "0") int delaySeconds,
                      @RequestParam(defaultValue = "EXPONENTIAL") RetryPolicy retryPolicy) {
        return queueService.submitJob(payload, "EMAIL",priority, maxAttempts, delaySeconds, retryPolicy);
    }

    @GetMapping("/inspect")
    public List inspectAllJobs() {
        return jobRepository.findAll();
    }
}
