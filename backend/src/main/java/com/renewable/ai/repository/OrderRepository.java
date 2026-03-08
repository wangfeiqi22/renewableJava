package com.renewable.ai.repository;

import com.renewable.ai.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCreatorId(Long creatorId);
    List<Order> findByCreatorIdOrDriverId(Long creatorId, Long driverId);
    List<Order> findByStatus(Integer status);
}
