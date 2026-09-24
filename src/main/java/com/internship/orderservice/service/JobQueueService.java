package com.internship.orderservice.service;

import com.internship.orderservice.entity.JobQueue;
import com.internship.orderservice.repository.JobQueueRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public JobQueue createJob(
            String orderId) {

        if (orderId == null ||
                orderId.isBlank()) {

            throw new IllegalArgumentException(
                    "Order ID is required"
            );
        }


        // -------------------------------------------------
        // EXISTING JOB
        // -------------------------------------------------

        return jobQueueRepository
                .findByOrderId(orderId)
                .orElseGet(() -> {

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

                    job.setProcessingAt(
                            null
                    );

                    job.setProcessedAt(
                            null
                    );

                    job.setErrorMessage(
                            null
                    );


                    try {

                        return jobQueueRepository.saveAndFlush(
                                job
                        );

                    } catch (
                            DataIntegrityViolationException e
                    ) {

                        /*
                         * Another pod may have created the
                         * same order's job simultaneously.
                         *
                         * The database unique constraint
                         * protects the data.
                         *
                         * The current transaction may now be
                         * rollback-only, so don't query using
                         * this same transaction.
                         */

                        return findExistingJobAfterConflict(
                                orderId
                        );
                    }
                });
    }


    // =====================================================
    // FIND EXISTING JOB AFTER RACE
    // =====================================================

    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    protected JobQueue findExistingJobAfterConflict(
            String orderId) {

        return jobQueueRepository
                .findByOrderId(orderId)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Job creation conflict for order: "
                                                + orderId
                                )
                );
    }


    // =====================================================
    // GET PENDING JOBS
    // =====================================================

    public List<JobQueue> getPendingJobs() {

        return jobQueueRepository
                .findByStatus("PENDING");
    }
}