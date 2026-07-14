package com.app.reliable_job_queue.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="ProcessedOperation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedOperation {

    @Id
    private String idempotencyKey;

    private LocalDateTime processedAt;

}
