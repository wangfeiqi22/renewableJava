package com.renewable.ai.repository;

import com.renewable.ai.entity.StationWasteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StationWasteTypeRepository extends JpaRepository<StationWasteType, Long> {
    List<StationWasteType> findByStationId(Long stationId);

    List<StationWasteType> findByStationIdAndStatus(Long stationId, Integer status);

    Optional<StationWasteType> findByStationIdAndWasteTypeId(Long stationId, Long wasteTypeId);

    @Query("SELECT swt FROM StationWasteType swt JOIN FETCH swt.wasteType WHERE swt.stationId = :stationId AND swt.status = 1")
    List<StationWasteType> findActiveByStationId(@Param("stationId") Long stationId);

    @Query("SELECT swt FROM StationWasteType swt JOIN FETCH swt.wasteType WHERE swt.wasteTypeId = :wasteTypeId AND swt.status = 1")
    List<StationWasteType> findActiveByWasteTypeId(@Param("wasteTypeId") Long wasteTypeId);

    @Query("SELECT DISTINCT swt.stationId FROM StationWasteType swt WHERE swt.wasteTypeId = :wasteTypeId AND swt.status = 1")
    List<Long> findStationIdsByWasteTypeId(@Param("wasteTypeId") Long wasteTypeId);
}
