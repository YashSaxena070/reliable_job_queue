package com.app.reliable_job_queue.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QueueStatsResponse {

    private long pending;
    private long processing;
    private long completed;
    private long dlq;

}
