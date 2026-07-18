package com.example.kservertask.order.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Objects;

import com.example.kservertask.error.BusinessException;
import com.example.kservertask.error.ErrorCode;
import com.example.kservertask.menu.entity.Menu;
import com.example.kservertask.menu.repository.MenuRepository;
import com.example.kservertask.order.entity.CoffeeOrder;
import com.example.kservertask.order.entity.OrderStatus;
import com.example.kservertask.order.entity.PaymentStatus;
import com.example.kservertask.order.repository.OrderRepository;
import com.example.kservertask.order.request.CreateOrderRequest;
import com.example.kservertask.order.response.CreateOrderResponse;
import com.example.kservertask.order.producer.OrderEventProducer;
import com.example.kservertask.point.entity.PointAccount;
import com.example.kservertask.point.entity.PointHistory;
import com.example.kservertask.point.repository.PointAccountRepository;
import com.example.kservertask.point.repository.PointHistoryRepository;
import com.example.kservertask.user.entity.AppUser;
import com.example.kservertask.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final AppUserRepository appUserRepository;
    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final PointAccountRepository pointAccountRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final OrderEventProducer orderEventProducer;

    /**
     * 기능: 메뉴를 포인트로 결제하고 주문을 생성한다.
     *
     * 파라미터:
     * - request: 주문 요청 정보
     *
     * 요청값:
     * - userId: 사용자 ID
     * - menuId: 주문할 메뉴 ID
     * - orderToken: 중복 주문 방지용 멱등성 키
     * - expectedMenuName: 요청 당시 메뉴명
     * - expectedPrice: 요청 당시 메뉴 가격
     * - expectedPointVersion: 요청 당시 포인트 계좌 버전
     *
     * 응답값:
     * - CreateOrderResponse: 주문 ID, 메뉴 정보, 결제 금액, 잔여 포인트, 주문·결제 상태
     */
    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        validateRequest(request);

        AppUser user = appUserRepository.findById(request.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_INFORMATION_MISMATCH));

        CoffeeOrder existingOrder = orderRepository
                .findByIdempotencyKey(request.orderToken())
                .orElse(null);

        if (existingOrder != null) {
            if (!Objects.equals(existingOrder.getUserId(), user.getUserId())) {
                throw new BusinessException(ErrorCode.USER_INFORMATION_MISMATCH);
            }

            return toResponse(existingOrder);
        }

        Menu menu = menuRepository.findById(request.menuId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));

        if (!Objects.equals(menu.getName(), request.expectedMenuName())
                || menu.getPrice() != request.expectedPrice()) {
            throw new BusinessException(ErrorCode.ORDER_FAILED);
        }

        PointAccount pointAccount = pointAccountRepository.findById(user.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_FAILED));

        if (!Objects.equals(pointAccount.getVersion(), request.expectedPointVersion())
                || pointAccount.getBalance() < menu.getPrice()) {
            throw new BusinessException(ErrorCode.PAYMENT_REJECTED);
        }

        String requestHash = createRequestHash(request);
        CoffeeOrder order = new CoffeeOrder(
                user.getUserId(),
                menu.getMenuId(),
                menu.getName(),
                menu.getPrice(),
                PaymentStatus.PAID,
                OrderStatus.PREPARING,
                request.orderToken(),
                requestHash,
                Instant.now()
        );

        try {
            orderRepository.saveAndFlush(order);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.DUPLICATE_REQUEST);
        }

        pointAccount.deductPoint(menu.getPrice());
        pointAccountRepository.flush();

        pointHistoryRepository.save(new PointHistory(
                user.getUserId(),
                order.getOrderId(),
                "USE",
                menu.getPrice(),
                pointAccount.getBalance(),
                pointAccount.getVersion(),
                request.orderToken(),
                requestHash
        ));

        publishOrderEventAfterCommit(order.getMenuId(), 1);

        return toResponse(order, pointAccount.getBalance());
    }

    /**
     * 기능: 기존 주문의 현재 포인트 잔액을 포함해 응답을 만든다. (내부 처리)
     *
     * 파라미터:
     * - order: 기존 주문 엔티티
     *
     * 요청값:
     * - userId: 포인트 잔액을 조회할 사용자 ID
     *
     * 응답값:
     * - CreateOrderResponse: 기존 주문 정보와 현재 잔여 포인트
     */
    /**
     * 기능: 주문 트랜잭션 커밋 이후 주문 완료 이벤트를 발행한다. (내부 처리)
     *
     * 파라미터:
     * - productId: 주문한 메뉴 ID
     * - quantity: 주문 수량
     *
     * 요청값:
     * - productId, quantity: Kafka 이벤트로 전달할 주문 정보
     *
     * 응답값:
     * - 반환값 없음; 트랜잭션 커밋 후 Kafka 이벤트를 발행한다.
     */
    private void publishOrderEventAfterCommit(Long productId, int quantity) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            orderEventProducer.sendOrderEvent(productId, quantity);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                orderEventProducer.sendOrderEvent(productId, quantity);
            }
        });
    }

    private CreateOrderResponse toResponse(CoffeeOrder order) {
        PointAccount pointAccount = pointAccountRepository.findById(order.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_FAILED));
        return toResponse(order, pointAccount.getBalance());
    }

    /**
     * 기능: 주문 엔티티를 주문 응답으로 변환한다. (내부 처리)
     *
     * 파라미터:
     * - order: 주문 엔티티
     * - remainingPoint: 차감 후 잔여 포인트
     *
     * 요청값:
     * - orderId, menuId, paymentAmount, paymentStatus, orderStatus, version: 주문 정보
     *
     * 응답값:
     * - CreateOrderResponse: 주문 결과 정보
     */
    private CreateOrderResponse toResponse(CoffeeOrder order, long remainingPoint) {
        return new CreateOrderResponse(
                order.getOrderId(),
                order.getMenuId(),
                order.getMenuNameSnapshot(),
                order.getPaymentAmount(),
                remainingPoint,
                order.getPaymentStatus(),
                order.getOrderStatus(),
                order.getVersion()
        );
    }

    /**
     * 기능: 주문 요청값의 필수 항목을 검증한다. (내부 처리)
     *
     * 파라미터:
     * - request: 주문 요청 정보
     *
     * 요청값:
     * - userId, menuId, orderToken, expectedMenuName, expectedPrice, expectedPointVersion
     *
     * 응답값:
     * - 반환값 없음; 잘못된 요청이면 주문 실패 예외를 발생시킨다.
     */
    private void validateRequest(CreateOrderRequest request) {
        if (request == null
                || request.userId() == null
                || request.menuId() == null
                || request.orderToken() == null
                || request.orderToken().isBlank()
                || request.expectedMenuName() == null
                || request.expectedMenuName().isBlank()
                || request.expectedPrice() == null
                || request.expectedPointVersion() == null) {
            throw new BusinessException(ErrorCode.ORDER_FAILED);
        }
    }

    /**
     * 기능: 주문 요청의 중복 확인용 SHA-256 해시를 생성한다. (내부 처리)
     *
     * 파라미터:
     * - request: 주문 요청 정보
     *
     * 요청값:
     * - userId, menuId, orderToken, expectedPrice: 해시 생성에 사용할 값
     *
     * 응답값:
     * - String: 요청 해시
     */
    private String createRequestHash(CreateOrderRequest request) {
        String value = request.userId() + ":" + request.menuId() + ":"
                + request.orderToken() + ":" + request.expectedPrice();

        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new BusinessException(ErrorCode.ORDER_FAILED);
        }
    }
}
