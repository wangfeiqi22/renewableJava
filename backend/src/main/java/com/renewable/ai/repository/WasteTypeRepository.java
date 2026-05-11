package com.renewable.ai.repository;

import com.renewable.ai.entity.WasteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WasteTypeRepository extends JpaRepository<WasteType, Long> {
    Optional<WasteType> findByCode(String code);
    List<WasteType> findByStatus(Integer status);
    List<WasteType> findByCategory(String category);
}
