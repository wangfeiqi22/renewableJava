package com.renewable.ai.service;

import com.renewable.ai.dto.*;
import com.renewable.ai.entity.*;
import com.renewable.ai.repository.*;
import com.renewable.ai.util.ExportUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExportService {
    
    @Autowired
    private DriverRepository driverRepository;
    
    @Autowired
    private FleetOrderRepository fleetOrderRepository;
    
    @Autowired
    private FleetProjectRepository fleetProjectRepository;
    
    @Autowired
    private FleetOrderRepository orderRepository;
    
    public void exportDrivers(HttpServletResponse response, Long fleetId) throws IOException {
        List<String> headers = List.of(
                "ID", "姓名", "手机号", "身份证号", "驾驶证号", 
                "车牌号", "车辆类型", "状态", "入职日期", "创建时间"
        );
        
        List<Driver> drivers = driverRepository.findByFleetId(fleetId, Pageable.unpaged()).getContent();
        List<DriverVO> driverVOs = drivers.stream().map(DriverVO::fromEntity).collect(Collectors.toList());
        
        ExportUtil.exportToCSV(response, "drivers_" + fleetId, headers, driverVOs, driver -> 
            new String[]{
                String.valueOf(driver.getId()),
                driver.getName(),
                driver.getPhone(),
                driver.getIdNumber(),
                driver.getDriverLicenseNumber(),
                driver.getVehiclePlate(),
                driver.getVehicleType(),
                driver.getStatusDescription(),
                driver.getHireDate() != null ? driver.getHireDate().toString() : "",
                driver.getCreatedAt() != null ? driver.getCreatedAt().toString() : ""
            }
        );
    }
    
    public void exportOrders(HttpServletResponse response, Long fleetId) throws IOException {
        List<String> headers = List.of(
                "订单号", "项目名称", "司机姓名", "司机电话", "废物类型",
                "预估重量", "实际重量", "取货地址", "目的地址", 
                "状态", "创建时间", "完成时间"
        );
        
        List<FleetOrder> orders = fleetOrderRepository.findByFleetId(fleetId, Pageable.unpaged()).getContent();
        List<OrderVO> orderVOs = orders.stream().map(OrderVO::fromEntity).collect(Collectors.toList());
        
        ExportUtil.exportToCSV(response, "orders_" + fleetId, headers, orderVOs, order -> 
            new String[]{
                order.getOrderNumber(),
                order.getProjectName(),
                order.getDriverName(),
                order.getDriverPhone(),
                order.getWasteType(),
                order.getEstimatedWeight() != null ? order.getEstimatedWeight().toString() : "",
                order.getActualWeight() != null ? order.getActualWeight().toString() : "",
                order.getPickupAddress(),
                order.getDestinationAddress(),
                order.getStatusDescription(),
                order.getCreatedAt() != null ? order.getCreatedAt().toString() : "",
                order.getCompletedTime() != null ? order.getCompletedTime().toString() : ""
            }
        );
    }
    
    public void exportProjects(HttpServletResponse response, Long fleetId) throws IOException {
        List<String> headers = List.of(
                "项目名称", "项目编号", "客户名称", "客户电话", "地址",
                "废物类型", "合同开始日期", "合同结束日期", "单价", 
                "总订单数", "完成订单数", "总收入", "总重量", "状态"
        );
        
        List<FleetProject> projects = fleetProjectRepository.findByFleetId(fleetId, Pageable.unpaged()).getContent();
        List<ProjectVO> projectVOs = projects.stream().map(ProjectVO::fromEntity).collect(Collectors.toList());
        
        ExportUtil.exportToCSV(response, "projects_" + fleetId, headers, projectVOs, project -> 
            new String[]{
                project.getProjectName(),
                project.getProjectCode(),
                project.getCustomerName(),
                project.getCustomerPhone(),
                project.getAddress(),
                project.getWasteTypes(),
                project.getContractStartDate() != null ? project.getContractStartDate().toString() : "",
                project.getContractEndDate() != null ? project.getContractEndDate().toString() : "",
                project.getUnitPrice() != null ? project.getUnitPrice().toString() : "",
                String.valueOf(project.getTotalOrders()),
                String.valueOf(project.getCompletedOrders()),
                project.getTotalRevenue() != null ? project.getTotalRevenue().toString() : "",
                project.getTotalWeight() != null ? project.getTotalWeight().toString() : "",
                project.getStatusDescription()
            }
        );
    }
}
