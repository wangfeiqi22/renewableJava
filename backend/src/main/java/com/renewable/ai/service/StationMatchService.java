package com.renewable.ai.service;

import com.renewable.ai.entity.*;
import com.renewable.ai.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StationMatchService {
    private final StationRepository stationRepository;
    private final StationWasteTypeRepository stationWasteTypeRepository;
    private final WasteTypeRepository wasteTypeRepository;
    private final FeeCalculationService feeCalculationService;

    public List<StationMatchVO> findBestStations(String wasteTypeCode, BigDecimal lat, BigDecimal lon, int limit) {
        WasteType wasteType = wasteTypeRepository.findByCode(wasteTypeCode)
                .orElseThrow(() -> new RuntimeException("垃圾类型不存在"));

        List<StationWasteType> stationWasteTypes = stationWasteTypeRepository.findActiveByWasteTypeId(wasteType.getId());

        if (stationWasteTypes.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> stationIds = stationWasteTypes.stream()
                .map(StationWasteType::getStationId)
                .distinct()
                .collect(Collectors.toList());

        List<Station> stations = stationRepository.findAllById(stationIds);

        List<StationMatchVO> matches = new ArrayList<>();
        for (Station station : stations) {
            if (station.getLat() != null && station.getLon() != null) {
                BigDecimal distance = feeCalculationService.calculateDistance(lat, lon, station.getLat(), station.getLon());

                StationWasteType swt = stationWasteTypes.stream()
                        .filter(s -> s.getStationId().equals(station.getId()))
                        .findFirst()
                        .orElse(null);

                matches.add(StationMatchVO.builder()
                        .stationId(station.getId())
                        .stationName(station.getName())
                        .stationAddress(station.getAddress())
                        .distance(distance)
                        .wasteTypePrice(swt != null ? swt.getPricePerCubic() : null)
                        .build());
            }
        }

        matches.sort(Comparator.comparing(StationMatchVO::getDistance));

        return matches.stream().limit(limit).collect(Collectors.toList());
    }

    public StationMatchVO matchSingleStation(String wasteTypeCode, BigDecimal lat, BigDecimal lon) {
        List<StationMatchVO> matches = findBestStations(wasteTypeCode, lat, lon, 1);
        if (matches.isEmpty()) {
            throw new RuntimeException("未找到可接收该垃圾类型的清运站");
        }
        return matches.get(0);
    }

    @lombok.Data
    @lombok.Builder
    public static class StationMatchVO {
        private Long stationId;
        private String stationName;
        private String stationAddress;
        private BigDecimal distance;
        private BigDecimal wasteTypePrice;
    }
}
