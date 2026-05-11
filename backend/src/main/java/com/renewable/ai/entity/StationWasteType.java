package com.renewable.ai.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "biz_station_waste_types", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"station_id", "waste_type_id"})
})
@Data
public class StationWasteType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "station_id", nullable = false)
    private Long stationId;

    @Column(name = "waste_type_id", nullable = false)
    private Long wasteTypeId;

    @Column(name = "price_per_cubic", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerCubic;

    private Integer status = 1;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waste_type_id", insertable = false, updatable = false)
    private WasteType wasteType;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
