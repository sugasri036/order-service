package com.internship.orderservice.repository;

import com.internship.orderservice.entity.JobQueue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JobQueueRepository extends JpaRepository<JobQueue, Long> {

    Optional<JobQueue> findByJobId(String jobId);

    Optional<JobQueue> findByOrderId(String orderId);

    List<JobQueue> findByStatus(String status);

    /*
     * Atomically claim a PENDING job.
     *
     * If multiple Order Service pods see the same job,
     * only one pod can change PENDING -> PROCESSING.
     */
    @Transactional
    @Modifying
    @Query("""
        UPDATE JobQueue j
        SET j.status = 'PROCESSING',
            j.processingAt = :processingAt,
            j.attempts = COALESCE(j.attempts, 0) + 1
        WHERE j.id = :jobId
          AND j.status = 'PENDING'
    """)
    int claimJob(
            @Param("jobId") Long jobId,
            @Param("processingAt") LocalDateTime processingAt
    );

    /*
     * Recover jobs that were stuck in PROCESSING.
     *
     * Example:
     * Pod crashes while processing a job.
     * After the timeout, another pod can retry it.
     */
    @Transactional
    @Modifying
    @Query("""
        UPDATE JobQueue j
        SET j.status = 'PENDING',
            j.processingAt = null
        WHERE j.status = 'PROCESSING'
          AND j.processingAt < :cutoff
    """)
    int resetStaleJobs(
            @Param("cutoff") LocalDateTime cutoff
    );
}