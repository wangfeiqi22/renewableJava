package com.renewable.ai.controller;

import com.renewable.ai.entity.Order;
import com.renewable.ai.repository.UserRepository;
import com.renewable.ai.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/driver")
@CrossOrigin(origins = "*")
public class DriverController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/task/history")
    public ResponseEntity<Page<Order>> getHistoryTasks(
            HttpServletRequest request,
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) String username,
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

        Long resolvedDriverId = driverId;
        if (resolvedDriverId == null && username != null && !username.isBlank()) {
            resolvedDriverId = userRepository.findByUsername(username)
                    .map(com.renewable.ai.entity.User::getId)
                    .orElse(null);
        }

        Long tokenUserId = null;
        Object tokenUserIdAttr = request.getAttribute("userId");
        if (tokenUserIdAttr instanceof Long) tokenUserId = (Long) tokenUserIdAttr;

        if (tokenUserId != null) {
            resolvedDriverId = tokenUserId;
        } else if (resolvedDriverId == null) {
            throw new RuntimeException("driverId or username is required");
        }

        return ResponseEntity.ok(orderService.getDriverHistory(resolvedDriverId, page, pageSize, start, end));
    }
}
