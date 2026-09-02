package com.internship.orderservice.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String orderId;

    private String userId;

    private String fundId;

    private String fundName;

    private Double amount;

    private Double nav;

    private Double units;

    private String paymentId;

    private String razorpayOrderId;

    private String idempotencyKey;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime paidAt;

    private LocalDateTime completedAt;


    // =====================================================
    // ID
    // =====================================================

    public Long getId() {
        return id;
    }


    // =====================================================
    // ORDER ID
    // =====================================================

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }


    // =====================================================
    // USER ID
    // =====================================================

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    // =====================================================
    // FUND ID
    // =====================================================

    public String getFundId() {
        return fundId;
    }

    public void setFundId(String fundId) {
        this.fundId = fundId;
    }


    // =====================================================
    // FUND NAME
    // =====================================================

    public String getFundName() {
        return fundName;
    }

    public void setFundName(String fundName) {
        this.fundName = fundName;
    }


    // =====================================================
    // AMOUNT
    // =====================================================

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }


    // =====================================================
    // NAV
    // =====================================================

    public Double getNav() {
        return nav;
    }

    public void setNav(Double nav) {
        this.nav = nav;
    }


    // =====================================================
    // UNITS
    // =====================================================

    public Double getUnits() {
        return units;
    }

    public void setUnits(Double units) {
        this.units = units;
    }


    // =====================================================
    // PAYMENT ID
    // =====================================================

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }


    // =====================================================
    // RAZORPAY ORDER ID
    // =====================================================

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public void setRazorpayOrderId(
            String razorpayOrderId) {

        this.razorpayOrderId = razorpayOrderId;
    }


    // =====================================================
    // IDEMPOTENCY KEY
    // =====================================================

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(
            String idempotencyKey) {

        this.idempotencyKey = idempotencyKey;
    }


    // =====================================================
    // STATUS
    // =====================================================

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    // =====================================================
    // CREATED AT
    // =====================================================

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt) {

        this.createdAt = createdAt;
    }


    // =====================================================
    // PAID AT
    // =====================================================

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(
            LocalDateTime paidAt) {

        this.paidAt = paidAt;
    }


    // =====================================================
    // COMPLETED AT
    // =====================================================

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(
            LocalDateTime completedAt) {

        this.completedAt = completedAt;
    }
}