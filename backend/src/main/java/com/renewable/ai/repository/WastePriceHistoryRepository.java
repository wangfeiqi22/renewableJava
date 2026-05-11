package com.renewable.ai.repository;

import com.renewable.ai.entity.WastePriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WastePriceHistoryRepository extends JpaRepository<WastePriceHistory, Long> {
    List<WastePriceHistory> findByStationWasteTypeIdOrderByChangedAtDesc(Long stationWasteTypeId);
}
