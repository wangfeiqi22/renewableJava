package com.renewable.ai.service;

import com.renewable.ai.dto.DriverCreateDTO;
import com.renewable.ai.dto.DriverUpdateDTO;
import com.renewable.ai.dto.DriverVO;
import com.renewable.ai.entity.Driver;
import com.renewable.ai.entity.DriverChangeLog;
import com.renewable.ai.entity.Fleet;
import com.renewable.ai.exception.DriverException;
import com.renewable.ai.repository.DriverChangeLogRepository;
import com.renewable.ai.repository.DriverRepository;
import com.renewable.ai.repository.FleetRepository;
import com.renewable.ai.repository.OrderRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DriverService {
    
    @Autowired
    private DriverRepository driverRepository;
    
    @Autowired
    private DriverChangeLogRepository changeLogRepository;
    
    @Autowired
    private FleetRepository fleetRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Transactional
    public DriverVO createDriver(DriverCreateDTO dto, Long fleetId, Long operatorId, String operatorName) {
        if (driverRepository.findByPhone(dto.getPhone()).isPresent()) {
            throw DriverException.phoneAlreadyExists(dto.getPhone());
        }
        
        if (dto.getDriverLicenseNumber() != null && !isValidLicenseFormat(dto.getDriverLicenseNumber())) {
            throw DriverException.invalidLicenseFormat();
        }
        
        Driver driver = new Driver();
        BeanUtils.copyProperties(dto, driver);
        driver.setFleetId(fleetId);
        driver.setStatus("active");
        
        Driver savedDriver = driverRepository.save(driver);
        
        logChange(savedDriver.getId(), "CREATE", null, null, 
                  "创建司机", operatorId, operatorName);
        
        return toVO(savedDriver);
    }
    
    public Page<DriverVO> getDrivers(Long fleetId, String name, String phone, 
                                    String status, Pageable pageable) {
        return driverRepository.findByFleetIdWithFilters(fleetId, name, phone, status, pageable)
                .map(this::toVO);
    }
    
    public DriverVO getDriverById(Long id, Long fleetId) {
        Driver driver = driverRepository.findByIdAndFleetId(id, fleetId)
                .orElseThrow(() -> DriverException.driverNotFound(id));
        return toVO(driver);
    }
    
    @Transactional
    public DriverVO updateDriver(Long id, DriverUpdateDTO dto, 
                                 Long fleetId, Long operatorId, String operatorName) {
        Driver driver = driverRepository.findByIdAndFleetId(id, fleetId)
                .orElseThrow(() -> DriverException.driverNotFound(id));
        
        if (dto.getPhone() != null && !dto.getPhone().equals(driver.getPhone())) {
            if (driverRepository.findByPhone(dto.getPhone()).isPresent()) {
                throw DriverException.phoneAlreadyExists(dto.getPhone());
            }
        }
        
        if (dto.getDriverLicenseNumber() != null && 
            !dto.getDriverLicenseNumber().equals(driver.getDriverLicenseNumber()) &&
            !isValidLicenseFormat(dto.getDriverLicenseNumber())) {
            throw DriverException.invalidLicenseFormat();
        }
        
        List<String> changedFields = detectChanges(driver, dto);
        
        BeanUtils.copyProperties(dto, driver, getNullPropertyNames(dto));
        
        Driver updatedDriver = driverRepository.save(driver);
        
        for (String field : changedFields) {
            logFieldChange(id, field, getOldValue(driver, field), 
                          getNewValue(dto, field), operatorId, operatorName, null);
        }
        
        return toVO(updatedDriver);
    }
    
    @Transactional
    public void deleteDriver(Long id, Long fleetId, Long operatorId, String operatorName) {
        Driver driver = driverRepository.findByIdAndFleetId(id, fleetId)
                .orElseThrow(() -> DriverException.driverNotFound(id));
        
        long pendingOrders = orderRepository.countByDriverIdAndStatusNotIn(id, List.of(3, 4));
        if (pendingOrders > 0) {
            throw DriverException.hasPendingOrders();
        }
        
        logChange(id, "DELETE", null, null, 
                  "删除司机: " + driver.getName(), operatorId, operatorName);
        
        driverRepository.delete(driver);
    }
    
    private void logChange(Long driverId, String changeType, String fieldName,
                          String newValue, String reason, Long operatorId, String operatorName) {
        DriverChangeLog log = new DriverChangeLog();
        log.setDriverId(driverId);
        log.setChangeType(changeType);
        log.setFieldName(fieldName);
        log.setNewValue(newValue);
        log.setChangeReason(reason);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        changeLogRepository.save(log);
    }
    
    private void logFieldChange(Long driverId, String field, String oldVal, 
                               String newVal, Long operatorId, String operatorName, String reason) {
        DriverChangeLog log = new DriverChangeLog();
        log.setDriverId(driverId);
        log.setChangeType("UPDATE");
        log.setFieldName(field);
        log.setOldValue(oldVal);
        log.setNewValue(newVal);
        log.setChangeReason(reason);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        changeLogRepository.save(log);
    }
    
    private boolean isValidLicenseFormat(String license) {
        return license != null && license.matches("^[0-9A-Z]{6,20}$");
    }
    
    private DriverVO toVO(Driver driver) {
        DriverVO vo = new DriverVO();
        BeanUtils.copyProperties(driver, vo);
        
        fleetRepository.findById(driver.getFleetId())
                .ifPresent(fleet -> vo.setFleetName(fleet.getName()));
        
        return vo;
    }
    
    private List<String> detectChanges(Driver driver, DriverUpdateDTO dto) {
        return List.of("name", "phone", "idNumber", "driverLicenseNumber", 
                       "driverLicenseLevel", "vehiclePlate", "vehicleType", 
                       "vehicleCapacity", "status", "hireDate")
                .stream()
                .filter(field -> hasChanged(driver, dto, field))
                .collect(Collectors.toList());
    }
    
    private boolean hasChanged(Driver driver, DriverUpdateDTO dto, String field) {
        Object oldVal = getOldValue(driver, field);
        Object newVal = getNewValue(dto, field);
        return newVal != null && !newVal.equals(oldVal);
    }
    
    private String getOldValue(Driver driver, String field) {
        switch (field) {
            case "name": return driver.getName();
            case "phone": return driver.getPhone();
            case "idNumber": return driver.getIdNumber();
            case "driverLicenseNumber": return driver.getDriverLicenseNumber();
            case "driverLicenseLevel": return driver.getDriverLicenseLevel();
            case "vehiclePlate": return driver.getVehiclePlate();
            case "vehicleType": return driver.getVehicleType();
            case "vehicleCapacity": return driver.getVehicleCapacity() != null ? 
                                     driver.getVehicleCapacity().toString() : null;
            case "status": return driver.getStatus();
            case "hireDate": return driver.getHireDate() != null ? 
                              driver.getHireDate().toString() : null;
            default: return null;
        }
    }
    
    private String getNewValue(DriverUpdateDTO dto, String field) {
        switch (field) {
            case "name": return dto.getName();
            case "phone": return dto.getPhone();
            case "idNumber": return dto.getIdNumber();
            case "driverLicenseNumber": return dto.getDriverLicenseNumber();
            case "driverLicenseLevel": return dto.getDriverLicenseLevel();
            case "vehiclePlate": return dto.getVehiclePlate();
            case "vehicleType": return dto.getVehicleType();
            case "vehicleCapacity": return dto.getVehicleCapacity() != null ? 
                                     dto.getVehicleCapacity().toString() : null;
            case "status": return dto.getStatus();
            case "hireDate": return dto.getHireDate() != null ? 
                              dto.getHireDate().toString() : null;
            default: return null;
        }
    }
    
    private String[] getNullPropertyNames(Object dto) {
        return java.util.Arrays.stream(dto.getClass().getDeclaredFields())
                .filter(field -> {
                    field.setAccessible(true);
                    try {
                        return field.get(dto) == null;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .map(java.lang.reflect.Field::getName)
                .toArray(String[]::new);
    }
    
    @Transactional
    public DriverVO updateDriverStatus(Long driverId, String status, Long operatorId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> DriverException.driverNotFound(driverId));
        
        String oldStatus = driver.getStatus();
        driver.setStatus(status);
        driverRepository.save(driver);
        
        DriverChangeLog log = new DriverChangeLog();
        log.setDriverId(driverId);
        log.setChangeType("STATUS_CHANGE");
        log.setFieldName("status");
        log.setOldValue(oldStatus);
        log.setNewValue(status);
        log.setOperatorId(operatorId);
        changeLogRepository.save(log);
        
        return DriverVO.fromEntity(driver);
    }
}
