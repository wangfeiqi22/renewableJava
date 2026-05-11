package com.renewable.ai.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProjectCreateDTO {
    
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100, message = "项目名称不能超过100字符")
    private String projectName;
    
    @Size(max = 32, message = "项目编号不能超过32字符")
    private String projectCode;
    
    private String customerName;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String customerPhone;
    
    @NotBlank(message = "地址不能为空")
    @Size(max = 200, message = "地址不能超过200字符")
    private String address;
    
    private Double latitude;
    
    private Double longitude;
    
    @Size(max = 500, message = "废物类型不能超过500字符")
    private String wasteTypes;
    
    @DecimalMin(value = "0", message = "预估月量不能为负数")
    private BigDecimal estimatedMonthlyVolume;
    
    private LocalDateTime contractStartDate;
    
    private LocalDateTime contractEndDate;
    
    @DecimalMin(value = "0", message = "单价不能为负数")
    private BigDecimal unitPrice;
    
    @Size(max = 1000, message = "描述不能超过1000字符")
    private String description;
    
    private String contactPerson;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "联系电话格式不正确")
    private String contactPhone;
    
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    
    public String getProjectCode() { return projectCode; }
    public void setProjectCode(String projectCode) { this.projectCode = projectCode; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public String getWasteTypes() { return wasteTypes; }
    public void setWasteTypes(String wasteTypes) { this.wasteTypes = wasteTypes; }
    
    public BigDecimal getEstimatedMonthlyVolume() { return estimatedMonthlyVolume; }
    public void setEstimatedMonthlyVolume(BigDecimal estimatedMonthlyVolume) { this.estimatedMonthlyVolume = estimatedMonthlyVolume; }
    
    public LocalDateTime getContractStartDate() { return contractStartDate; }
    public void setContractStartDate(LocalDateTime contractStartDate) { this.contractStartDate = contractStartDate; }
    
    public LocalDateTime getContractEndDate() { return contractEndDate; }
    public void setContractEndDate(LocalDateTime contractEndDate) { this.contractEndDate = contractEndDate; }
    
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
}
