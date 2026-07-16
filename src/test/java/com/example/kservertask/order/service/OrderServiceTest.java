package com.example.kservertask.order.service;

import java.util.Optional;

import com.example.kservertask.menu.entity.Menu;
import com.example.kservertask.menu.repository.MenuRepository;
import com.example.kservertask.order.entity.CoffeeOrder;
import com.example.kservertask.order.entity.OrderStatus;
import com.example.kservertask.order.entity.PaymentStatus;
import com.example.kservertask.order.repository.OrderRepository;
import com.example.kservertask.order.request.CreateOrderRequest;
import com.example.kservertask.order.response.CreateOrderResponse;
import com.example.kservertask.point.entity.PointAccount;
import com.example.kservertask.point.entity.PointHistory;
import com.example.kservertask.point.repository.PointAccountRepository;
import com.example.kservertask.point.repository.PointHistoryRepository;
import com.example.kservertask.user.entity.AppUser;
import com.example.kservertask.user.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PointAccountRepository pointAccountRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createsPaidOrderAndDeductsPoint() {
        AppUser user = new AppUser("user", "01012345678");
        ReflectionTestUtils.setField(user, "userId", 1L);

        Menu menu = new Menu(1L, "아메리카노", 1_000L);
        ReflectionTestUtils.setField(menu, "menuId", 10L);

        PointAccount account = new PointAccount(1L, 5_000L);
        ReflectionTestUtils.setField(account, "version", 0L);

        CreateOrderRequest request = new CreateOrderRequest(
                1L, 10L, "order-token-1", "아메리카노", 1_000L, 0L
        );

        when(appUserRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findByIdempotencyKey("order-token-1"))
                .thenReturn(Optional.empty());
        when(menuRepository.findById(10L)).thenReturn(Optional.of(menu));
        when(pointAccountRepository.findById(1L)).thenReturn(Optional.of(account));
        doAnswer(invocation -> {
            CoffeeOrder order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "orderId", 100L);
            ReflectionTestUtils.setField(order, "version", 0L);
            return order;
        }).when(orderRepository).saveAndFlush(any(CoffeeOrder.class));

        CreateOrderResponse response = orderService.createOrder(request);

        assertThat(response.orderId()).isEqualTo(100L);
        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(response.orderStatus()).isEqualTo(OrderStatus.PREPARING);
        assertThat(response.remainingPoint()).isEqualTo(4_000L);
        verify(pointHistoryRepository).save(any(PointHistory.class));
    }
}
