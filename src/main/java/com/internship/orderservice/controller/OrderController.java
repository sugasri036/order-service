package com.internship.orderservice.controller;

import com.internship.orderservice.entity.Order;
import com.internship.orderservice.service.OrderService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public OrderController(
            OrderService orderService) {

        this.orderService =
                orderService;
    }


    // =====================================================
    // CREATE ORDER
    // =====================================================

    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestBody Order order) {

        try {

            return ResponseEntity.ok(
                    orderService.createOrder(
                            order
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .body(e.getMessage());
        }
    }


    // =====================================================
    // GET ALL
    // =====================================================

    @GetMapping
    public ResponseEntity<List<Order>>
    getAllOrders() {

        return ResponseEntity.ok(
                orderService.getAllOrders()
        );
    }


    // =====================================================
    // GET BY ORDER ID
    // =====================================================

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(
            @PathVariable String orderId) {

        try {

            return ResponseEntity.ok(
                    orderService.getOrderByOrderId(
                            orderId
                    )
            );

        } catch (Exception e) {

            return ResponseEntity
                    .notFound()
                    .build();
        }
    }


    // =====================================================
    // GET USER ORDERS
    // =====================================================

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>>
    getOrdersByUser(
            @PathVariable String userId) {

        return ResponseEntity.ok(
                orderService.getOrdersByUser(
                        userId
                )
        );
    }


    // =====================================================
    // MARK ORDER AS PAID
    // =====================================================

    @PutMapping("/{orderId}/payment")
    public ResponseEntity<?> markOrderAsPaid(
            @PathVariable String orderId,
            @RequestParam String paymentId) {

        try {

            return ResponseEntity.ok(
                    orderService.markOrderAsPaid(
                            orderId,
                            paymentId
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }


    // =====================================================
    // MARK ORDER AS PAYMENT FAILED
    // =====================================================

    @PutMapping("/{orderId}/payment-failed")
    public ResponseEntity<?> markOrderAsPaymentFailed(
            @PathVariable String orderId) {

        try {

            return ResponseEntity.ok(
                    orderService.markOrderAsPaymentFailed(
                            orderId
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}