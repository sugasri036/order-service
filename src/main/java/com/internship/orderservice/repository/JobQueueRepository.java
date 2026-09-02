package com.internship.orderservice.repository;

import com.internship.orderservice.entity.JobQueue;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobQueueRepository
        extends JpaRepository<JobQueue, Long> {

    Optional<JobQueue> findByJobId(
            String jobId
    );


    Optional<JobQueue> findByOrderId(
            String orderId
    );


    List<JobQueue> findByStatus(
            String status
    );
}