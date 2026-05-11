package com.renewable.ai.service;

import com.renewable.ai.entity.Station;
import com.renewable.ai.entity.StationWasteType;
import com.renewable.ai.entity.WasteType;
import com.renewable.ai.repository.StationRepository;
import com.renewable.ai.repository.StationWasteTypeRepository;
import com.renewable.ai.repository.WasteTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StationMatchServiceTest {

    @Mock
    private StationRepository stationRepository;

    @Mock
    private StationWasteTypeRepository stationWasteTypeRepository;

    @Mock
    private WasteTypeRepository wasteTypeRepository;

    @Mock
    private FeeCalculationService feeCalculationService;

    @InjectMocks
    private StationMatchService stationMatchService;

    private WasteType wasteType;
    private Station station1;
    private Station station2;
    private Station station3;

    @BeforeEach
    void setUp() {
        wasteType = new WasteType();
        wasteType.setId(1L);
        wasteType.setCode("CONSTRUCTION");
        wasteType.setName("建筑垃圾");

        station1 = new Station();
        station1.setId(1L);
        station1.setName("清运站A");
        station1.setLat(new BigDecimal("39.9042"));
        station1.setLon(new BigDecimal("116.4074"));

        station2 = new Station();
        station2.setId(2L);
        station2.setName("清运站B");
        station2.setLat(new BigDecimal("39.9500"));
        station2.setLon(new BigDecimal("116.4500"));

        station3 = new Station();
        station3.setId(3L);
        station3.setName("清运站C");
        station3.setLat(new BigDecimal("40.0000"));
        station3.setLon(new BigDecimal("116.5000"));
    }

    @Test
    void testFindBestStationsSingleMatch() {
        when(wasteTypeRepository.findByCode("CONSTRUCTION")).thenReturn(Optional.of(wasteType));

        StationWasteType swt = new StationWasteType();
        swt.setStationId(1L);
        swt.setPricePerCubic(new BigDecimal("50"));

        when(stationWasteTypeRepository.findActiveByWasteTypeId(1L))
            .thenReturn(Arrays.asList(swt));

        when(stationRepository.findAllById(any())).thenReturn(Arrays.asList(station1));

        when(feeCalculationService.calculateDistance(any(), any(), any(), any()))
            .thenReturn(new BigDecimal("10.5"));

        List<StationMatchService.StationMatchVO> result = stationMatchService.findBestStations(
            "CONSTRUCTION", new BigDecimal("39.9042"), new BigDecimal("116.4074"), 5
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("清运站A", result.get(0).getStationName());
        assertEquals(new BigDecimal("10.5"), result.get(0).getDistance());
    }

    @Test
    void testFindBestStationsMultipleMatchesSortedByDistance() {
        when(wasteTypeRepository.findByCode("CONSTRUCTION")).thenReturn(Optional.of(wasteType));

        StationWasteType swt1 = new StationWasteType();
        swt1.setStationId(1L);
        swt1.setPricePerCubic(new BigDecimal("50"));

        StationWasteType swt2 = new StationWasteType();
        swt2.setStationId(2L);
        swt2.setPricePerCubic(new BigDecimal("45"));

        StationWasteType swt3 = new StationWasteType();
        swt3.setStationId(3L);
        swt3.setPricePerCubic(new BigDecimal("55"));

        when(stationWasteTypeRepository.findActiveByWasteTypeId(1L))
            .thenReturn(Arrays.asList(swt1, swt2, swt3));

        when(stationRepository.findAllById(any()))
            .thenReturn(Arrays.asList(station1, station2, station3));

        when(feeCalculationService.calculateDistance(
            new BigDecimal("39.9042"), new BigDecimal("116.4074"),
            new BigDecimal("39.9042"), new BigDecimal("116.4074")
        )).thenReturn(new BigDecimal("0"));

        when(feeCalculationService.calculateDistance(
            new BigDecimal("39.9042"), new BigDecimal("116.4074"),
            new BigDecimal("39.9500"), new BigDecimal("116.4500")
        )).thenReturn(new BigDecimal("5"));

        when(feeCalculationService.calculateDistance(
            new BigDecimal("39.9042"), new BigDecimal("116.4074"),
            new BigDecimal("40.0000"), new BigDecimal("116.5000")
        )).thenReturn(new BigDecimal("12"));

        List<StationMatchService.StationMatchVO> result = stationMatchService.findBestStations(
            "CONSTRUCTION", new BigDecimal("39.9042"), new BigDecimal("116.4074"), 3
        );

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("清运站A", result.get(0).getStationName());
        assertEquals("清运站B", result.get(1).getStationName());
        assertEquals("清运站C", result.get(2).getStationName());
        assertEquals(new BigDecimal("0"), result.get(0).getDistance());
        assertEquals(new BigDecimal("5"), result.get(1).getDistance());
        assertEquals(new BigDecimal("12"), result.get(2).getDistance());
    }

    @Test
    void testFindBestStationsWithLimit() {
        when(wasteTypeRepository.findByCode("CONSTRUCTION")).thenReturn(Optional.of(wasteType));

        StationWasteType swt1 = new StationWasteType();
        swt1.setStationId(1L);
        swt1.setPricePerCubic(new BigDecimal("50"));

        StationWasteType swt2 = new StationWasteType();
        swt2.setStationId(2L);
        swt2.setPricePerCubic(new BigDecimal("45"));

        StationWasteType swt3 = new StationWasteType();
        swt3.setStationId(3L);
        swt3.setPricePerCubic(new BigDecimal("55"));

        when(stationWasteTypeRepository.findActiveByWasteTypeId(1L))
            .thenReturn(Arrays.asList(swt1, swt2, swt3));

        when(stationRepository.findAllById(any()))
            .thenReturn(Arrays.asList(station1, station2, station3));

        when(feeCalculationService.calculateDistance(any(), any(), any(), any()))
            .thenReturn(new BigDecimal("5"));

        List<StationMatchService.StationMatchVO> result = stationMatchService.findBestStations(
            "CONSTRUCTION", new BigDecimal("39.9042"), new BigDecimal("116.4074"), 2
        );

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testFindBestStationsNoMatch() {
        when(wasteTypeRepository.findByCode("CONSTRUCTION")).thenReturn(Optional.of(wasteType));
        when(stationWasteTypeRepository.findActiveByWasteTypeId(1L)).thenReturn(Collections.emptyList());

        List<StationMatchService.StationMatchVO> result = stationMatchService.findBestStations(
            "CONSTRUCTION", new BigDecimal("39.9042"), new BigDecimal("116.4074"), 5
        );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindBestStationsInvalidWasteType() {
        when(wasteTypeRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            stationMatchService.findBestStations("INVALID", new BigDecimal("39.9042"), new BigDecimal("116.4074"), 5);
        });
    }

    @Test
    void testMatchSingleStationSuccess() {
        when(wasteTypeRepository.findByCode("CONSTRUCTION")).thenReturn(Optional.of(wasteType));

        StationWasteType swt = new StationWasteType();
        swt.setStationId(1L);
        swt.setPricePerCubic(new BigDecimal("50"));

        when(stationWasteTypeRepository.findActiveByWasteTypeId(1L))
            .thenReturn(Arrays.asList(swt));

        when(stationRepository.findAllById(any())).thenReturn(Arrays.asList(station1));

        when(feeCalculationService.calculateDistance(any(), any(), any(), any()))
            .thenReturn(new BigDecimal("10"));

        StationMatchService.StationMatchVO result = stationMatchService.matchSingleStation(
            "CONSTRUCTION", new BigDecimal("39.9042"), new BigDecimal("116.4074")
        );

        assertNotNull(result);
        assertEquals("清运站A", result.getStationName());
        assertEquals(new BigDecimal("10"), result.getDistance());
    }

    @Test
    void testMatchSingleStationNoStationAvailable() {
        when(wasteTypeRepository.findByCode("CONSTRUCTION")).thenReturn(Optional.of(wasteType));
        when(stationWasteTypeRepository.findActiveByWasteTypeId(1L)).thenReturn(Collections.emptyList());

        assertThrows(RuntimeException.class, () -> {
            stationMatchService.matchSingleStation("CONSTRUCTION", new BigDecimal("39.9042"), new BigDecimal("116.4074"));
        });
    }

    @Test
    void testStationWithNullCoordinates() {
        when(wasteTypeRepository.findByCode("CONSTRUCTION")).thenReturn(Optional.of(wasteType));

        Station stationWithNullCoords = new Station();
        stationWithNullCoords.setId(4L);
        stationWithNullCoords.setName("清运站D");
        stationWithNullCoords.setLat(null);
        stationWithNullCoords.setLon(null);

        StationWasteType swt = new StationWasteType();
        swt.setStationId(1L);
        swt.setPricePerCubic(new BigDecimal("50"));

        when(stationWasteTypeRepository.findActiveByWasteTypeId(1L))
            .thenReturn(Arrays.asList(swt));

        when(stationRepository.findAllById(any()))
            .thenReturn(Arrays.asList(stationWithNullCoords));

        List<StationMatchService.StationMatchVO> result = stationMatchService.findBestStations(
            "CONSTRUCTION", new BigDecimal("39.9042"), new BigDecimal("116.4074"), 5
        );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
