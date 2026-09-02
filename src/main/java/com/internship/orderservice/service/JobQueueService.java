package com.internship.orderservice.service;

import com.internship.orderservice.entity.JobQueue;
import com.internship.orderservice.repository.JobQueueRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class JobQueueService {

    private final JobQueueRepository jobQueueRepository;


    public JobQueueService(
            JobQueueRepository jobQueueRepository) {

        this.jobQueueRepository =
                jobQueueRepository;
    }


    // =====================================================
    // CREATE JOB
    // =====================================================

    public JobQueue createJob(
            String orderId) {

        // -------------------------------------------------
        // PREVENT DUPLICATE JOB
        // -------------------------------------------------

        if (jobQueueRepository
                .findByOrderId(orderId)
                .isPresent()) {

            return jobQueueRepository
                    .findByOrderId(orderId)
                    .get();
        }


        // -------------------------------------------------
        // CREATE JOB
        // -------------------------------------------------

        JobQueue job =
                new JobQueue();


        job.setJobId(
                "JOB-" + UUID.randomUUID()
        );


        job.setOrderId(
                orderId
        );


        job.setStatus(
                "PENDING"
        );


        job.setCreatedAt(
                LocalDateTime.now()
        );


        job.setAttempts(
                0
        );


        // -------------------------------------------------
        // SAVE
        // -------------------------------------------------

        JobQueue savedJob =
                jobQueueRepository.save(
                        job
                );


        System.out.println();
        System.out.println(
                "===================================="
        );

        System.out.println(
                "JOB ADDED TO QUEUE"
        );

        System.out.println(
                "Job ID: "
                        + savedJob.getJobId()
        );

        System.out.println(
                "Order ID: "
                        + savedJob.getOrderId()
        );

        System.out.println(
                "Status: "
                        + savedJob.getStatus()
        );

        System.out.println(
                "===================================="
        );


        return savedJob;
    }


    // =====================================================
    // GET PENDING JOBS
    // =====================================================

    public List<JobQueue> getPendingJobs() {

        return jobQueueRepository
                .findByStatus("PENDING");
    }
}