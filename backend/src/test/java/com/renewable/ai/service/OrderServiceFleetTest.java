package com.renewable.ai.service;

import com.renewable.ai.entity.Order;
import com.renewable.ai.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class OrderServiceFleetTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    public void setUp() {
        orderRepository.deleteAll();
    }

    @Test
    public void testCreateFleetOrder_Success() {
        // Test Case: 正常创建车队订单
        Long driverId = 1L;
        Long fleetId = 100L;

        Order order = new Order();
        order.setPickupAddress("测试地址");
        order.setWasteType("recyclable");
        order.setEstWeight(java.math.BigDecimal.valueOf(5.5));

        Order created = orderService.createFleetOrder(order, driverId, fleetId);

        assertNotNull(created);
        assertNotNull(created.getOrderNo());
        assertEquals(driverId, created.getDriverId());
        assertEquals(fleetId, created.getFleetId());
        assertEquals(20, created.getStatus()); // Assigned status
        assertEquals("FLEET_CREATE", created.getSourceType());
    }

    @Test
    public void testCreateFleetOrder_NoDriver() {
        // Test Case: 未选择司机时应该抛出异常
        Order order = new Order();
        order.setPickupAddress("测试地址");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.createFleetOrder(order, null, 100L);
        });

        assertEquals("司机ID不能为空", exception.getMessage());
    }

    @Test
    public void testCreateFleetOrder_DuplicateSubmission() {
        // Test Case: 重复提交应该在1分钟内被阻止
        Long driverId = 1L;
        Long fleetId = 100L;

        Order order1 = new Order();
        order1.setPickupAddress("重复地址");
        order1.setWasteType("recyclable");

        Order created1 = orderService.createFleetOrder(order1, driverId, fleetId);
        assertNotNull(created1);

        // 尝试在同一地址提交第二个订单
        Order order2 = new Order();
        order2.setPickupAddress("重复地址");
        order2.setWasteType("recyclable");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.createFleetOrder(order2, driverId, fleetId);
        });

        assertEquals("重复提交：您刚刚已在该地址创建了相同清运单", exception.getMessage());
    }

    @Test
    public void testCreateFleetOrder_WithAllFields() {
        // Test Case: 创建包含所有字段的订单
        Long driverId = 2L;
        Long fleetId = 101L;

        Order order = new Order();
        order.setPickupAddress("完整地址");
        order.setWasteType("hazardous");
        order.setEstWeight(java.math.BigDecimal.valueOf(10.0));
        order.setPlacementStatus("已堆放到位");
        order.setWasteDesc("测试备注信息");

        Order created = orderService.createFleetOrder(order, driverId, fleetId);

        assertNotNull(created);
        assertEquals("完整地址", created.getPickupAddress());
        assertEquals("hazardous", created.getWasteType());
        assertEquals(java.math.BigDecimal.valueOf(10.0), created.getEstWeight());
        assertEquals("已堆放到位", created.getPlacementStatus());
        assertEquals("测试备注信息", created.getWasteDesc());
    }

    @Test
    public void testCreateFleetOrder_StatusFlow() {
        // Test Case: 验证订单状态流转
        Long driverId = 3L;
        Long fleetId = 102L;

        Order order = new Order();
        order.setPickupAddress("状态测试地址");

        Order created = orderService.createFleetOrder(order, driverId, fleetId);
        assertEquals(20, created.getStatus()); // Initial: Assigned

        // 模拟状态更新: Assigned -> Accepted
        Order updated = orderService.updateStatus(created.getId(), 25);
        assertEquals(25, updated.getStatus());

        // 模拟状态更新: Accepted -> Loading
        updated = orderService.updateStatus(created.getId(), 30);
        assertEquals(30, updated.getStatus());
    }
}
