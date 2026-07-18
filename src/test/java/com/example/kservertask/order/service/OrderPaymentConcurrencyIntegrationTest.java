package com.example.kservertask.order.service;

import com.example.kservertask.error.BusinessException;
import com.example.kservertask.menu.entity.Menu;
import com.example.kservertask.menu.repository.MenuRepository;
import com.example.kservertask.order.producer.OrderEventProducer;
import com.example.kservertask.order.repository.OrderRepository;
import com.example.kservertask.order.request.CreateOrderRequest;
import com.example.kservertask.order.response.CreateOrderResponse;
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
import com.example.kservertask.point.service.PointChargeService;
import com.example.kservertask.user.entity.AppUser;
import com.example.kservertask.user.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({PointChargeService.class, OrderService.class})
class OrderPaymentConcurrencyIntegrationTest {

    private static final long INITIAL_POINT = 5_000L;
    private static final long PAYMENT_AMOUNT = 1_000L;
    private static final String PHONE_NUMBER = "01012345678";
    private static final String MENU_NAME = "Americano";

    @Container
    @ServiceConnection
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4");

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PointAccountRepository pointAccountRepository;

    @Autowired
    private PointChargeRepository pointChargeRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private PointChargeService pointChargeService;

    @Autowired
    private OrderService orderService;

    @MockitoBean
    private MockPaymentClient mockPaymentClient;

    @MockitoBean
    private OrderEventProducer orderEventProducer;

    private Long userId;
    private Long menuId;
    private Long pointVersion;

    @BeforeEach
    void setUp() {
        pointHistoryRepository.deleteAll();
        orderRepository.deleteAll();
        pointChargeRepository.deleteAll();
        pointAccountRepository.deleteAll();
        menuRepository.deleteAll();
        appUserRepository.deleteAll();

        AppUser user = appUserRepository.saveAndFlush(new AppUser("user", PHONE_NUMBER));
        Menu menu = menuRepository.saveAndFlush(new Menu(1L, MENU_NAME, PAYMENT_AMOUNT));
        PointAccount pointAccount = pointAccountRepository.saveAndFlush(
                new PointAccount(user.getUserId(), INITIAL_POINT)
        );

        userId = user.getUserId();
        menuId = menu.getMenuId();
        pointVersion = pointAccount.getVersion();

        when(mockPaymentClient.verifyPayment(anyString()))
                .thenAnswer(invocation -> {
                    String paymentId = invocation.getArgument(0);
                    return new PaymentVerificationResponse(paymentId, PAYMENT_AMOUNT, true);
                });
    }

    @Test
    void chargesPointOnlyOnceForConcurrentSamePaymentId() throws Exception {
        PointChargeRequest request = new PointChargeRequest(
                userId,
                PHONE_NUMBER,
                "same-payment-id",
                PAYMENT_AMOUNT
        );

        List<Attempt<PointChargeResponse>> attempts = runConcurrently(
                () -> pointChargeService.chargePoint(request)
        );

        assertAtLeastOneSucceededAndOnlyKnownConcurrencyFailures(attempts);
        assertThat(pointChargeRepository.findAll())
                .singleElement()
                .extracting(PointCharge::getStatus)
                .isEqualTo(PointChargeStatus.COMPLETED);
        assertThat(pointHistoryRepository.findAll())
                .singleElement()
                .extracting(PointHistory::getHistoryType)
                .isEqualTo("CHARGE");

        PointAccount savedAccount = findPointAccount();
        assertThat(savedAccount.getBalance()).isEqualTo(INITIAL_POINT + PAYMENT_AMOUNT);
        assertThat(savedAccount.getVersion()).isEqualTo(pointVersion + 1);
    }

    @Test
    void createsOrderAndDeductsPointOnlyOnceForConcurrentSameOrderToken() throws Exception {
        CreateOrderRequest request = createOrderRequest("same-order-token");

        List<Attempt<CreateOrderResponse>> attempts = runConcurrently(
                () -> orderService.createOrder(request)
        );

        assertAtLeastOneSucceededAndOnlyKnownConcurrencyFailures(attempts);
        assertThat(orderRepository.findAll()).hasSize(1);
        assertThat(pointHistoryRepository.findAll())
                .singleElement()
                .extracting(PointHistory::getHistoryType)
                .isEqualTo("USE");

        PointAccount savedAccount = findPointAccount();
        assertThat(savedAccount.getBalance()).isEqualTo(INITIAL_POINT - PAYMENT_AMOUNT);
        assertThat(savedAccount.getVersion()).isEqualTo(pointVersion + 1);
        verify(orderEventProducer, times(1)).sendOrderEvent(menuId, 1);
    }

    @Test
    void rollsBackOrderAndPointWhenHistorySaveFails() {
        String orderToken = "rollback-order-token";
        pointHistoryRepository.saveAndFlush(new PointHistory(
                userId,
                null,
                "USE",
                PAYMENT_AMOUNT,
                INITIAL_POINT,
                pointVersion,
                orderToken,
                "0".repeat(64)
        ));

        assertThatThrownBy(() -> orderService.createOrder(createOrderRequest(orderToken)))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(orderRepository.findAll()).isEmpty();
        assertThat(pointHistoryRepository.findAll()).hasSize(1);

        PointAccount savedAccount = findPointAccount();
        assertThat(savedAccount.getBalance()).isEqualTo(INITIAL_POINT);
        assertThat(savedAccount.getVersion()).isEqualTo(pointVersion);
        verifyNoInteractions(orderEventProducer);
    }

    private CreateOrderRequest createOrderRequest(String orderToken) {
        return new CreateOrderRequest(
                userId,
                menuId,
                orderToken,
                MENU_NAME,
                PAYMENT_AMOUNT,
                pointVersion
        );
    }

    private PointAccount findPointAccount() {
        return pointAccountRepository.findById(userId).orElseThrow();
    }

    private <T> List<Attempt<T>> runConcurrently(Callable<T> task) throws Exception {
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Attempt<T>> firstResult = executor.submit(() -> captureAttempt(task, ready, start));
            Future<Attempt<T>> secondResult = executor.submit(() -> captureAttempt(task, ready, start));

            waitForLatch(ready, "Both requests were not ready within the timeout.");
            start.countDown();

            return List.of(
                    firstResult.get(15, TimeUnit.SECONDS),
                    secondResult.get(15, TimeUnit.SECONDS)
            );
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    private <T> Attempt<T> captureAttempt(
            Callable<T> task,
            CountDownLatch ready,
            CountDownLatch start
    ) {
        ready.countDown();
        waitForLatch(start, "The concurrent start signal was not received.");

        try {
            return new Attempt<>(task.call(), null);
        } catch (RuntimeException exception) {
            return new Attempt<>(null, exception);
        } catch (Exception exception) {
            throw new IllegalStateException("The concurrent request failed.", exception);
        }
    }

    private <T> void assertAtLeastOneSucceededAndOnlyKnownConcurrencyFailures(
            List<Attempt<T>> attempts
    ) {
        assertThat(attempts).anyMatch(Attempt::succeeded);
        assertThat(attempts)
                .filteredOn(attempt -> !attempt.succeeded())
                .allSatisfy(attempt -> assertThat(attempt.failure())
                        .isInstanceOfAny(BusinessException.class, DataAccessException.class));
    }

    private void waitForLatch(CountDownLatch latch, String timeoutMessage) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException(timeoutMessage);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("The concurrency test wait was interrupted.", exception);
        }
    }

    private record Attempt<T>(T value, RuntimeException failure) {

        private boolean succeeded() {
            return failure == null;
        }
    }
}
