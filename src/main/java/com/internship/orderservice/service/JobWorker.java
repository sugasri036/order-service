package com.internship.orderservice.service;

import com.internship.orderservice.dto.FundResponse;
import com.internship.orderservice.entity.JobQueue;
import com.internship.orderservice.entity.Order;
import com.internship.orderservice.repository.JobQueueRepository;
import com.internship.orderservice.repository.OrderRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class JobWorker {

    private final JobQueueRepository jobQueueRepository;

    private final OrderRepository orderRepository;

    private final RestTemplate restTemplate;

    @Value("${fund.service.url}")
    private String fundServiceUrl;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public JobWorker(
            JobQueueRepository jobQueueRepository,
            OrderRepository orderRepository,
            RestTemplate restTemplate
    ) {

        this.jobQueueRepository =
                jobQueueRepository;

        this.orderRepository =
                orderRepository;

        this.restTemplate =
                restTemplate;
    }


    // =====================================================
    // BACKGROUND WORKER
    // =====================================================

    @Scheduled(
            fixedDelayString =
                    "${worker.fixed-delay:5000}"
    )
    public void processJobs() {

        // -------------------------------------------------
        // RESET STALE JOBS
        // -------------------------------------------------

        resetStaleJobs();


        // -------------------------------------------------
        // FIND PENDING JOBS
        // -------------------------------------------------

        List<JobQueue> jobs =
                jobQueueRepository
                        .findByStatus("PENDING");


        if (jobs.isEmpty()) {

            return;
        }


        System.out.println();

        System.out.println(
                "===================================="
        );

        System.out.println(
                "BACKGROUND WORKER"
        );

        System.out.println(
                "Pending jobs found: "
                        + jobs.size()
        );

        System.out.println(
                "===================================="
        );


        // -------------------------------------------------
        // TRY TO CLAIM EACH JOB
        // -------------------------------------------------

        for (JobQueue job : jobs) {

            boolean claimed =
                    claimJob(job);


            /*
             * If another pod already claimed the job,
             * updatedRows will be 0.
             *
             * We simply skip the job.
             */

            if (!claimed) {

                continue;
            }


            processClaimedJob(job);
        }
    }


    // =====================================================
    // CLAIM JOB
    // =====================================================

    private boolean claimJob(
            JobQueue job) {

        LocalDateTime processingTime =
                LocalDateTime.now();


        int updatedRows =
                jobQueueRepository.claimJob(
                        job.getId(),
                        processingTime
                );


        /*
         * updatedRows == 1
         *
         * This pod successfully performed:
         *
         * PENDING → PROCESSING
         *
         *
         * updatedRows == 0
         *
         * Another pod already claimed it.
         */

        return updatedRows == 1;
    }


    // =====================================================
    // PROCESS CLAIMED JOB
    // =====================================================

    private void processClaimedJob(
            JobQueue job) {

        try {

            System.out.println();

            System.out.println(
                    "Processing Job: "
                            + job.getJobId()
            );

            System.out.println(
                    "Order ID: "
                            + job.getOrderId()
            );


            // -------------------------------------------------
            // FIND ORDER
            // -------------------------------------------------

            Order order =
                    orderRepository
                            .findByOrderId(
                                    job.getOrderId()
                            )
                            .orElseThrow(
                                    () ->
                                            new RuntimeException(
                                                    "Order not found: "
                                                            + job.getOrderId()
                                            )
                            );


            // -------------------------------------------------
            // IDEMPOTENCY CHECK
            // -------------------------------------------------

            /*
             * If the order was already completed,
             * don't process the investment again.
             */

            if ("COMPLETED".equalsIgnoreCase(
                    order.getStatus()
            )) {

                markJobCompleted(job);

                System.out.println(
                        "Order already COMPLETED. "
                                + "Job marked completed."
                );

                return;
            }


            // -------------------------------------------------
            // CHECK PAYMENT
            // -------------------------------------------------

            if (!"PAID".equalsIgnoreCase(
                    order.getStatus()
            )) {

                throw new RuntimeException(
                        "Order is not PAID"
                );
            }


            // =================================================
            // CALL FUND SERVICE
            // =================================================

            String fundUrl =
                    fundServiceUrl
                            + "/api/funds/"
                            + order.getFundId();


            System.out.println();

            System.out.println(
                    "Calling Fund Service..."
            );

            System.out.println(
                    "Fund Service URL: "
                            + fundServiceUrl
            );

            System.out.println(
                    "Fund ID: "
                            + order.getFundId()
            );


            FundResponse fund =
                    restTemplate.getForObject(
                            fundUrl,
                            FundResponse.class
                    );


            // -------------------------------------------------
            // VALIDATE FUND RESPONSE
            // -------------------------------------------------

            if (fund == null) {

                throw new RuntimeException(
                        "Fund Service returned empty response"
                );
            }


            if (fund.getNav() == null ||
                    fund.getNav() <= 0) {

                throw new RuntimeException(
                        "Invalid NAV received from Fund Service"
                );
            }


            // =================================================
            // GET NAV
            // =================================================

            Double nav =
                    fund.getNav();


            System.out.println();

            System.out.println(
                    "Fund Name: "
                            + fund.getName()
            );

            System.out.println(
                    "NAV: "
                            + nav
            );


            // =================================================
            // CALCULATE UNITS
            // =================================================

            if (order.getAmount() == null ||
                    order.getAmount() <= 0) {

                throw new RuntimeException(
                        "Invalid investment amount"
                );
            }


            Double units =
                    order.getAmount() / nav;


            if (units <= 0 ||
                    units.isInfinite() ||
                    units.isNaN()) {

                throw new RuntimeException(
                        "Invalid units calculated"
                );
            }


            System.out.println();

            System.out.println(
                    "Investment Amount: ₹"
                            + order.getAmount()
            );

            System.out.println(
                    "NAV: ₹"
                            + nav
            );

            System.out.println(
                    "Units: "
                            + units
            );


            // =================================================
            // SAVE FUND INFORMATION
            // =================================================

            order.setFundName(
                    fund.getName()
            );

            order.setNav(
                    nav
            );

            order.setUnits(
                    units
            );


            // =================================================
            // SET COMPLETION TIME
            // =================================================

            order.setCompletedAt(
                    LocalDateTime.now()
            );


            // =================================================
            // MARK ORDER COMPLETED
            // =================================================

            order.setStatus(
                    "COMPLETED"
            );


            // =================================================
            // SAVE ORDER
            // =================================================

            orderRepository.saveAndFlush(
                    order
            );


            // =================================================
            // MARK JOB COMPLETED
            // =================================================

            markJobCompleted(job);


            // =================================================
            // SUCCESS LOG
            // =================================================

            System.out.println();

            System.out.println(
                    "===================================="
            );

            System.out.println(
                    "JOB COMPLETED"
            );

            System.out.println(
                    "Order ID: "
                            + order.getOrderId()
            );

            System.out.println(
                    "Fund: "
                            + order.getFundName()
            );

            System.out.println(
                    "NAV: "
                            + order.getNav()
            );

            System.out.println(
                    "Units: "
                            + order.getUnits()
            );

            System.out.println(
                    "Completed At: "
                            + order.getCompletedAt()
            );

            System.out.println(
                    "Order Status: "
                            + order.getStatus()
            );

            System.out.println(
                    "Job Status: COMPLETED"
            );

            System.out.println(
                    "===================================="
            );


        } catch (Exception e) {

            handleJobFailure(
                    job,
                    e
            );
        }
    }


    // =====================================================
    // MARK JOB COMPLETED
    // =====================================================

    private void markJobCompleted(
            JobQueue job) {

        job.setStatus(
                "COMPLETED"
        );

        job.setProcessedAt(
                LocalDateTime.now()
        );

        job.setProcessingAt(
                null
        );

        job.setErrorMessage(
                null
        );


        jobQueueRepository.save(
                job
        );
    }


    // =====================================================
    // HANDLE JOB FAILURE
    // =====================================================

    private void handleJobFailure(
            JobQueue job,
            Exception exception) {

        int attempts =
                job.getAttempts() == null
                        ? 1
                        : job.getAttempts();


        /*
         * Maximum 5 attempts.
         */

        if (attempts < 5) {

            job.setStatus(
                    "PENDING"
            );

            job.setProcessingAt(
                    null
            );

            job.setErrorMessage(
                    exception.getMessage()
            );


            jobQueueRepository.save(
                    job
            );


            System.out.println();

            System.out.println(
                    "JOB FAILED - WILL RETRY"
            );

            System.out.println(
                    "Job ID: "
                            + job.getJobId()
            );

            System.out.println(
                    "Attempt: "
                            + attempts
            );

            System.out.println(
                    "Error: "
                            + exception.getMessage()
            );


            return;
        }


        // -------------------------------------------------
        // PERMANENT FAILURE
        // -------------------------------------------------

        job.setStatus(
                "FAILED"
        );

        job.setProcessingAt(
                null
        );

        job.setErrorMessage(
                exception.getMessage()
        );


        jobQueueRepository.save(
                job
        );


        System.out.println();

        System.out.println(
                "===================================="
        );

        System.out.println(
                "JOB PERMANENTLY FAILED"
        );

        System.out.println(
                "Job ID: "
                        + job.getJobId()
        );

        System.out.println(
                "Order ID: "
                        + job.getOrderId()
        );

        System.out.println(
                "Attempts: "
                        + attempts
        );

        System.out.println(
                "Error: "
                        + exception.getMessage()
        );

        System.out.println(
                "===================================="
        );
    }


    // =====================================================
    // RESET STALE JOBS
    // =====================================================

    private void resetStaleJobs() {

        /*
         * If a Kubernetes pod crashes after:
         *
         * PENDING → PROCESSING
         *
         * the job could remain PROCESSING forever.
         *
         * Jobs processing for more than 10 minutes
         * are returned to PENDING.
         */

        LocalDateTime cutoff =
                LocalDateTime.now()
                        .minusMinutes(10);


        int resetCount =
                jobQueueRepository.resetStaleJobs(
                        cutoff
                );


        if (resetCount > 0) {

            System.out.println(
                    "Reset stale jobs: "
                            + resetCount
            );
        }
    }
}