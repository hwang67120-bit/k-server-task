package com.example.kservertask.point.payment;

public record PaymentVerificationResponse(
        String paymentId,
        long amount,
        boolean completed
) {
}
