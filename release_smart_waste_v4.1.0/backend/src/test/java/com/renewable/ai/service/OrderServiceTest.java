package com.renewable.ai.service;

import com.renewable.ai.entity.Order;
import com.renewable.ai.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createOrder_ShouldGenerateOrderNo() {
        Order order = new Order();
        order.setCreatorId(1L);
        order.setPickupAddress("Test Address");
        
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order created = orderService.createOrder(order);

        assertNotNull(created.getOrderNo());
        assertEquals("Test Address", created.getPickupAddress());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void updateStatus_ShouldUpdateStatus_WhenOrderExists() {
        Order existing = new Order();
        existing.setId(1L);
        existing.setStatus(10);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order updated = orderService.updateStatus(1L, 20);

        assertEquals(20, updated.getStatus());
    }
}
