package com.example.kservertask.point.payment;

import org.springframework.stereotype.Component;

@Component
public class MockPaymentClientStub implements MockPaymentClient {

    @Override
    public PaymentVerificationResponse verifyPayment(String paymentId) {
        return new PaymentVerificationResponse(paymentId, 1_000L, true);
    }
}
