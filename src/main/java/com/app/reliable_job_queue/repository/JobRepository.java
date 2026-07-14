package com.app.reliable_job_queue.repository;

import com.app.reliable_job_queue.enums.JobStatus;
import com.app.reliable_job_queue.model.Job;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job,Long> {

    long countByStatus(JobStatus status);

    // Find jobs that are either PENDING or had a worker crash (lockedUntil has expired)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT j\n" +
            "FROM Job j\n" +
            "WHERE\n" +
            "j.queueName = :queueName\n" +
            "AND\n" +
            "(\n" +
            "    j.status='PENDING'\n" +
            "    OR\n" +
            "    (\n" +
            "        j.status='PROCESSING'\n" +
            "        AND j.lockedUntil < :now\n" +
            "    )\n" +
            ")\n" +
            "AND j.runAt <= :now\n" +
            "ORDER BY\n" +
            "j.priority DESC,\n" +
            "j.createdAt ASC")
    List<Job> findNextAvailableJob(String queueName, @Param("now") LocalDateTime now, Pageable pageable);

    @Modifying
    @Query("""
       DELETE FROM Job j
       WHERE j.status = 'COMPLETED'
       AND j.completedAt < :cutoff
       """)
    void deleteCompletedJobsOlderThan(@Param("cutoff") LocalDateTime cutoff);

}
