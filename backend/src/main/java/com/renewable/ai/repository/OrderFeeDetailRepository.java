package com.renewable.ai.repository;

import com.renewable.ai.entity.OrderFeeDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OrderFeeDetailRepository extends JpaRepository<OrderFeeDetail, Long> {
    Optional<OrderFeeDetail> findByOrderId(Long orderId);
}
