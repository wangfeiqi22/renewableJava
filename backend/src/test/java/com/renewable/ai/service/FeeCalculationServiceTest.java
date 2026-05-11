package com.renewable.ai.service;

import com.renewable.ai.entity.FeeConfig;
import com.renewable.ai.repository.FeeConfigRepository;
import com.renewable.ai.repository.StationRepository;
import com.renewable.ai.repository.StationWasteTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeeCalculationServiceTest {

    @Mock
    private FeeConfigRepository feeConfigRepository;

    @Mock
    private StationRepository stationRepository;

    @Mock
    private StationWasteTypeRepository stationWasteTypeRepository;

    @InjectMocks
    private FeeCalculationService feeCalculationService;

    @BeforeEach
    void setUp() {
        FeeConfig basePrice = new FeeConfig();
        basePrice.setConfigKey("transport_base_price");
        basePrice.setConfigValue("200");
        when(feeConfigRepository.findByConfigKey("transport_base_price"))
            .thenReturn(Optional.of(basePrice));

        FeeConfig baseKm = new FeeConfig();
        baseKm.setConfigKey("transport_base_km");
        baseKm.setConfigValue("5");
        when(feeConfigRepository.findByConfigKey("transport_base_km"))
            .thenReturn(Optional.of(baseKm));

        FeeConfig pricePerKm = new FeeConfig();
        pricePerKm.setConfigKey("transport_price_per_km");
        pricePerKm.setConfigValue("18");
        when(feeConfigRepository.findByConfigKey("transport_price_per_km"))
            .thenReturn(Optional.of(pricePerKm));

        FeeConfig forkliftFee = new FeeConfig();
        forkliftFee.setConfigKey("forklift_fee");
        forkliftFee.setConfigValue("100");
        when(feeConfigRepository.findByConfigKey("forklift_fee"))
            .thenReturn(Optional.of(forkliftFee));

        FeeConfig containerFee = new FeeConfig();
        containerFee.setConfigKey("container_fee");
        containerFee.setConfigValue("50");
        when(feeConfigRepository.findByConfigKey("container_fee"))
            .thenReturn(Optional.of(containerFee));
    }

    @Test
    void testTransportFeeWithinBaseKm() {
        BigDecimal distance = new BigDecimal("3");
        BigDecimal fee = feeCalculationService.calculateTransportFee(distance);

        assertEquals(new BigDecimal("200.00"), fee);
    }

    @Test
    void testTransportFeeExactlyBaseKm() {
        BigDecimal distance = new BigDecimal("5");
        BigDecimal fee = feeCalculationService.calculateTransportFee(distance);

        assertEquals(new BigDecimal("200.00"), fee);
    }

    @Test
    void testTransportFeeExceedingBaseKm() {
        BigDecimal distance = new BigDecimal("7");
        BigDecimal fee = feeCalculationService.calculateTransportFee(distance);

        assertEquals(new BigDecimal("236.00"), fee);
    }

    @Test
    void testTransportFeeExceedingBaseKmLarge() {
        BigDecimal distance = new BigDecimal("20");
        BigDecimal fee = feeCalculationService.calculateTransportFee(distance);

        assertEquals(new BigDecimal("470.00"), fee);
    }

    @Test
    void testDisposalFeeCalculation() {
        BigDecimal wasteTypePrice = new BigDecimal("50");
        BigDecimal volume = new BigDecimal("5");
        BigDecimal fee = feeCalculationService.calculateDisposalFee(wasteTypePrice, volume);

        assertEquals(new BigDecimal("250.00"), fee);
    }

    @Test
    void testDisposalFeeWithDecimal() {
        BigDecimal wasteTypePrice = new BigDecimal("45.5");
        BigDecimal volume = new BigDecimal("3.5");
        BigDecimal fee = feeCalculationService.calculateDisposalFee(wasteTypePrice, volume);

        assertEquals(new BigDecimal("159.25"), fee);
    }

    @Test
    void testDisposalFeeNullValues() {
        BigDecimal fee = feeCalculationService.calculateDisposalFee(null, null);
        assertEquals(BigDecimal.ZERO, fee);

        fee = feeCalculationService.calculateDisposalFee(new BigDecimal("50"), null);
        assertEquals(BigDecimal.ZERO, fee);

        fee = feeCalculationService.calculateDisposalFee(null, new BigDecimal("5"));
        assertEquals(BigDecimal.ZERO, fee);
    }

    @Test
    void testHandlingFeeNoServices() {
        BigDecimal fee = feeCalculationService.calculateHandlingFee(false, 0);
        assertEquals(new BigDecimal("0.00"), fee);
    }

    @Test
    void testHandlingFeeForkliftOnly() {
        BigDecimal fee = feeCalculationService.calculateHandlingFee(true, 0);
        assertEquals(new BigDecimal("100.00"), fee);
    }

    @Test
    void testHandlingFeeContainersOnly() {
        BigDecimal fee = feeCalculationService.calculateHandlingFee(false, 3);
        assertEquals(new BigDecimal("150.00"), fee);
    }

    @Test
    void testHandlingFeeBothServices() {
        BigDecimal fee = feeCalculationService.calculateHandlingFee(true, 2);
        assertEquals(new BigDecimal("200.00"), fee);
    }

    @Test
    void testCalculateTotalFee() {
        BigDecimal distance = new BigDecimal("10");
        BigDecimal wasteTypePrice = new BigDecimal("50");
        BigDecimal volume = new BigDecimal("5");
        boolean forkliftRequired = true;
        int containerCount = 1;

        Map<String, BigDecimal> result = feeCalculationService.calculateTotalFee(
            distance, wasteTypePrice, volume, forkliftRequired, containerCount
        );

        assertNotNull(result);
        assertEquals(new BigDecimal("290.00"), result.get("transportFee"));
        assertEquals(new BigDecimal("250.00"), result.get("disposalFee"));
        assertEquals(new BigDecimal("150.00"), result.get("handlingFee"));
        assertEquals(new BigDecimal("690.00"), result.get("totalFee"));
    }

    @Test
    void testHaversineDistanceSamePoint() {
        BigDecimal lat1 = new BigDecimal("39.9042");
        BigDecimal lon1 = new BigDecimal("116.4074");
        BigDecimal lat2 = new BigDecimal("39.9042");
        BigDecimal lon2 = new BigDecimal("116.4074");

        BigDecimal distance = feeCalculationService.calculateDistance(lat1, lon1, lat2, lon2);

        assertEquals(new BigDecimal("0.00"), distance);
    }

    @Test
    void testHaversineDistanceBeijingToTianjin() {
        BigDecimal beijingLat = new BigDecimal("39.9042");
        BigDecimal beijingLon = new BigDecimal("116.4074");
        BigDecimal tianjinLat = new BigDecimal("39.3439");
        BigDecimal tianjinLon = new BigDecimal("117.3616");

        BigDecimal distance = feeCalculationService.calculateDistance(
            beijingLat, beijingLon, tianjinLat, tianjinLon
        );

        assertTrue(distance.compareTo(new BigDecimal("100")) > 0);
        assertTrue(distance.compareTo(new BigDecimal("150")) < 0);
    }

    @Test
    void testHaversineDistanceNullValues() {
        BigDecimal distance = feeCalculationService.calculateDistance(null, null, null, null);
        assertEquals(BigDecimal.ZERO, distance);
    }

    @Test
    void testFeeCalculationEdgeCases() {
        BigDecimal distance = BigDecimal.ZERO;
        BigDecimal wasteTypePrice = new BigDecimal("50");
        BigDecimal volume = new BigDecimal("1");

        Map<String, BigDecimal> result = feeCalculationService.calculateTotalFee(
            distance, wasteTypePrice, volume, false, 0
        );

        assertEquals(new BigDecimal("200.00"), result.get("transportFee"));
        assertEquals(new BigDecimal("50.00"), result.get("disposalFee"));
        assertEquals(new BigDecimal("0.00"), result.get("handlingFee"));
        assertEquals(new BigDecimal("250.00"), result.get("totalFee"));
    }

    @Test
    void testFeeCalculationWithZeroWasteTypePrice() {
        BigDecimal distance = new BigDecimal("5");
        BigDecimal wasteTypePrice = BigDecimal.ZERO;
        BigDecimal volume = new BigDecimal("10");

        Map<String, BigDecimal> result = feeCalculationService.calculateTotalFee(
            distance, wasteTypePrice, volume, false, 0
        );

        assertEquals(new BigDecimal("0.00"), result.get("disposalFee"));
        assertEquals(new BigDecimal("200.00"), result.get("totalFee"));
    }

    @Test
    void testMultipleContainerCount() {
        BigDecimal fee1 = feeCalculationService.calculateHandlingFee(false, 5);
        BigDecimal fee2 = feeCalculationService.calculateHandlingFee(false, 10);

        assertEquals(new BigDecimal("250.00"), fee1);
        assertEquals(new BigDecimal("500.00"), fee2);
    }
}
