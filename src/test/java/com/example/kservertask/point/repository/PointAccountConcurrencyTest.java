package com.example.kservertask.point.repository;

import com.example.kservertask.point.entity.PointAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PointAccountConcurrencyTest {

    private static final long USER_ID = 1L;
    private static final long CHARGE_AMOUNT = 1_000L;

    @Container
    @ServiceConnection
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4");

    @Autowired
    private PointAccountRepository pointAccountRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        pointAccountRepository.deleteAll();
        pointAccountRepository.saveAndFlush(new PointAccount(USER_ID, 0L));
    }

    @Test
    void allowsOnlyOneUpdateWhenTwoTransactionsChangeTheSameVersion() throws Exception {
        CountDownLatch loadedTransactions = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<RuntimeException> firstResult = executor.submit(
                    () -> updatePointInNewTransaction(loadedTransactions)
            );
            Future<RuntimeException> secondResult = executor.submit(
                    () -> updatePointInNewTransaction(loadedTransactions)
            );

            RuntimeException firstException = firstResult.get(10, TimeUnit.SECONDS);
            RuntimeException secondException = secondResult.get(10, TimeUnit.SECONDS);

            long successCount = Stream.of(firstException, secondException)
                    .filter(Objects::isNull)
                    .count();
            RuntimeException conflict = Stream.of(firstException, secondException)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElseThrow();

            assertThat(successCount).isEqualTo(1L);
            assertThat(conflict).isInstanceOf(OptimisticLockingFailureException.class);

            PointAccount savedAccount = pointAccountRepository.findById(USER_ID).orElseThrow();
            assertThat(savedAccount.getBalance()).isEqualTo(CHARGE_AMOUNT);
            assertThat(savedAccount.getVersion()).isEqualTo(1L);
        } finally {
            executor.shutdownNow();
        }
    }

    private RuntimeException updatePointInNewTransaction(CountDownLatch loadedTransactions) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        try {
            transactionTemplate.executeWithoutResult(status -> {
                PointAccount pointAccount = pointAccountRepository.findById(USER_ID).orElseThrow();
                waitUntilBothTransactionsLoad(loadedTransactions);

                pointAccount.addPoint(CHARGE_AMOUNT);
                pointAccountRepository.flush();
            });
            return null;
        } catch (RuntimeException exception) {
            return exception;
        }
    }

    private void waitUntilBothTransactionsLoad(CountDownLatch loadedTransactions) {
        loadedTransactions.countDown();

        try {
            if (!loadedTransactions.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("? ????? ?? ?? ?? ???? ?????.");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("??? ??? ??? ???????.", exception);
        }
    }
}
