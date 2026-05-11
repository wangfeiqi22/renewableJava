package com.renewable.ai.service;

import com.renewable.ai.entity.*;
import com.renewable.ai.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StationWasteTypeService {
    private final StationWasteTypeRepository stationWasteTypeRepository;
    private final WasteTypeRepository wasteTypeRepository;
    private final WastePriceHistoryRepository priceHistoryRepository;
    private final StationRepository stationRepository;

    public List<WasteType> getAllActiveWasteTypes() {
        return wasteTypeRepository.findByStatus(1);
    }

    public List<StationWasteType> getStationWasteTypes(Long stationId) {
        return stationWasteTypeRepository.findActiveByStationId(stationId);
    }

    @Transactional
    public StationWasteType addWasteTypeToStation(Long stationId, Long wasteTypeId, BigDecimal price) {
        if (stationWasteTypeRepository.findByStationIdAndWasteTypeId(stationId, wasteTypeId).isPresent()) {
            throw new RuntimeException("该垃圾类型已配置");
        }

        Optional<Station> stationOpt = stationRepository.findById(stationId);
        Optional<WasteType> wasteTypeOpt = wasteTypeRepository.findById(wasteTypeId);

        if (stationOpt.isEmpty() || wasteTypeOpt.isEmpty()) {
            throw new RuntimeException("清运站或垃圾类型不存在");
        }

        StationWasteType stationWasteType = new StationWasteType();
        stationWasteType.setStationId(stationId);
        stationWasteType.setWasteTypeId(wasteTypeId);
        stationWasteType.setPricePerCubic(price);
        stationWasteType.setStatus(1);

        return stationWasteTypeRepository.save(stationWasteType);
    }

    @Transactional
    public StationWasteType updatePrice(Long stationWasteTypeId, BigDecimal newPrice, Long changedBy, String reason) {
        StationWasteType stationWasteType = stationWasteTypeRepository.findById(stationWasteTypeId)
                .orElseThrow(() -> new RuntimeException("配置不存在"));

        WastePriceHistory history = new WastePriceHistory();
        history.setStationWasteTypeId(stationWasteTypeId);
        history.setOldPrice(stationWasteType.getPricePerCubic());
        history.setNewPrice(newPrice);
        history.setChangedBy(changedBy);
        history.setChangeReason(reason);
        priceHistoryRepository.save(history);

        stationWasteType.setPricePerCubic(newPrice);
        return stationWasteTypeRepository.save(stationWasteType);
    }

    @Transactional
    public void removeWasteTypeFromStation(Long stationWasteTypeId) {
        stationWasteTypeRepository.deleteById(stationWasteTypeId);
    }

    public List<WastePriceHistory> getPriceHistory(Long stationWasteTypeId) {
        return priceHistoryRepository.findByStationWasteTypeIdOrderByChangedAtDesc(stationWasteTypeId);
    }
}
