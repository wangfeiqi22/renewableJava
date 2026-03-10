package com.renewable.ai.controller;

import com.renewable.ai.entity.Order;
import com.renewable.ai.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/driver")
@CrossOrigin(origins = "*")
public class DriverController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/task/history")
    public ResponseEntity<Page<Order>> getHistoryTasks(
            @RequestParam Long driverId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        // Default date range: last 90 days
        // Parse ISO string (e.g. 2023-10-01T12:00:00.000Z) to ZonedDateTime then to LocalDateTime
        LocalDateTime end;
        if (endDate != null) {
            end = ZonedDateTime.parse(endDate).toLocalDateTime();
        } else {
            end = LocalDateTime.now();
        }
        
        LocalDateTime start;
        if (startDate != null) {
            start = ZonedDateTime.parse(startDate).toLocalDateTime();
        } else {
            start = end.minusDays(90);
        }

        return ResponseEntity.ok(orderService.getDriverHistory(driverId, page, pageSize, start, end));
    }
}
