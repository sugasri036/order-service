package com.internship.orderservice.controller;

import com.internship.orderservice.entity.Order;
import com.internship.orderservice.service.OrderService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;


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

            @RequestHeader(
                    value = "X-User-Id",
                    required = false
            )
            String authenticatedUserId,

            @RequestBody Order order

    ) {

        try {

            if (authenticatedUserId == null ||
                    authenticatedUserId.isBlank()) {

                return ResponseEntity
                        .status(
                                HttpStatus.UNAUTHORIZED
                        )
                        .body(
                                "Authenticated user ID is required"
                        );
            }


            /*
             * NEVER trust userId supplied by the frontend.
             *
             * The Gateway created X-User-Id from
             * the verified JWT.
             */

            order.setUserId(
                    authenticatedUserId
            );


            return ResponseEntity.ok(
                    orderService.createOrder(
                            order
                    )
            );


        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            e.getMessage()
                    );


        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .body(
                            "Unable to create order"
                    );
        }
    }


    // =====================================================
    // GET ALL ORDERS
    // =====================================================

    @GetMapping
    public ResponseEntity<List<Order>>
    getAllOrders() {

        /*
         * This endpoint should eventually become
         * ADMIN ONLY.
         *
         * For now it remains available to authenticated
         * requests through the Gateway.
         */

        return ResponseEntity.ok(
                orderService.getAllOrders()
        );
    }


    // =====================================================
    // GET ORDER BY ID
    // =====================================================

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(

            @PathVariable String orderId

    ) {

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
    public ResponseEntity<?> getOrdersByUser(

            @PathVariable String userId,

            @RequestHeader(
                    value = "X-User-Id",
                    required = false
            )
            String authenticatedUserId

    ) {

        if (authenticatedUserId == null ||
                authenticatedUserId.isBlank()) {

            return ResponseEntity
                    .status(
                            HttpStatus.UNAUTHORIZED
                    )
                    .body(
                            "Authenticated user ID is required"
                    );
        }


        /*
         * Prevent:
         *
         * User A
         *    ↓
         * /user/User-B
         *
         * from reading User B's orders.
         */

        if (!authenticatedUserId.equals(userId)) {

            return ResponseEntity
                    .status(
                            HttpStatus.FORBIDDEN
                    )
                    .body(
                            "You are not allowed to access another user's orders"
                    );
        }


        return ResponseEntity.ok(
                orderService.getOrdersByUser(
                        authenticatedUserId
                )
        );
    }


    // =====================================================
    // MARK ORDER AS PAID
    // =====================================================

    @PutMapping("/{orderId}/payment")
    public ResponseEntity<?> markOrderAsPaid(

            @PathVariable String orderId,

            @RequestParam String paymentId

    ) {

        /*
         * This endpoint is called by Payment Service.
         *
         * It is intentionally not dependent on X-User-Id
         * because it is a service-to-service callback.
         *
         * Later we will protect this with an internal
         * service secret when we move completely to
         * Kubernetes.
         */

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
                    .body(
                            e.getMessage()
                    );
        }
    }


    // =====================================================
    // MARK PAYMENT FAILED
    // =====================================================

    @PutMapping("/{orderId}/payment-failed")
    public ResponseEntity<?> markOrderAsPaymentFailed(

            @PathVariable String orderId

    ) {

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
                    .body(
                            e.getMessage()
                    );
        }
    }
}