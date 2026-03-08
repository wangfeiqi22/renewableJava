package com.renewable.ai.config;

import com.renewable.ai.entity.*;
import com.renewable.ai.repository.*;
import com.renewable.ai.util.SecurityUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository,
                                   StationRepository stationRepository,
                                   FleetRepository fleetRepository,
                                   VehicleRepository vehicleRepository,
                                   KnowledgeBaseRepository kbRepository) {
        return args -> {
            // 1. Init Admin User
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPasswordHash(SecurityUtil.hashPassword("admin123"));
                admin.setRole("admin");
                admin.setPhone("13800000000");
                userRepository.save(admin);
                System.out.println("Initialized Admin User: admin / admin123");
            }
            
            // 2. Init Driver User
            if (userRepository.findByUsername("driver1").isEmpty()) {
                User driver = new User();
                driver.setUsername("driver1");
                driver.setPasswordHash(SecurityUtil.hashPassword("123456"));
                driver.setRole("driver");
                driver.setPhone("13900000001");
                userRepository.save(driver);
                System.out.println("Initialized Driver User: driver1 / 123456");
            }

            // 3. Init Stations
            if (stationRepository.count() == 0) {
                Station s1 = new Station();
                s1.setName("朝阳区第一清运站");
                s1.setType(1);
                s1.setAddress("北京市朝阳区xx路88号");
                s1.setLat(new BigDecimal("39.9042"));
                s1.setLon(new BigDecimal("116.4074"));
                stationRepository.save(s1);

                Station s2 = new Station();
                s2.setName("海淀中转站");
                s2.setType(2);
                s2.setAddress("北京市海淀区xx街12号");
                stationRepository.save(s2);
                System.out.println("Initialized Stations");
            }

            // 4. Init Fleets & Vehicles
            if (fleetRepository.count() == 0) {
                Fleet f1 = new Fleet();
                f1.setName("第一车队");
                f1 = fleetRepository.save(f1);

                Vehicle v1 = new Vehicle();
                v1.setFleetId(f1.getId());
                v1.setPlateNo("京A88888");
                v1.setType("压缩车");
                v1.setLoadCapacity(new BigDecimal("5.0"));
                vehicleRepository.save(v1);
                System.out.println("Initialized Fleet & Vehicle");
            }
            
            // 5. Init Knowledge Base
            if (kbRepository.count() == 0) {
                KnowledgeBase k1 = new KnowledgeBase();
                k1.setQuestion("清运费用是多少？");
                k1.setAnswer("生活垃圾 50元/吨，建筑垃圾 80元/吨，大件垃圾按件计费。");
                k1.setCategory("pricing");
                kbRepository.save(k1);

                KnowledgeBase k2 = new KnowledgeBase();
                k2.setQuestion("工作时间是几点？");
                k2.setAnswer("我们的工作时间是每天 8:00 - 20:00，节假日不休息。");
                k2.setCategory("guide");
                kbRepository.save(k2);
                System.out.println("Initialized Knowledge Base");
            }
        };
    }
}
