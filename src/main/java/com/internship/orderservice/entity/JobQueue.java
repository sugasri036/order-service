package com.internship.orderservice.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "job_queue",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_job_order",
                        columnNames = "orderId"
                )
        }
)
public class JobQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =====================================================
    // JOB ID
    // =====================================================

    private String jobId;


    // =====================================================
    // ORDER ID
    // =====================================================

    private String orderId;


    // =====================================================
    // JOB STATUS
    // =====================================================

    private String status;


    // =====================================================
    // CREATED TIME
    // =====================================================

    private LocalDateTime createdAt;


    // =====================================================
    // PROCESSED TIME
    // =====================================================

    private LocalDateTime processedAt;


    // =====================================================
    // NUMBER OF ATTEMPTS
    // =====================================================

    private Integer attempts;


    // =====================================================
    // ERROR MESSAGE
    // =====================================================

    @Column(length = 1000)
    private String errorMessage;


    // =====================================================
    // GETTERS / SETTERS
    // =====================================================

    public Long getId() {
        return id;
    }


    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }


    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }


    public Integer getAttempts() {
        return attempts;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }


    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}