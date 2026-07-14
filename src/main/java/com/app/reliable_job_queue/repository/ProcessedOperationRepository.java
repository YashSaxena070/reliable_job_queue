package com.app.reliable_job_queue.repository;

import com.app.reliable_job_queue.model.ProcessedOperation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedOperationRepository extends JpaRepository<ProcessedOperation, String> {

}
