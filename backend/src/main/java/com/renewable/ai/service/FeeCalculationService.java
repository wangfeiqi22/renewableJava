package com.renewable.ai.service;

import com.renewable.ai.entity.*;
import com.renewable.ai.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeeCalculationService {
    private final FeeConfigRepository feeConfigRepository;
    private final StationRepository stationRepository;
    private final StationWasteTypeRepository stationWasteTypeRepository;

    private static final String TRANSPORT_BASE_PRICE = "transport_base_price";
    private static final String TRANSPORT_BASE_KM = "transport_base_km";
    private static final String TRANSPORT_PRICE_PER_KM = "transport_price_per_km";
    private static final String FORKLIFT_FEE = "forklift_fee";
    private static final String CONTAINER_FEE = "container_fee";

    public BigDecimal calculateTransportFee(BigDecimal distanceKm) {
        BigDecimal basePrice = getConfigValue(TRANSPORT_BASE_PRICE, "200");
        BigDecimal baseKm = getConfigValue(TRANSPORT_BASE_KM, "5");
        BigDecimal pricePerKm = getConfigValue(TRANSPORT_PRICE_PER_KM, "18");

        if (distanceKm.compareTo(baseKm) <= 0) {
            return basePrice;
        }

        BigDecimal extraKm = distanceKm.subtract(baseKm);
        return basePrice.add(extraKm.multiply(pricePerKm)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateDisposalFee(BigDecimal wasteTypePrice, BigDecimal volume) {
        if (wasteTypePrice == null || volume == null) {
            return BigDecimal.ZERO;
        }
        return wasteTypePrice.multiply(volume).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateHandlingFee(boolean forkliftRequired, int containerCount) {
        BigDecimal forkliftFee = getConfigValue(FORKLIFT_FEE, "100");
        BigDecimal containerFee = getConfigValue(CONTAINER_FEE, "50");

        BigDecimal total = BigDecimal.ZERO;
        if (forkliftRequired) {
            total = total.add(forkliftFee);
        }
        total = total.add(containerFee.multiply(new BigDecimal(containerCount)));

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public Map<String, BigDecimal> calculateTotalFee(BigDecimal distanceKm, BigDecimal wasteTypePrice,
                                                    BigDecimal volume, boolean forkliftRequired, int containerCount) {
        Map<String, BigDecimal> result = new HashMap<>();

        BigDecimal transportFee = calculateTransportFee(distanceKm);
        BigDecimal disposalFee = calculateDisposalFee(wasteTypePrice, volume);
        BigDecimal handlingFee = calculateHandlingFee(forkliftRequired, containerCount);
        BigDecimal totalFee = transportFee.add(disposalFee).add(handlingFee);

        result.put("transportFee", transportFee);
        result.put("disposalFee", disposalFee);
        result.put("handlingFee", handlingFee);
        result.put("totalFee", totalFee);

        return result;
    }

    public BigDecimal calculateDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return BigDecimal.ZERO;
        }

        final double R = 6371.0;

        double lat1Rad = Math.toRadians(lat1.doubleValue());
        double lat2Rad = Math.toRadians(lat2.doubleValue());
        double deltaLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double deltaLon = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return new BigDecimal(R * c).setScale(2, RoundingMode.HALF_UP);
    }

    public FeeCalculationResult calculateOrderFee(BigDecimal pickupLat, BigDecimal pickupLon,
                                                   Long stationId, String wasteTypeCode,
                                                   BigDecimal estVolume, boolean forkliftRequired, int containerCount) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("清运站不存在"));

        if (station.getLat() == null || station.getLon() == null) {
            throw new RuntimeException("清运站坐标信息不完整");
        }

        BigDecimal distance = calculateDistance(pickupLat, pickupLon, station.getLat(), station.getLon());

        WasteType wasteType = wasteTypeRepository.findByCode(wasteTypeCode)
                .orElseThrow(() -> new RuntimeException("垃圾类型不存在"));

        StationWasteType stationWasteType = stationWasteTypeRepository
                .findByStationIdAndWasteTypeId(stationId, wasteType.getId())
                .orElseThrow(() -> new RuntimeException("该清运站不支持此垃圾类型"));

        Map<String, BigDecimal> fees = calculateTotalFee(distance, stationWasteType.getPricePerCubic(),
                estVolume, forkliftRequired, containerCount);

        return FeeCalculationResult.builder()
                .stationId(stationId)
                .stationName(station.getName())
                .distance(distance)
                .transportFee(fees.get("transportFee"))
                .disposalFee(fees.get("disposalFee"))
                .handlingFee(fees.get("handlingFee"))
                .totalFee(fees.get("totalFee"))
                .wasteTypePrice(stationWasteType.getPricePerCubic())
                .estimatedVolume(estVolume)
                .forkliftRequired(forkliftRequired)
                .containerCount(containerCount)
                .build();
    }

    private BigDecimal getConfigValue(String key, String defaultValue) {
        Optional<FeeConfig> config = feeConfigRepository.findByConfigKey(key);
        return config.map(c -> new BigDecimal(c.getConfigValue()))
                .orElse(new BigDecimal(defaultValue));
    }

    @lombok.Data
    @lombok.Builder
    public static class FeeCalculationResult {
        private Long stationId;
        private String stationName;
        private BigDecimal distance;
        private BigDecimal transportFee;
        private BigDecimal disposalFee;
        private BigDecimal handlingFee;
        private BigDecimal totalFee;
        private BigDecimal wasteTypePrice;
        private BigDecimal estimatedVolume;
        private boolean forkliftRequired;
        private int containerCount;
    }
}
