package com.internship.orderservice.repository;

import com.internship.orderservice.entity.Order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository
        extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderId(
            String orderId
    );

    Optional<Order> findByIdempotencyKey(
            String idempotencyKey
    );

    List<Order> findByUserId(
            String userId
    );

    List<Order> findByStatus(
            String status
    );
}