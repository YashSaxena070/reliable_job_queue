package com.app.reliable_job_queue.enums;

public enum JobStatus {
    PENDING,
    PROCESSING,
    FAILED,
    DLQ,
    COMPLETED
}
