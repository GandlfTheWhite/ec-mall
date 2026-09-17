package com.zyd.ecmall;

import com.zyd.ecmall.entity.Order;
import com.zyd.ecmall.entity.OrderItem;
import com.zyd.ecmall.mapper.OrderItemMapper;
import com.zyd.ecmall.mapper.OrderMapper;
import com.zyd.ecmall.mapper.ProductMapper;
import com.zyd.ecmall.service.CartService;
import com.zyd.ecmall.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderFlowTest {
    private OrderMapper orders;
    private OrderItemMapper items;
    private ProductMapper products;
    private OrderService service;
    private Order order;

    @BeforeEach
    void setup() {
        orders = mock(OrderMapper.class);
        items = mock(OrderItemMapper.class);
        products = mock(ProductMapper.class);
        service = new OrderService(mock(CartService.class), orders, items, products);
        order = new Order();
        order.setId(10L);
        order.setMemberId(1L);
        order.setStatus(0);
        when(orders.selectByIdForUpdate(10L)).thenReturn(order);
        when(orders.selectById(10L)).thenReturn(order);
    }

    @Test
    void paymentRequiresOwnershipAndUnpaidStatus() {
        assertThrows(ResponseStatusException.class, () -> service.processPayment(10L, 2L));
        for (int status : new int[] {1, 2, 3, 4}) {
            order.setStatus(status);
            assertThrows(ResponseStatusException.class, () -> service.processPayment(10L, 1L));
        }
        verify(orders, never()).updateStatus(anyLong(), anyInt());
        order.setStatus(0);
        assertEquals(1, service.processPayment(10L, 1L).getStatus());
        verify(orders).updateStatus(10L, 1);
    }

    @Test
    void administrativeCancellationRestoresInventoryOnlyOnce() {
        OrderItem item = new OrderItem();
        item.setProductId(3L);
        item.setQuantity(2);
        when(items.selectByOrderId(10L)).thenReturn(List.of(item));
        service.updateStatus(10L, 4);
        order.setStatus(4);
        service.updateStatus(10L, 4);
        verify(products, times(1)).addStock(3L, 2);
        assertThrows(ResponseStatusException.class, () -> service.updateStatus(10L, 0));
    }

    @Test
    void schedulerRechecksLockedStateSoPaidOrdersAreNotCancelled() {
        when(orders.selectTimeoutOrders(any())).thenReturn(List.of(order));
        order.setStatus(1);
        service.cancelTimeoutOrders();
        verify(orders, never()).cancelOrder(anyLong());
        verifyNoInteractions(products);
    }

    @Test
    void orderStatusCannotSkipOrReverseFulfillment() {
        assertThrows(ResponseStatusException.class, () -> service.updateStatus(10L, 3));
        order.setStatus(1);
        service.updateStatus(10L, 2);
        verify(orders).updateStatus(10L, 2);
        order.setStatus(3);
        assertThrows(ResponseStatusException.class, () -> service.updateStatus(10L, 0));
    }
}
