package com.example.kservertask.point.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;
import java.util.Optional;

import com.example.kservertask.error.BusinessException;
import com.example.kservertask.error.ErrorCode;
import com.example.kservertask.point.entity.PointAccount;
import com.example.kservertask.point.entity.PointCharge;
import com.example.kservertask.point.entity.PointChargeStatus;
import com.example.kservertask.point.entity.PointHistory;
import com.example.kservertask.point.payment.MockPaymentClient;
import com.example.kservertask.point.payment.PaymentVerificationResponse;
import com.example.kservertask.point.repository.PointAccountRepository;
import com.example.kservertask.point.repository.PointChargeRepository;
import com.example.kservertask.point.repository.PointHistoryRepository;
import com.example.kservertask.point.request.PointChargeRequest;
import com.example.kservertask.point.response.PointChargeResponse;
import com.example.kservertask.user.entity.AppUser;
import com.example.kservertask.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointChargeService {

    private final AppUserRepository appUserRepository;
    private final PointChargeRepository pointChargeRepository;
    private final PointAccountRepository pointAccountRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final MockPaymentClient mockPaymentClient;

    @Transactional
    public PointChargeResponse chargePoint(PointChargeRequest request) {
        validateRequest(request);

        AppUser user = appUserRepository.findById(request.userId())
                .filter(appUser -> Objects.equals(appUser.getPhoneNumber(), request.phoneNumber()))
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_INFORMATION_MISMATCH));

        PaymentVerificationResponse payment = mockPaymentClient.verifyPayment(request.paymentId());

        if (!payment.completed()) {
            throw new BusinessException(ErrorCode.PAYMENT_REJECTED);
        }

        if (!Objects.equals(payment.paymentId(), request.paymentId())
                || payment.amount() != request.amount()) {
            throw new BusinessException(ErrorCode.POINT_CHARGE_FAILED);
        }

        long paidAmount = payment.amount();
        String requestHash = createRequestHash(request);
        Optional<PointCharge> existingCharge = pointChargeRepository
                .findByPaymentIdForUpdate(request.paymentId());

        if (existingCharge.isPresent()) {
            PointCharge pointCharge = existingCharge.get();

            if (!Objects.equals(pointCharge.getUserId(), user.getUserId())
                    || pointCharge.getPaidAmount() != paidAmount) {
                throw new BusinessException(ErrorCode.POINT_CHARGE_FAILED);
            }

            if (pointCharge.getStatus() == PointChargeStatus.COMPLETED) {
                return new PointChargeResponse(user.getUserId());
            }

            return proceedCharge(user, pointCharge, paidAmount, requestHash);
        }

        PointCharge pointCharge = new PointCharge(
                user.getUserId(),
                request.paymentId(),
                paidAmount
        );

        try {
            pointChargeRepository.saveAndFlush(pointCharge);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.DUPLICATE_REQUEST);
        }

        return proceedCharge(user, pointCharge, paidAmount, requestHash);
    }

    private PointChargeResponse proceedCharge(
            AppUser user,
            PointCharge pointCharge,
            long paidAmount,
            String requestHash
    ) {
        PointAccount pointAccount = pointAccountRepository.findById(user.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.POINT_CHARGE_FAILED));

        pointCharge.start();
        pointAccount.addPoint(paidAmount);
        pointAccountRepository.flush();

        PointHistory history = new PointHistory(
                user.getUserId(),
                null,
                "CHARGE",
                paidAmount,
                pointAccount.getBalance(),
                pointAccount.getVersion(),
                pointCharge.getPaymentId(),
                requestHash
        );
        pointHistoryRepository.save(history);
        pointCharge.complete();

        return new PointChargeResponse(user.getUserId());
    }

    private void validateRequest(PointChargeRequest request) {
        if (request == null
                || request.userId() == null
                || request.userId() <= 0
                || request.phoneNumber() == null
                || request.phoneNumber().isBlank()
                || request.paymentId() == null
                || request.paymentId().isBlank()
                || request.amount() == null
                || request.amount() <= 0) {
            throw new BusinessException(ErrorCode.POINT_CHARGE_FAILED);
        }
    }

    private String createRequestHash(PointChargeRequest request) {
        String value = request.userId() + ":" + request.paymentId() + ":" + request.amount();

        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new BusinessException(ErrorCode.POINT_CHARGE_FAILED);
        }
    }
}
