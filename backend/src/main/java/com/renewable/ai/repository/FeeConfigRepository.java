package com.renewable.ai.repository;

import com.renewable.ai.entity.FeeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FeeConfigRepository extends JpaRepository<FeeConfig, Long> {
    Optional<FeeConfig> findByConfigKey(String configKey);
}
