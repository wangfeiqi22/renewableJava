package com.renewable.ai.repository;

import com.renewable.ai.entity.DriverChangeLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverChangeLogRepository extends JpaRepository<DriverChangeLog, Long> {
    
    Page<DriverChangeLog> findByDriverIdOrderByChangeTimeDesc(Long driverId, Pageable pageable);
}
