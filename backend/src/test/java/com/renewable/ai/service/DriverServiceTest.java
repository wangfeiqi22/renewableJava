package com.renewable.ai.service;

import com.renewable.ai.dto.DriverCreateDTO;
import com.renewable.ai.dto.DriverUpdateDTO;
import com.renewable.ai.dto.DriverVO;
import com.renewable.ai.entity.Driver;
import com.renewable.ai.exception.DriverException;
import com.renewable.ai.repository.DriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private DriverService driverService;

    private Driver testDriver;
    private DriverCreateDTO createDTO;
    private DriverUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        testDriver = new Driver();
        testDriver.setId(1L);
        testDriver.setFleetId(100L);
        testDriver.setName("张三");
        testDriver.setPhone("13800138000");
        testDriver.setIdNumber("110101199001011234");
        testDriver.setDriverLicenseNumber("DL123456789");
        testDriver.setStatus("AVAILABLE");
        testDriver.setCreatedAt(LocalDateTime.now());
        testDriver.setUpdatedAt(LocalDateTime.now());

        createDTO = new DriverCreateDTO();
        createDTO.setName("李四");
        createDTO.setPhone("13900139000");
        createDTO.setIdNumber("110101199502025678");
        createDTO.setDriverLicenseNumber("DL987654321");

        updateDTO = new DriverUpdateDTO();
        updateDTO.setName("王五");
        updateDTO.setPhone("13700137000");
    }

    @Test
    void testCreateDriver_Success() {
        when(driverRepository.existsByPhoneAndFleetIdNot(anyString(), anyLong())).thenReturn(false);
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> {
            Driver driver = invocation.getArgument(0);
            driver.setId(1L);
            return driver;
        });

        DriverVO result = driverService.createDriver(createDTO, 100L, 1L, "admin");

        assertNotNull(result);
        assertEquals("李四", result.getName());
        assertEquals("13900139000", result.getPhone());
        verify(driverRepository).save(any(Driver.class));
    }

    @Test
    void testCreateDriver_PhoneAlreadyExists() {
        when(driverRepository.existsByPhoneAndFleetIdNot(anyString(), anyLong())).thenReturn(true);

        assertThrows(DriverException.class, () -> 
            driverService.createDriver(createDTO, 100L, 1L, "admin")
        );
        
        verify(driverRepository, never()).save(any());
    }

    @Test
    void testCreateDriver_InvalidLicenseFormat() {
        createDTO.setDriverLicenseNumber("INVALID");

        when(driverRepository.existsByPhoneAndFleetIdNot(anyString(), anyLong())).thenReturn(false);

        assertThrows(DriverException.class, () -> 
            driverService.createDriver(createDTO, 100L, 1L, "admin")
        );
        
        verify(driverRepository, never()).save(any());
    }

    @Test
    void testGetDrivers_Success() {
        List<Driver> drivers = Arrays.asList(testDriver);
        Page<Driver> driverPage = new PageImpl<>(drivers);
        
        when(driverRepository.findByFleetIdWithFilters(anyLong(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(driverPage);

        Page<DriverVO> result = driverService.getDrivers(100L, null, null, null, Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("张三", result.getContent().get(0).getName());
    }

    @Test
    void testGetDriverById_Success() {
        when(driverRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testDriver));

        DriverVO result = driverService.getDriverById(1L, 100L);

        assertNotNull(result);
        assertEquals("张三", result.getName());
    }

    @Test
    void testGetDriverById_NotFound() {
        when(driverRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(DriverException.class, () -> 
            driverService.getDriverById(999L, 100L)
        );
    }

    @Test
    void testUpdateDriver_Success() {
        when(driverRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testDriver));
        when(driverRepository.existsByPhoneAndFleetIdNot(anyString(), anyLong()))
                .thenReturn(false);
        when(driverRepository.save(any(Driver.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DriverVO result = driverService.updateDriver(1L, updateDTO, 100L, 1L, "admin");

        assertNotNull(result);
        assertEquals("王五", result.getName());
        verify(driverRepository).save(any(Driver.class));
    }

    @Test
    void testDeleteDriver_Success() {
        when(driverRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testDriver));
        when(driverRepository.countByDriverIdAndStatusNotIn(anyLong(), anyList()))
                .thenReturn(0L);

        assertDoesNotThrow(() -> 
            driverService.deleteDriver(1L, 100L, 1L, "admin")
        );
        
        verify(driverRepository).delete(testDriver);
    }

    @Test
    void testDeleteDriver_HasPendingOrders() {
        when(driverRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testDriver));
        when(driverRepository.countByDriverIdAndStatusNotIn(anyLong(), anyList()))
                .thenReturn(5L);

        assertThrows(DriverException.class, () -> 
            driverService.deleteDriver(1L, 100L, 1L, "admin")
        );
        
        verify(driverRepository, never()).delete(any());
    }

    @Test
    void testDeleteDriver_NotFound() {
        when(driverRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(DriverException.class, () -> 
            driverService.deleteDriver(999L, 100L, 1L, "admin")
        );
    }

    @Test
    void testUpdateDriverStatus_Success() {
        when(driverRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testDriver));
        when(driverRepository.save(any(Driver.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DriverVO result = driverService.updateDriverStatus(1L, "ON_LEAVE", 100L);

        assertNotNull(result);
        assertEquals("ON_LEAVE", result.getStatus());
    }

    @Test
    void testUpdateDriverStatus_InvalidStatus() {
        when(driverRepository.findByIdAndFleetId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testDriver));

        assertThrows(DriverException.class, () -> 
            driverService.updateDriverStatus(1L, "INVALID_STATUS", 100L)
        );
    }
}
