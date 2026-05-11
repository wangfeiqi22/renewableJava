package com.renewable.ai.repository;

import com.renewable.ai.entity.StationAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StationAddressRepository extends JpaRepository<StationAddress, Long> {
    Optional<StationAddress> findByStationId(Long stationId);
}
