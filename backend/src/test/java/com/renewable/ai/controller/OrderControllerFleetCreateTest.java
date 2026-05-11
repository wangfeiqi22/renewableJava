package com.renewable.ai.controller;

import com.renewable.ai.entity.Order;
import com.renewable.ai.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class OrderControllerFleetCreateTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    public void testCreateFleetOrder_Success() throws Exception {
        // 清理测试数据
        orderRepository.deleteAll();

        Map<String, Object> payload = new HashMap<>();
        payload.put("pickupAddress", "测试取货地址");
        payload.put("wasteType", "recyclable");
        payload.put("estWeight", 5.5);
        payload.put("placementStatus", "已堆放到位");
        payload.put("wasteDesc", "测试备注");
        payload.put("driverId", 1L);
        payload.put("fleetId", 100L);

        MvcResult result = mockMvc.perform(post("/api/orders/fleet-create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(payload)))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("orderNo"));
        assertTrue(content.contains("\"status\":20"));
    }

    @Test
    public void testCreateFleetOrder_NoDriver() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("pickupAddress", "测试地址");
        payload.put("wasteType", "recyclable");
        payload.put("driverId", null);
        payload.put("fleetId", 100L);

        mockMvc.perform(post("/api/orders/fleet-create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("请选择司机"));
    }

    @Test
    public void testCreateFleetOrder_ValidationErrors() throws Exception {
        // 测试缺少必填字段
        Map<String, Object> payload = new HashMap<>();
        payload.put("driverId", 1L);
        payload.put("fleetId", 100L);
        // 缺少 pickupAddress

        mockMvc.perform(post("/api/orders/fleet-create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(payload)))
                .andExpect(status().isBadRequest());
    }

    private static String asJsonString(Object obj) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
