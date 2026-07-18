package com.example.kservertask.point.payment;

public interface MockPaymentClient {
    PaymentVerificationResponse verifyPayment(String paymentId);
}
