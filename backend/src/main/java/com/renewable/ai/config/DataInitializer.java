package com.renewable.ai.config;

import com.renewable.ai.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private PermissionService permissionService;
    
    @Override
    public void run(String... args) {
        permissionService.initializeDefaultRolesAndPermissions();
    }
}
