package com.renewable.ai.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "biz_order_photos")
@Data
public class OrderPhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "node_type", nullable = false)
    private String nodeType; // create, load, station_entry, unload

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "gps_lat")
    private BigDecimal gpsLat;

    @Column(name = "gps_lon")
    private BigDecimal gpsLon;

    @Column(name = "taken_at")
    private LocalDateTime takenAt;

    @Column(name = "is_watermarked")
    private Boolean isWatermarked = false;

    @Column(name = "exif_data", length = 1000)
    private String exifData;

    @PrePersist
    protected void onCreate() {
        if (takenAt == null) {
            takenAt = LocalDateTime.now();
        }
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public String getNodeType() { return nodeType; }
    public void setNodeType(String nodeType) { this.nodeType = nodeType; }
    
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    
    public BigDecimal getGpsLat() { return gpsLat; }
    public void setGpsLat(BigDecimal gpsLat) { this.gpsLat = gpsLat; }
    
    public BigDecimal getGpsLon() { return gpsLon; }
    public void setGpsLon(BigDecimal gpsLon) { this.gpsLon = gpsLon; }
    
    public LocalDateTime getTakenAt() { return takenAt; }
    public void setTakenAt(LocalDateTime takenAt) { this.takenAt = takenAt; }
    
    public Boolean getIsWatermarked() { return isWatermarked; }
    public void setIsWatermarked(Boolean isWatermarked) { this.isWatermarked = isWatermarked; }
    
    public String getExifData() { return exifData; }
    public void setExifData(String exifData) { this.exifData = exifData; }
}
