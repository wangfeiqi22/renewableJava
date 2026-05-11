package com.renewable.ai.service;

import com.renewable.ai.dto.OrderCreateDTO;
import com.renewable.ai.dto.OrderReassignDTO;
import com.renewable.ai.dto.OrderVO;
import com.renewable.ai.entity.Driver;
import com.renewable.ai.entity.FleetOrder;
import com.renewable.ai.exception.FleetOrderException;
import com.renewable.ai.repository.DriverRepository;
import com.renewable.ai.repository.FleetOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FleetOrderServiceTest {

    @Mock
    private FleetOrderRepository fleetOrderRepository;

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private FleetOrderService fleetOrderService;

    private FleetOrder testOrder;
    private Driver testDriver;
    private OrderCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        testOrder = new FleetOrder();
        testOrder.setId(1L);
        testOrder.setFleetId(100L);
        testOrder.setOrderNumber("ORD123456");
        testOrder.setProjectId(1L);
        testOrder.setProjectName("测试项目");
        testOrder.setPickupAddress("北京市朝阳区");
        testOrder.setDestinationAddress("北京市海淀区");
        testOrder.setStatus(FleetOrder.OrderStatus.PENDING_ASSIGNMENT);
        testOrder.setIsPooled(false);
        testOrder.setCreatedAt(LocalDateTime.now());
        testOrder.setUpdatedAt(LocalDateTime.now());

        testDriver = new Driver();
        testDriver.setId(1L);
        testDriver.setFleetId(100L);
        testDriver.setName("张三");
        testDriver.setPhone("13800138000");
        testDriver.setStatus("AVAILABLE");

        createDTO = new OrderCreateDTO();
        createDTO.setProjectId(1L);
        createDTO.setProjectName("新项目");
        createDTO.setPickupAddress("北京市朝阳区");
        createDTO.setDestinationAddress("北京市海淀区");
        createDTO.setWasteType("一般废物");
        createDTO.setEstimatedWeight(100.0);
    }

    @Test
    void testCreateOrder_Success() {
        when(fleetOrderRepository.save(any(FleetOrder.class))).thenAnswer(invocation -> {
            FleetOrder order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        OrderVO result = fleetOrderService.createOrder(createDTO, 100L, 1L, "admin");

        assertNotNull(result);
        assertEquals("新项目", result.getProjectName());
        assertEquals("PENDING_ASSIGNMENT", result.getStatus());
        verify(fleetOrderRepository).save(any(FleetOrder.class));
    }

    @Test
    void testGetOrderById_Success() {
        when(fleetOrderRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testOrder));

        OrderVO result = fleetOrderService.getOrderById(1L, 100L);

        assertNotNull(result);
        assertEquals("ORD123456", result.getOrderNumber());
    }

    @Test
    void testGetOrderById_NotFound() {
        when(fleetOrderRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(FleetOrderException.class, () -> 
            fleetOrderService.getOrderById(999L, 100L)
        );
    }

    @Test
    void testAssignOrder_Success() {
        testOrder.setStatus(FleetOrder.OrderStatus.PENDING_ASSIGNMENT);
        
        when(fleetOrderRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testOrder));
        when(driverRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testDriver));
        when(fleetOrderRepository.countByDriverIdAndStatusNotIn(anyLong(), anyList()))
                .thenReturn(0L);
        when(fleetOrderRepository.save(any(FleetOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderVO result = fleetOrderService.assignOrder(1L, 1L, 100L, 1L, "admin");

        assertNotNull(result);
        assertEquals("ASSIGNED", result.getStatus());
        assertEquals("张三", result.getDriverName());
    }

    @Test
    void testAssignOrder_InvalidStatus() {
        testOrder.setStatus(FleetOrder.OrderStatus.ASSIGNED);
        
        when(fleetOrderRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testOrder));

        assertThrows(FleetOrderException.class, () -> 
            fleetOrderService.assignOrder(1L, 1L, 100L, 1L, "admin")
        );
    }

    @Test
    void testAssignOrder_DriverNotAvailable() {
        testDriver.setStatus("ON_LEAVE");
        
        when(fleetOrderRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testOrder));
        when(driverRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testDriver));

        assertThrows(FleetOrderException.class, () -> 
            fleetOrderService.assignOrder(1L, 1L, 100L, 1L, "admin")
        );
    }

    @Test
    void testAssignOrder_DriverHasActiveOrders() {
        when(fleetOrderRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testOrder));
        when(driverRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testDriver));
        when(fleetOrderRepository.countByDriverIdAndStatusNotIn(anyLong(), anyList()))
                .thenReturn(3L);

        assertThrows(FleetOrderException.class, () -> 
            fleetOrderService.assignOrder(1L, 1L, 100L, 1L, "admin")
        );
    }

    @Test
    void testReassignOrder_Success() {
        testOrder.setStatus(FleetOrder.OrderStatus.ASSIGNED);
        testOrder.setDriverId(1L);
        testOrder.setDriverName("张三");
        
        Driver newDriver = new Driver();
        newDriver.setId(2L);
        newDriver.setFleetId(100L);
        newDriver.setName("李四");
        newDriver.setPhone("13900139000");
        newDriver.setStatus("AVAILABLE");

        OrderReassignDTO dto = new OrderReassignDTO();
        dto.setNewDriverId(2L);
        dto.setReason("原司机临时有事");
        
        when(fleetOrderRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testOrder));
        when(driverRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.of(newDriver));
        when(fleetOrderRepository.countByDriverIdAndStatusNotIn(anyLong(), anyList()))
                .thenReturn(0L);
        when(fleetOrderRepository.save(any(FleetOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderVO result = fleetOrderService.reassignOrder(1L, dto, 100L, 1L, "admin");

        assertNotNull(result);
        assertEquals("李四", result.getDriverName());
    }

    @Test
    void testReassignOrder_InvalidStatus() {
        testOrder.setStatus(FleetOrder.OrderStatus.COMPLETED);
        
        when(fleetOrderRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testOrder));

        OrderReassignDTO dto = new OrderReassignDTO();
        dto.setNewDriverId(2L);

        assertThrows(FleetOrderException.class, () -> 
            fleetOrderService.reassignOrder(1L, dto, 100L, 1L, "admin")
        );
    }

    @Test
    void testDeleteOrder_Success() {
        testOrder.setStatus(FleetOrder.OrderStatus.CANCELLED);
        
        when(fleetOrderRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testOrder));

        assertDoesNotThrow(() -> 
            fleetOrderService.deleteOrder(1L, 100L)
        );
        
        verify(fleetOrderRepository).delete(testOrder);
    }

    @Test
    void testDeleteOrder_InProgressStatus() {
        testOrder.setStatus(FleetOrder.OrderStatus.IN_TRANSIT);
        
        when(fleetOrderRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testOrder));

        assertThrows(FleetOrderException.class, () -> 
            fleetOrderService.deleteOrder(1L, 100L)
        );
    }

    @Test
    void testUpdateOrderStatus_Success() {
        when(fleetOrderRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testOrder));
        when(fleetOrderRepository.save(any(FleetOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderVO result = fleetOrderService.updateOrderStatus(1L, "ASSIGNED", 100L);

        assertNotNull(result);
        assertEquals("ASSIGNED", result.getStatus());
    }
}
