package com.example.kservertask.point.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
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
import com.example.kservertask.point.response.PointHistoryResponse;
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

    private static final int MAX_PAYMENT_RETRY = 3;

    private final AppUserRepository appUserRepository;
    private final PointChargeRepository pointChargeRepository;
    private final PointAccountRepository pointAccountRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final MockPaymentClient mockPaymentClient;

    /**
     * 기능: 결제 완료 여부를 확인하고 포인트를 충전한다.
     *
     * 파라미터:
     * - request: 포인트 충전 요청 정보
     *
     * 요청값:
     * - userId: 사용자 ID
     * - phoneNumber: 사용자 전화번호
     * - paymentId: 결제 식별값
     * - amount: 충전 요청 금액
     *
     * 응답값:
     * - userId: 충전한 사용자 ID
     */
    @Transactional
    public PointChargeResponse chargePoint(PointChargeRequest request) {
        validateRequest(request);

        AppUser user = appUserRepository.findById(request.userId())
                .filter(appUser -> Objects.equals(appUser.getPhoneNumber(), request.phoneNumber()))
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_INFORMATION_MISMATCH));

        PaymentVerificationResponse payment = verifyPaymentWithRetry(request.paymentId());

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

    /**
     * 기능: 사용자의 포인트 충전 내역을 최신순으로 조회한다.
     *
     * 파라미터:
     * - userId: 조회할 사용자 ID
     *
     * 요청값:
     * - userId: 충전 내역을 조회할 사용자 식별값
     *
     * 응답값:
     * - pointHistoryId: 포인트 이력 ID
     * - amount: 충전 금액
     * - balanceAfter: 충전 후 포인트 잔액
     * - createdAt: 충전 일시
     */
    public List<PointHistoryResponse> getChargeHistories(Long userId) {
        appUserRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_INFORMATION_MISMATCH));

        return pointHistoryRepository
                .findByUserIdAndHistoryTypeOrderByPointHistoryIdDesc(userId, "CHARGE")
                .stream()
                .map(PointHistoryResponse::from)
                .toList();
    }

    /**
     * 기능: 사용자의 포인트 사용 내역을 최신순으로 조회한다.
     *
     * 파라미터:
     * - userId: 조회할 사용자 ID
     *
     * 요청값:
     * - userId: 사용 내역을 조회할 사용자 식별값
     *
     * 응답값:
     * - pointHistoryId: 포인트 이력 ID
     * - amount: 사용한 포인트 금액
     * - balanceAfter: 사용 후 포인트 잔액
     * - createdAt: 포인트 사용 일시
     */
    public List<PointHistoryResponse> getUseHistories(Long userId) {
        appUserRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_INFORMATION_MISMATCH));

        return pointHistoryRepository
                .findByUserIdAndHistoryTypeOrderByPointHistoryIdDesc(userId, "USE")
                .stream()
                .map(PointHistoryResponse::from)
                .toList();
    }

    /**
     * 기능: 포인트와 충전 완료 상태를 저장하고 충전 이력을 생성한다. (내부 처리)
     *
     * 파라미터:
     * - user: 충전 대상 사용자
     * - pointCharge: 충전 결제 정보
     * - paidAmount: 결제 확인 금액
     * - requestHash: 중복 요청 확인용 해시
     *
     * 요청값:
     * - userId: 포인트를 올릴 사용자 식별값
     * - paidAmount: 실제 결제된 충전 금액
     * - requestHash: 충전 요청을 식별하는 해시값
     *
     * 응답값:
     * - userId: 충전한 사용자 ID
     */
    /**
     * 기능: 결제 검증 연결을 재시도한다. (내부 처리)
     *
     * 파라미터:
     * - paymentId: 결제 식별값
     *
     * 요청값:
     * - paymentId: 결제 서버에 검증을 요청할 값
     *
     * 응답값:
     * - PaymentVerificationResponse: 결제 검증 결과
     * - 3회 모두 실패하면 인터넷 연결 예외를 발생시킨다.
     */
    private PaymentVerificationResponse verifyPaymentWithRetry(String paymentId) {
        for (int attempt = 1; attempt <= MAX_PAYMENT_RETRY; attempt++) {
            try {
                return mockPaymentClient.verifyPayment(paymentId);
            } catch (RuntimeException exception) {
                if (attempt == MAX_PAYMENT_RETRY) {
                    throw new BusinessException(ErrorCode.PAYMENT_CONNECTION_FAILED);
                }
            }
        }

        throw new BusinessException(ErrorCode.PAYMENT_CONNECTION_FAILED);
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

    /**
     * 기능: 포인트 충전 요청값이 유효한지 검증한다. (내부 처리)
     *
     * 파라미터:
     * - request: 포인트 충전 요청 정보
     *
     * 요청값:
     * - userId: 1 이상인 사용자 ID
     * - phoneNumber: 비어 있지 않은 사용자 전화번호
     * - paymentId: 비어 있지 않은 결제 식별값
     * - amount: 1 이상인 충전 금액
     *
     * 응답값:
     * - 반환값 없음; 유효하지 않으면 예외를 발생시킨다.
     */
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

    /**
     * 기능: 포인트 충전 요청의 중복 확인용 해시를 생성한다. (내부 처리)
     *
     * 파라미터:
     * - request: 포인트 충전 요청 정보
     *
     * 요청값:
     * - userId: 사용자 ID
     * - paymentId: 결제 식별값
     * - amount: 충전 금액
     *
     * 응답값:
     * - String: SHA-256 방식의 요청 해시
     */
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
