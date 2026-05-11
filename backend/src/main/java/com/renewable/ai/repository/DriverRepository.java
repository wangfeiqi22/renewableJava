package com.renewable.ai.repository;

import com.renewable.ai.entity.Driver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    
    Page<Driver> findByFleetId(Long fleetId, Pageable pageable);
    
    Page<Driver> findByFleetIdAndNameContaining(Long fleetId, String name, Pageable pageable);
    
    Page<Driver> findByFleetIdAndPhoneContaining(Long fleetId, String phone, Pageable pageable);
    
    Page<Driver> findByFleetIdAndStatus(Long fleetId, String status, Pageable pageable);
    
    @Query("SELECT d FROM Driver d WHERE d.fleetId = :fleetId AND " +
           "(:name IS NULL OR d.name LIKE %:name%) AND " +
           "(:phone IS NULL OR d.phone LIKE %:phone%) AND " +
           "(:status IS NULL OR d.status = :status)")
    Page<Driver> findByFleetIdWithFilters(
            @Param("fleetId") Long fleetId,
            @Param("name") String name,
            @Param("phone") String phone,
            @Param("status") String status,
            Pageable pageable);
    
    Optional<Driver> findByPhone(String phone);
    
    Optional<Driver> findByIdAndFleetId(Long id, Long fleetId);
    
    boolean existsByPhoneAndFleetIdNot(String phone, Long excludeFleetId);
    
    boolean existsByDriverLicenseNumber(String driverLicenseNumber);
}
