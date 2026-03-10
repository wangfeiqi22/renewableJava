package com.renewable.ai.repository;

import com.renewable.ai.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCreatorId(Long creatorId);
    List<Order> findByCreatorIdOrDriverId(Long creatorId, Long driverId);
    List<Order> findByStatus(Integer status);

    @org.springframework.data.jpa.repository.Query("SELECT o FROM Order o WHERE (o.driverId = :driverId OR o.creatorId = :driverId) AND o.status IN :statuses AND o.updatedAt >= :startDate AND o.updatedAt <= :endDate")
    org.springframework.data.domain.Page<Order> findHistoryTasks(
        @org.springframework.data.repository.query.Param("driverId") Long driverId,
        @org.springframework.data.repository.query.Param("statuses") List<Integer> statuses,
        @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate,
        @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate,
        org.springframework.data.domain.Pageable pageable
    );
}
