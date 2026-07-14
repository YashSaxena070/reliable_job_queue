package com.app.reliable_job_queue.model;


import com.app.reliable_job_queue.enums.JobStatus;
import com.app.reliable_job_queue.enums.RetryPolicy;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="Jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String payload;

    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    private int priority;
    private int attempts;

    private boolean isExtendLease;

    @Enumerated(EnumType.STRING)
    private RetryPolicy retryPolicy;

    private String queueName;

    private int maxAttempts;

    private LocalDateTime runAt;
    private LocalDateTime lockedUntil;
    private LocalDateTime createdAt;


    @Version
    private Long version;
    private LocalDateTime startedAt;

    private Integer timeoutSeconds;


    private LocalDateTime completedAt;
    private String lastError;
    private LocalDateTime lastFailedAt;
}
