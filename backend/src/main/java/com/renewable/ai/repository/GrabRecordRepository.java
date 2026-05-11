package com.renewable.ai.repository;

import com.renewable.ai.entity.GrabRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GrabRecordRepository extends JpaRepository<GrabRecord, Long> {
    
    Page<GrabRecord> findByDriverId(Long driverId, Pageable pageable);
    
    Page<GrabRecord> findByOrderId(Long orderId, Pageable pageable);
    
    @Query("SELECT COUNT(g) FROM GrabRecord g WHERE g.driverId = :driverId " +
           "AND g.grabTime BETWEEN :startTime AND :endTime")
    long countByDriverIdAndGrabTimeBetween(
            @Param("driverId") Long driverId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
    
    @Query("SELECT COUNT(g) FROM GrabRecord g WHERE g.driverId = :driverId " +
           "AND g.grabTime BETWEEN :startTime AND :endTime AND g.success = true")
    long countSuccessByDriverIdAndGrabTimeBetween(
            @Param("driverId") Long driverId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
    
    List<GrabRecord> findByOrderIdAndSuccessOrderByPositionAsc(Long orderId, Boolean success);
    
    @Query("SELECT MAX(g.position) FROM GrabRecord g WHERE g.orderId = :orderId")
    Integer findMaxPositionByOrderId(@Param("orderId") Long orderId);
    
    List<GrabRecord> findByOrderIdAndIsWinner(Long orderId, Boolean isWinner);
    
    @Query("SELECT g FROM GrabRecord g WHERE g.driverId = :driverId " +
           "AND g.grabTime >= :since ORDER BY g.grabTime DESC")
    Page<GrabRecord> findRecentGrabsByDriver(
            @Param("driverId") Long driverId,
            @Param("since") LocalDateTime since,
            Pageable pageable);
}
