package com.renewable.ai.repository;

import com.renewable.ai.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StationRepository extends JpaRepository<Station, Long> {
    List<Station> findByType(Integer type);
    List<Station> findByManagerId(Long managerId);
}
