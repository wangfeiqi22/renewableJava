package com.renewable.ai.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "biz_order_fee_details")
@Data
public class OrderFeeDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(name = "transport_fee", precision = 10, scale = 2)
    private BigDecimal transportFee = BigDecimal.ZERO;

    @Column(name = "disposal_fee", precision = 10, scale = 2)
    private BigDecimal disposalFee = BigDecimal.ZERO;

    @Column(name = "handling_fee", precision = 10, scale = 2)
    private BigDecimal handlingFee = BigDecimal.ZERO;

    @Column(name = "total_fee", precision = 10, scale = 2)
    private BigDecimal totalFee = BigDecimal.ZERO;

    @Column(name = "distance_km", precision = 10, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "estimated_volume", precision = 10, scale = 2)
    private BigDecimal estimatedVolume;

    @Column(name = "waste_type_price", precision = 10, scale = 2)
    private BigDecimal wasteTypePrice;

    @Column(name = "forklift_required")
    private Boolean forkliftRequired = false;

    @Column(name = "container_count")
    private Integer containerCount = 0;

    @Column(name = "fee_breakdown", columnDefinition = "jsonb")
    private String feeBreakdown;

    @Column(name = "is_confirmed")
    private Boolean isConfirmed = false;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "confirmed_by")
    private Long confirmedBy;

    @Column(name = "manual_adjustment", precision = 10, scale = 2)
    private BigDecimal manualAdjustment = BigDecimal.ZERO;

    @Column(name = "adjustment_reason")
    private String adjustmentReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
