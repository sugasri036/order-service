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
            RestTemplate restTemplate) {

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

    @Scheduled(fixedDelay = 5000)
    public void processJobs() {

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


        for (JobQueue job : jobs) {

            processJob(job);
        }
    }


    // =====================================================
    // PROCESS SINGLE JOB
    // =====================================================

    private void processJob(
            JobQueue job) {

        try {

            // -------------------------------------------------
            // CHANGE JOB STATUS TO PROCESSING
            // -------------------------------------------------

            job.setStatus(
                    "PROCESSING"
            );

            job.setAttempts(
                    job.getAttempts() + 1
            );

            jobQueueRepository.save(job);


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
                                    () -> new RuntimeException(
                                            "Order not found: "
                                                    + job.getOrderId()
                                    )
                            );


            // -------------------------------------------------
            // CHECK PAYMENT
            // -------------------------------------------------

            if (!"PAID".equals(
                    order.getStatus())) {

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

            Double units =
                    order.getAmount() / nav;


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

            order.setNav(nav);

            order.setUnits(units);


            // =================================================
            // SET INVESTMENT COMPLETION TIME
            // =================================================

            LocalDateTime completedTime =
                    LocalDateTime.now();

            order.setCompletedAt(
                    completedTime
            );


            // =================================================
            // MARK ORDER AS COMPLETED
            // =================================================

            order.setStatus(
                    "COMPLETED"
            );


            // =================================================
            // SAVE COMPLETED ORDER
            // =================================================

            orderRepository.saveAndFlush(order);


            // =================================================
            // MARK JOB COMPLETED
            // =================================================

            job.setStatus(
                    "COMPLETED"
            );

            job.setProcessedAt(
                    LocalDateTime.now()
            );

            job.setErrorMessage(null);


            jobQueueRepository.save(job);


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
                    "Job Status: "
                            + job.getStatus()
            );

            System.out.println(
                    "===================================="
            );


        } catch (Exception e) {

            // =================================================
            // JOB FAILED
            // =================================================

            job.setStatus(
                    "FAILED"
            );

            job.setErrorMessage(
                    e.getMessage()
            );


            jobQueueRepository.save(job);


            System.out.println();

            System.out.println(
                    "===================================="
            );

            System.out.println(
                    "JOB FAILED"
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
                    "Error: "
                            + e.getMessage()
            );

            System.out.println(
                    "===================================="
            );
        }
    }
}