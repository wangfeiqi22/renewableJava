package com.renewable.ai.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "biz_station_addresses")
@Data
public class StationAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "station_id", nullable = false, unique = true)
    private Long stationId;

    private String province;
    private String city;
    private String district;
    private String street;
    private String doorNo;
    private String fullAddress;

    @Column(precision = 9, scale = 6)
    private BigDecimal lat;

    @Column(precision = 9, scale = 6)
    private BigDecimal lon;

    @Column(name = "map_provider")
    private String mapProvider;

    @Column(name = "map_place_id")
    private String mapPlaceId;

    private Boolean isVerified = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", insertable = false, updatable = false)
    private Station station;

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
