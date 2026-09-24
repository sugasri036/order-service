package com.internship.orderservice.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "job_queue",

        uniqueConstraints = {

                @UniqueConstraint(
                        name = "uk_job_order",
                        columnNames = "order_id"
                )
        },

        indexes = {

                @Index(
                        name = "idx_job_status",
                        columnList = "status"
                ),

                @Index(
                        name = "idx_job_status_created",
                        columnList = "status,created_at"
                )
        }
)
public class JobQueue {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;


    private String jobId;


    private String orderId;


    private String status;


    private LocalDateTime createdAt;


    private LocalDateTime processedAt;


    /*
     * When a worker claims the job.
     *
     * This lets us detect jobs that were stuck in
     * PROCESSING because a pod crashed.
     */

    private LocalDateTime processingAt;


    private Integer attempts;


    @Column(length = 1000)
    private String errorMessage;


    // =====================================================
    // ID
    // =====================================================

    public Long getId() {
        return id;
    }


    // =====================================================
    // JOB ID
    // =====================================================

    public String getJobId() {

        return jobId;
    }

    public void setJobId(
            String jobId) {

        this.jobId =
                jobId;
    }


    // =====================================================
    // ORDER ID
    // =====================================================

    public String getOrderId() {

        return orderId;
    }

    public void setOrderId(
            String orderId) {

        this.orderId =
                orderId;
    }


    // =====================================================
    // STATUS
    // =====================================================

    public String getStatus() {

        return status;
    }

    public void setStatus(
            String status) {

        this.status =
                status;
    }


    // =====================================================
    // CREATED AT
    // =====================================================

    public LocalDateTime getCreatedAt() {

        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {

        this.createdAt =
                createdAt;
    }


    // =====================================================
    // PROCESSED AT
    // =====================================================

    public LocalDateTime getProcessedAt() {

        return processedAt;
    }

    public void setProcessedAt(
            LocalDateTime processedAt) {

        this.processedAt =
                processedAt;
    }


    // =====================================================
    // PROCESSING AT
    // =====================================================

    public LocalDateTime getProcessingAt() {

        return processingAt;
    }

    public void setProcessingAt(
            LocalDateTime processingAt) {

        this.processingAt =
                processingAt;
    }


    // =====================================================
    // ATTEMPTS
    // =====================================================

    public Integer getAttempts() {

        return attempts;
    }

    public void setAttempts(
            Integer attempts) {

        this.attempts =
                attempts;
    }


    // =====================================================
    // ERROR MESSAGE
    // =====================================================

    public String getErrorMessage() {

        return errorMessage;
    }

    public void setErrorMessage(
            String errorMessage) {

        this.errorMessage =
                errorMessage;
    }
}