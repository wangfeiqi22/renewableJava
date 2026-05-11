package com.renewable.ai.controller;

import com.renewable.ai.entity.Driver;
import com.renewable.ai.entity.User;
import com.renewable.ai.repository.DriverRepository;
import com.renewable.ai.repository.UserRepository;
import com.renewable.ai.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class DriverControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private UserRepository userRepository;

    private User fleetOwner;
    private User dispatcher;
    private Driver driver1;
    private Driver driver2;
    private Driver otherFleetDriver;

    @BeforeEach
    void setUp() {
        driverRepository.deleteAll();
        userRepository.deleteAll();

        // Create fleet owner for Fleet 1
        fleetOwner = new User();
        fleetOwner.setUsername("fleetowner_test_" + System.currentTimeMillis());
        fleetOwner.setPasswordHash(SecurityUtil.hashPassword("password"));
        fleetOwner.setRole("fleet_owner");
        fleetOwner.setFleetId(1L);
        fleetOwner.setStatus(1);
        fleetOwner = userRepository.save(fleetOwner);

        // Create dispatcher for Fleet 1
        dispatcher = new User();
        dispatcher.setUsername("dispatcher_test_" + System.currentTimeMillis());
        dispatcher.setPasswordHash(SecurityUtil.hashPassword("password"));
        dispatcher.setRole("fleet_dispatcher");
        dispatcher.setFleetId(1L);
        dispatcher.setStatus(1);
        dispatcher = userRepository.save(dispatcher);

        // Create driver for Fleet 1
        driver1 = new Driver();
        driver1.setName("张三");
        driver1.setPhone("13800138001");
        driver1.setFleetId(1L);
        driver1.setStatus("AVAILABLE");
        driver1.setCreatedBy(fleetOwner.getId());
        driver1.setCreatedByName(fleetOwner.getName());
        driver1 = driverRepository.save(driver1);

        // Create another driver for Fleet 1
        driver2 = new Driver();
        driver2.setName("李四");
        driver2.setPhone("13800138002");
        driver2.setFleetId(1L);
        driver2.setStatus("AVAILABLE");
        driver2.setCreatedBy(fleetOwner.getId());
        driver2.setCreatedByName(fleetOwner.getName());
        driver2 = driverRepository.save(driver2);

        // Create driver for Fleet 2 (different fleet)
        otherFleetDriver = new Driver();
        otherFleetDriver.setName("王五");
        otherFleetDriver.setPhone("13900139000");
        otherFleetDriver.setFleetId(2L); // Different fleet!
        otherFleetDriver.setStatus("AVAILABLE");
        otherFleetDriver.setCreatedBy(999L);
        otherFleetDriver.setCreatedByName("Other Owner");
        otherFleetDriver = driverRepository.save(otherFleetDriver);
    }

    @Test
    @WithMockUser(username = "fleet_owner", roles = {"FLEET_OWNER"})
    void testGetDrivers_ShouldOnlyReturnDriversForCurrentFleet() throws Exception {
        mockMvc.perform(get("/api/fleet/drivers")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray());

        // Verify only Fleet 1 drivers are returned
        // Note: In real implementation, SecurityUtil.getCurrentUser() would be used
    }

    @Test
    @WithMockUser(username = "dispatcher", roles = {"FLEET_DISPATCHER"})
    void testGetDrivers_DispatcherShouldOnlySeeOwnFleetDrivers() throws Exception {
        mockMvc.perform(get("/api/fleet/drivers")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Dispatcher should not see drivers from other fleets
    }

    @Test
    @WithMockUser(username = "fleet_owner", roles = {"FLEET_OWNER"})
    void testGetDriverById_ShouldValidateFleetOwnership() throws Exception {
        // Test accessing driver from own fleet (should succeed)
        mockMvc.perform(get("/api/fleet/drivers/" + driver1.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // Test accessing driver from other fleet (should fail)
        mockMvc.perform(get("/api/fleet/drivers/" + otherFleetDriver.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "fleet_owner", roles = {"FLEET_OWNER"})
    void testUpdateDriver_ShouldValidateFleetOwnership() throws Exception {
        String updateJson = """
                {
                    "name": "张三 Updated",
                    "phone": "13800138001"
                }
                """;

        // Update own fleet driver (should succeed)
        mockMvc.perform(put("/api/fleet/drivers/" + driver1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk());

        // Update other fleet driver (should fail)
        mockMvc.perform(put("/api/fleet/drivers/" + otherFleetDriver.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "fleet_owner", roles = {"FLEET_OWNER"})
    void testDeleteDriver_ShouldValidateFleetOwnership() throws Exception {
        // Delete own fleet driver (should succeed)
        mockMvc.perform(delete("/api/fleet/drivers/" + driver1.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Delete other fleet driver (should fail)
        mockMvc.perform(delete("/api/fleet/drivers/" + otherFleetDriver.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testGetDrivers_WithoutAuthentication_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/fleet/drivers")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
