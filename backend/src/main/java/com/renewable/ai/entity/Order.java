package com.renewable.ai.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "biz_orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", unique = true, nullable = false)
    private String orderNo;

    @Column(name = "creator_id")
    private Long creatorId;

    private Integer type; // 1: Smart, 2: Property, 3: Street, 4: VIP, 5: Individual Self

    private Integer status = 10; // 10: Pending, 20: Assigned, 30: Loading, 40: Transit, 50: Arrived, 60: Done, 90: Cancelled

    @Column(name = "source_type")
    private String sourceType;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "waste_type")
    private String wasteType;

    @Column(name = "waste_desc")
    private String wasteDesc;

    @Column(name = "est_weight")
    private BigDecimal estWeight;

    @Column(name = "est_volume")
    private BigDecimal estVolume;

    @Column(name = "pickup_address", nullable = false)
    private String pickupAddress;

    @Column(name = "pickup_lat")
    private BigDecimal pickupLat;

    @Column(name = "pickup_lon")
    private BigDecimal pickupLon;

    @Column(name = "station_id")
    private Long stationId;

    @Column(name = "fleet_id")
    private Long fleetId;

    @Column(name = "driver_id")
    private Long driverId;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "placement_status")
    private String placementStatus;

    @Column(name = "rental_days")
    private Integer rentalDays;

    @Column(name = "additional_services")
    private String additionalServices;

    @Column(name = "commission")
    private BigDecimal commission;

    @Column(name = "matched_station_id")
    private Long matchedStationId;

    @Column(name = "match_distance_km", precision = 10, scale = 2)
    private BigDecimal matchDistanceKm;

    @Column(name = "match_algorithm")
    private String matchAlgorithm;

    @Column(name = "fee_calculation_status")
    private String feeCalculationStatus = "PENDING";

    @Column(name = "pickup_address_province")
    private String pickupAddressProvince;

    @Column(name = "pickup_address_city")
    private String pickupAddressCity;

    @Column(name = "pickup_address_district")
    private String pickupAddressDistrict;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    
    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
    
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    
    public String getWasteType() { return wasteType; }
    public void setWasteType(String wasteType) { this.wasteType = wasteType; }
    
    public String getWasteDesc() { return wasteDesc; }
    public void setWasteDesc(String wasteDesc) { this.wasteDesc = wasteDesc; }
    
    public BigDecimal getEstWeight() { return estWeight; }
    public void setEstWeight(BigDecimal estWeight) { this.estWeight = estWeight; }
    
    public BigDecimal getEstVolume() { return estVolume; }
    public void setEstVolume(BigDecimal estVolume) { this.estVolume = estVolume; }
    
    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }
    
    public BigDecimal getPickupLat() { return pickupLat; }
    public void setPickupLat(BigDecimal pickupLat) { this.pickupLat = pickupLat; }
    
    public BigDecimal getPickupLon() { return pickupLon; }
    public void setPickupLon(BigDecimal pickupLon) { this.pickupLon = pickupLon; }
    
    public Long getStationId() { return stationId; }
    public void setStationId(Long stationId) { this.stationId = stationId; }
    
    public Long getFleetId() { return fleetId; }
    public void setFleetId(Long fleetId) { this.fleetId = fleetId; }
    
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
    
    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getPlacementStatus() { return placementStatus; }
    public void setPlacementStatus(String placementStatus) { this.placementStatus = placementStatus; }
    
    public Integer getRentalDays() { return rentalDays; }
    public void setRentalDays(Integer rentalDays) { this.rentalDays = rentalDays; }
    
    public String getAdditionalServices() { return additionalServices; }
    public void setAdditionalServices(String additionalServices) { this.additionalServices = additionalServices; }
    
    public BigDecimal getCommission() { return commission; }
    public void setCommission(BigDecimal commission) { this.commission = commission; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
