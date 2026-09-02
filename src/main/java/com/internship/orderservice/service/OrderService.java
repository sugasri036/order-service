package com.internship.orderservice.service;

import com.internship.orderservice.entity.Order;
import com.internship.orderservice.repository.OrderRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    private final RestTemplate restTemplate;

    private final JobQueueService jobQueueService;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public OrderService(
            OrderRepository orderRepository,
            RestTemplate restTemplate,
            JobQueueService jobQueueService) {

        this.orderRepository =
                orderRepository;

        this.restTemplate =
                restTemplate;

        this.jobQueueService =
                jobQueueService;
    }


    // =====================================================
    // CREATE ORDER
    // =====================================================

    public Order createOrder(
            Order order) {

        // -------------------------------------------------
        // VALIDATE USER
        // -------------------------------------------------

        if (order.getUserId() == null ||
                order.getUserId().isBlank()) {

            throw new IllegalArgumentException(
                    "User ID is required"
            );
        }


        // -------------------------------------------------
        // VALIDATE FUND
        // -------------------------------------------------

        if (order.getFundId() == null ||
                order.getFundId().isBlank()) {

            throw new IllegalArgumentException(
                    "Fund ID is required"
            );
        }


        // -------------------------------------------------
        // VALIDATE AMOUNT
        // -------------------------------------------------

        if (order.getAmount() == null ||
                order.getAmount() <= 0) {

            throw new IllegalArgumentException(
                    "Amount must be greater than 0"
            );
        }


        // -------------------------------------------------
        // IDEMPOTENCY
        // -------------------------------------------------

        if (order.getIdempotencyKey() != null &&
                !order.getIdempotencyKey().isBlank()) {

            return orderRepository
                    .findByIdempotencyKey(
                            order.getIdempotencyKey()
                    )
                    .orElseGet(
                            () -> createNewOrder(
                                    order
                            )
                    );
        }


        return createNewOrder(order);
    }


    // =====================================================
    // CREATE NEW ORDER
    // =====================================================

    private Order createNewOrder(
            Order order) {

        // -------------------------------------------------
        // GENERATE INTERNAL ORDER ID
        // -------------------------------------------------

        order.setOrderId(
                "ORD-" + UUID.randomUUID()
        );


        // -------------------------------------------------
        // INITIAL STATUS
        // -------------------------------------------------

        order.setStatus(
                "PENDING"
        );


        // -------------------------------------------------
        // CREATED TIME
        // -------------------------------------------------

        order.setCreatedAt(
                LocalDateTime.now()
        );


        // -------------------------------------------------
        // IDEMPOTENCY KEY
        // -------------------------------------------------

        if (order.getIdempotencyKey() == null ||
                order.getIdempotencyKey().isBlank()) {

            order.setIdempotencyKey(
                    UUID.randomUUID().toString()
            );
        }


        // =================================================
        // SAVE ORDER FIRST
        // =================================================

        Order savedOrder =
                orderRepository.save(
                        order
                );


        // =================================================
        // PREPARE PAYMENT REQUEST
        // =================================================

        Map<String, Object>
                paymentRequest =
                new HashMap<>();


        paymentRequest.put(
                "orderId",
                savedOrder.getOrderId()
        );


        paymentRequest.put(
                "userId",
                savedOrder.getUserId()
        );


        paymentRequest.put(
                "amount",
                savedOrder.getAmount()
        );


        paymentRequest.put(
                "idempotencyKey",
                savedOrder.getIdempotencyKey()
        );


        // =================================================
        // CALL PAYMENT SERVICE
        // =================================================

        try {

            System.out.println();
            System.out.println(
                    "===================================="
            );

            System.out.println(
                    "CALLING PAYMENT SERVICE"
            );

            System.out.println(
                    "Internal Order ID: "
                            + savedOrder.getOrderId()
            );

            System.out.println(
                    "Fund: "
                            + savedOrder.getFundName()
            );

            System.out.println(
                    "Amount: ₹"
                            + savedOrder.getAmount()
            );

            System.out.println(
                    "===================================="
            );


            Map<String, Object>
                    paymentResponse =
                    restTemplate.postForObject(
                            "http://localhost:8080/api/payments",
                            paymentRequest,
                            Map.class
                    );


            // =================================================
            // SAVE PAYMENT INFORMATION
            // =================================================

            if (paymentResponse != null) {

                Object paymentId =
                        paymentResponse.get(
                                "paymentId"
                        );


                Object razorpayOrderId =
                        paymentResponse.get(
                                "razorpayOrderId"
                        );


                if (paymentId != null) {

                    savedOrder.setPaymentId(
                            paymentId.toString()
                    );
                }


                if (razorpayOrderId != null) {

                    savedOrder.setRazorpayOrderId(
                            razorpayOrderId.toString()
                    );
                }
            }


            // -------------------------------------------------
            // PAYMENT ORDER CREATED
            // -------------------------------------------------

            savedOrder.setStatus(
                    "PAYMENT_CREATED"
            );


            return orderRepository.save(
                    savedOrder
            );


        } catch (Exception e) {

            // =================================================
            // PAYMENT CREATION FAILED
            // =================================================

            System.out.println();
            System.out.println(
                    "===================================="
            );

            System.out.println(
                    "PAYMENT CREATION FAILED"
            );

            System.out.println(
                    "Order ID: "
                            + savedOrder.getOrderId()
            );

            System.out.println(
                    "Reason: "
                            + e.getMessage()
            );

            System.out.println(
                    "MARKING ORDER AS PAYMENT_FAILED"
            );

            System.out.println(
                    "===================================="
            );


            savedOrder.setStatus(
                    "PAYMENT_FAILED"
            );


            orderRepository.save(
                    savedOrder
            );


            throw new RuntimeException(
                    "Order created but payment order could not be created: "
                            + e.getMessage(),
                    e
            );
        }
    }


    // =====================================================
    // MARK ORDER AS PAID
    // =====================================================

    public Order markOrderAsPaid(
            String orderId,
            String paymentId) {

        Order order =
                orderRepository
                        .findByOrderId(
                                orderId
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Order not found: "
                                                + orderId
                                )
                        );


        // =================================================
        // IMPORTANT:
        // DO NOT DOWNGRADE COMPLETED ORDER
        // =================================================

        if ("COMPLETED".equalsIgnoreCase(
                order.getStatus()
        )) {

            System.out.println();
            System.out.println(
                    "===================================="
            );

            System.out.println(
                    "ORDER ALREADY COMPLETED"
            );

            System.out.println(
                    "Order ID: "
                            + order.getOrderId()
            );

            System.out.println(
                    "Existing Completed At: "
                            + order.getCompletedAt()
            );

            System.out.println(
                    "Keeping status as COMPLETED"
            );

            System.out.println(
                    "===================================="
            );


            // -------------------------------------------------
            // Update payment ID only if needed
            // -------------------------------------------------

            if (paymentId != null &&
                    !paymentId.isBlank()) {

                order.setPaymentId(
                        paymentId
                );
            }


            return orderRepository.save(
                    order
            );
        }


        // =================================================
        // UPDATE ORDER STATUS
        // =================================================

        order.setStatus(
                "PAID"
        );


        order.setPaymentId(
                paymentId
        );


        order.setPaidAt(
                LocalDateTime.now()
        );


        // -------------------------------------------------
        // SAVE ORDER
        // -------------------------------------------------

        Order savedOrder =
                orderRepository.save(
                        order
                );


        // =================================================
        // ADD JOB TO QUEUE
        // =================================================

        jobQueueService.createJob(
                savedOrder.getOrderId()
        );


        // =================================================
        // LOG
        // =================================================

        System.out.println();
        System.out.println(
                "===================================="
        );

        System.out.println(
                "ORDER UPDATED TO PAID"
        );

        System.out.println(
                "Order ID: "
                        + savedOrder.getOrderId()
        );

        System.out.println(
                "Payment ID: "
                        + savedOrder.getPaymentId()
        );

        System.out.println(
                "Status: "
                        + savedOrder.getStatus()
        );

        System.out.println(
                "Paid At: "
                        + savedOrder.getPaidAt()
        );

        System.out.println(
                "JOB ADDED TO QUEUE"
        );

        System.out.println(
                "===================================="
        );


        return savedOrder;
    }


    // =====================================================
    // MARK ORDER AS PAYMENT FAILED
    // =====================================================

    public Order markOrderAsPaymentFailed(
            String orderId) {

        Order order =
                orderRepository
                        .findByOrderId(
                                orderId
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Order not found: "
                                                + orderId
                                )
                        );


        // -------------------------------------------------
        // DO NOT CHANGE COMPLETED ORDERS
        // -------------------------------------------------

        if ("COMPLETED".equalsIgnoreCase(
                order.getStatus()
        )) {

            System.out.println(
                    "Order already COMPLETED. "
                            + "Skipping PAYMENT_FAILED update."
            );

            return order;
        }


        // -------------------------------------------------
        // UPDATE STATUS
        // -------------------------------------------------

        order.setStatus(
                "PAYMENT_FAILED"
        );


        Order savedOrder =
                orderRepository.save(
                        order
                );


        // -------------------------------------------------
        // LOG
        // -------------------------------------------------

        System.out.println();
        System.out.println(
                "===================================="
        );

        System.out.println(
                "ORDER PAYMENT FAILED"
        );

        System.out.println(
                "Order ID: "
                        + savedOrder.getOrderId()
        );

        System.out.println(
                "Status: "
                        + savedOrder.getStatus()
        );

        System.out.println(
                "===================================="
        );


        return savedOrder;
    }


    // =====================================================
    // GET ORDER
    // =====================================================

    public Order getOrderByOrderId(
            String orderId) {

        return orderRepository
                .findByOrderId(
                        orderId
                )
                .orElseThrow(
                        () -> new RuntimeException(
                                "Order not found: "
                                        + orderId
                        )
                );
    }


    // =====================================================
    // GET ALL ORDERS
    // =====================================================

    public List<Order>
    getAllOrders() {

        return orderRepository.findAll();
    }


    // =====================================================
    // GET USER ORDERS
    // =====================================================

    public List<Order>
    getOrdersByUser(
            String userId) {

        return orderRepository
                .findByUserId(
                        userId
                );
    }
}