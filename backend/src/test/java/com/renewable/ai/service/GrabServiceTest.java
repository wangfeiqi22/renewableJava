package com.renewable.ai.service;

import com.renewable.ai.dto.GrabRecordVO;
import com.renewable.ai.entity.Driver;
import com.renewable.ai.entity.FleetOrder;
import com.renewable.ai.entity.GrabRecord;
import com.renewable.ai.entity.GrabRule;
import com.renewable.ai.exception.GrabException;
import com.renewable.ai.repository.DriverRepository;
import com.renewable.ai.repository.FleetOrderRepository;
import com.renewable.ai.repository.GrabRecordRepository;
import com.renewable.ai.repository.GrabRuleRepository;
import com.renewable.ai.util.DistributedLock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GrabServiceTest {

    @Mock
    private FleetOrderRepository fleetOrderRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private GrabRuleRepository grabRuleRepository;

    @Mock
    private GrabRecordRepository grabRecordRepository;

    @Mock
    private DistributedLock distributedLock;

    @InjectMocks
    private GrabService grabService;

    private FleetOrder testOrder;
    private Driver testDriver;
    private GrabRule testRule;

    @BeforeEach
    void setUp() {
        testOrder = new FleetOrder();
        testOrder.setId(1L);
        testOrder.setFleetId(100L);
        testOrder.setOrderNumber("ORD123456");
        testOrder.setStatus(FleetOrder.OrderStatus.PENDING_ASSIGNMENT);
        testOrder.setIsPooled(true);

        testDriver = new Driver();
        testDriver.setId(1L);
        testDriver.setFleetId(100L);
        testDriver.setName("张三");
        testDriver.setPhone("13800138000");
        testDriver.setStatus("AVAILABLE");

        testRule = new GrabRule();
        testRule.setId(1L);
        testRule.setFleetId(100L);
        testRule.setEnabled(true);
        testRule.setMaxOrdersPerDriverPerDay(10);
        testRule.setMinIntervalSeconds(5);
        testRule.setFirstComeFirstServe(true);
        testRule.setAllowMultipleGrab(false);
    }

    @Test
    void testGrabOrder_Success() {
        when(distributedLock.tryLock(anyString(), anyLong())).thenReturn(true);
        when(fleetOrderRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testOrder));
        when(driverRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testDriver));
        when(grabRuleRepository.findByFleetIdAndEnabled(anyLong(), anyBoolean()))
                .thenReturn(Arrays.asList(testRule));
        when(grabRecordRepository.findByOrderIdAndSuccessOrderByPositionAsc(anyLong(), anyBoolean()))
                .thenReturn(Arrays.asList());
        when(grabRecordRepository.findMaxPositionByOrderId(anyLong())).thenReturn(0);
        when(grabRecordRepository.save(any(GrabRecord.class))).thenAnswer(invocation -> {
            GrabRecord record = invocation.getArgument(0);
            record.setId(1L);
            return record;
        });
        when(fleetOrderRepository.save(any(FleetOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GrabRecordVO result = grabService.grabOrder(1L, 1L, 100L);

        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertTrue(result.getIsWinner());
        verify(distributedLock).unlock(anyString());
    }

    @Test
    void testGrabOrder_LockFailed() {
        when(distributedLock.tryLock(anyString(), anyLong())).thenReturn(false);

        assertThrows(GrabException.class, () -> 
            grabService.grabOrder(1L, 1L, 100L)
        );
    }

    @Test
    void testGrabOrder_OrderNotInPool() {
        testOrder.setIsPooled(false);
        
        when(distributedLock.tryLock(anyString(), anyLong())).thenReturn(true);
        when(fleetOrderRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testOrder));

        assertThrows(GrabException.class, () -> 
            grabService.grabOrder(1L, 1L, 100L)
        );
        
        verify(distributedLock).unlock(anyString());
    }

    @Test
    void testGrabOrder_DriverNotAvailable() {
        testDriver.setStatus("ON_LEAVE");
        
        when(distributedLock.tryLock(anyString(), anyLong())).thenReturn(true);
        when(fleetOrderRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testOrder));
        when(driverRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testDriver));

        assertThrows(GrabException.class, () -> 
            grabService.grabOrder(1L, 1L, 100L)
        );
        
        verify(distributedLock).unlock(anyString());
    }

    @Test
    void testGrabOrder_AlreadyGrabbed() {
        GrabRecord existingRecord = new GrabRecord();
        existingRecord.setOrderId(1L);
        existingRecord.setSuccess(true);
        existingRecord.setIsWinner(true);
        
        when(distributedLock.tryLock(anyString(), anyLong())).thenReturn(true);
        when(fleetOrderRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testOrder));
        when(driverRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testDriver));
        when(grabRuleRepository.findByFleetIdAndEnabled(anyLong(), anyBoolean()))
                .thenReturn(Arrays.asList(testRule));
        when(grabRecordRepository.findByOrderIdAndSuccessOrderByPositionAsc(anyLong(), anyBoolean()))
                .thenReturn(Arrays.asList(existingRecord));

        assertThrows(GrabException.class, () -> 
            grabService.grabOrder(1L, 1L, 100L)
        );
        
        verify(distributedLock).unlock(anyString());
    }

    @Test
    void testGrabOrder_MaxOrdersReached() {
        when(distributedLock.tryLock(anyString(), anyLong())).thenReturn(true);
        when(fleetOrderRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testOrder));
        when(driverRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testDriver));
        when(grabRuleRepository.findByFleetIdAndEnabled(anyLong(), anyBoolean()))
                .thenReturn(Arrays.asList(testRule));
        when(grabRecordRepository.findByOrderIdAndSuccessOrderByPositionAsc(anyLong(), anyBoolean()))
                .thenReturn(Arrays.asList());
        when(grabRecordRepository.countByDriverIdAndGrabTimeBetween(anyLong(), any(), any()))
                .thenReturn(10L);

        assertThrows(GrabException.class, () -> 
            grabService.grabOrder(1L, 1L, 100L)
        );
        
        verify(distributedLock).unlock(anyString());
    }

    @Test
    void testAddOrderToPool_Success() {
        when(fleetOrderRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testOrder));
        when(fleetOrderRepository.save(any(FleetOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> 
            grabService.addOrderToPool(1L, 100L)
        );
        
        verify(fleetOrderRepository).save(any(FleetOrder.class));
    }

    @Test
    void testAddOrderToPool_InvalidStatus() {
        testOrder.setStatus(FleetOrder.OrderStatus.ASSIGNED);
        
        when(fleetOrderRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testOrder));

        assertThrows(GrabException.class, () -> 
            grabService.addOrderToPool(1L, 100L)
        );
    }
}
