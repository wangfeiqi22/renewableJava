package com.renewable.ai.repository;

import com.renewable.ai.entity.DispatchLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DispatchLogRepository extends JpaRepository<DispatchLog, Long> {
    
    Page<DispatchLog> findByOrderId(Long orderId, Pageable pageable);
    
    Page<DispatchLog> findByOperatorId(Long operatorId, Pageable pageable);
    
    @Query("SELECT d FROM DispatchLog d WHERE d.orderId IN :orderIds ORDER BY d.actionTime DESC")
    List<DispatchLog> findByOrderIds(@Param("orderIds") List<Long> orderIds);
    
    @Query("SELECT d FROM DispatchLog d WHERE d.actionTime BETWEEN :startTime AND :endTime ORDER BY d.actionTime DESC")
    Page<DispatchLog> findByActionTimeBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);
    
    @Query("SELECT d FROM DispatchLog d WHERE d.actionType = :actionType ORDER BY d.actionTime DESC")
    Page<DispatchLog> findByActionType(@Param("actionType") DispatchLog.ActionType actionType, Pageable pageable);
}
