package com.example.kservertask.point.service;

import com.example.kservertask.error.BusinessException;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointChargeServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PointChargeRepository pointChargeRepository;

    @Mock
    private PointAccountRepository pointAccountRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @Mock
    private MockPaymentClient mockPaymentClient;

    @InjectMocks
    private PointChargeService pointChargeService;

    @Test
    void chargesPointWhenMockPaymentIsCompleted() {
        AppUser user = new AppUser("user", "01012345678");
        ReflectionTestUtils.setField(user, "userId", 1L);
        PointAccount account = new PointAccount(1L, 0L);
        ReflectionTestUtils.setField(account, "version", 0L);
        PointChargeRequest request = new PointChargeRequest(
                1L, "01012345678", "payment-1", 5_000L
        );

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(user));
        when(mockPaymentClient.verifyPayment("payment-1"))
                .thenReturn(new PaymentVerificationResponse("payment-1", 5_000L, true));
        when(pointChargeRepository.findByPaymentIdForUpdate("payment-1"))
                .thenReturn(Optional.empty());
        when(pointAccountRepository.findById(1L)).thenReturn(Optional.of(account));

        PointChargeResponse response = pointChargeService.chargePoint(request);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(account.getBalance()).isEqualTo(5_000L);

        ArgumentCaptor<PointCharge> chargeCaptor = ArgumentCaptor.forClass(PointCharge.class);
        verify(pointChargeRepository).saveAndFlush(chargeCaptor.capture());
        assertThat(chargeCaptor.getValue().getStatus()).isEqualTo(PointChargeStatus.COMPLETED);
        verify(pointHistoryRepository).save(any(PointHistory.class));
    }

    @Test
    void rejectsChargeWhenUserInformationDoesNotMatch() {
        AppUser user = new AppUser("user", "01099999999");
        ReflectionTestUtils.setField(user, "userId", 1L);
        PointChargeRequest request = new PointChargeRequest(
                1L, "01012345678", "payment-mismatch", 5_000L
        );

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> pointChargeService.chargePoint(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("회원 정보가 일치하지 않습니다.");
    }

    @Test
    void returnsPaymentConnectionFailureAfterThreeRetries() {
        AppUser user = new AppUser("user", "01012345678");
        ReflectionTestUtils.setField(user, "userId", 1L);
        PointChargeRequest request = new PointChargeRequest(
                1L, "01012345678", "payment-network-error", 5_000L
        );

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(user));
        when(mockPaymentClient.verifyPayment("payment-network-error"))
                .thenThrow(new RuntimeException("결제 서버 연결이 끊어졌습니다."));

        assertThatThrownBy(() -> pointChargeService.chargePoint(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("인터넷 연결이 끊겼습니다.");

        verify(mockPaymentClient, times(3)).verifyPayment("payment-network-error");
    }

    @Test
    void reconnectsAndChargesAfterFirstPaymentVerificationFailure() {
        AppUser user = new AppUser("user", "01012345678");
        ReflectionTestUtils.setField(user, "userId", 1L);
        PointAccount account = new PointAccount(1L, 0L);
        ReflectionTestUtils.setField(account, "version", 0L);
        PointChargeRequest request = new PointChargeRequest(
                1L, "01012345678", "payment-reconnect", 5_000L
        );

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(user));
        when(mockPaymentClient.verifyPayment("payment-reconnect"))
                .thenThrow(new RuntimeException("연결 끊김"))
                .thenReturn(new PaymentVerificationResponse("payment-reconnect", 5_000L, true));
        when(pointChargeRepository.findByPaymentIdForUpdate("payment-reconnect"))
                .thenReturn(Optional.empty());
        when(pointAccountRepository.findById(1L)).thenReturn(Optional.of(account));

        PointChargeResponse response = pointChargeService.chargePoint(request);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(account.getBalance()).isEqualTo(5_000L);
        verify(mockPaymentClient, times(2)).verifyPayment("payment-reconnect");
    }
}
