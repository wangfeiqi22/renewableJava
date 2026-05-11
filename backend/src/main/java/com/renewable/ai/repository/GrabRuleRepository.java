package com.renewable.ai.repository;

import com.renewable.ai.entity.GrabRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface GrabRuleRepository extends JpaRepository<GrabRule, Long> {
    
    Optional<GrabRule> findByFleetId(Long fleetId);
    
    List<GrabRule> findByFleetIdAndEnabled(Long fleetId, Boolean enabled);
    
    boolean existsByFleetId(Long fleetId);
}
