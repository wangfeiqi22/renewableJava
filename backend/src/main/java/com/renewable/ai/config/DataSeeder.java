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
            userRepository.findByUsername("admin").ifPresentOrElse(existing -> {
                if (existing.getStatus() == null || existing.getStatus() != 1) {
                    existing.setStatus(1);
                    userRepository.save(existing);
                }
            }, () -> {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPasswordHash(SecurityUtil.hashPassword("admin123"));
                admin.setRole("admin");
                admin.setPhone("13800000000");
                admin.setStatus(1); // Admin always active
                userRepository.save(admin);
                System.out.println("Initialized Admin User: admin / admin123");
            });
            
            // 2. Init Fleet Driver User
            userRepository.findByUsername("driver1").ifPresentOrElse(existing -> {
                if (existing.getStatus() == null || existing.getStatus() != 1) {
                    existing.setStatus(1);
                    userRepository.save(existing);
                }
            }, () -> {
                User driver = new User();
                driver.setUsername("driver1");
                driver.setPasswordHash(SecurityUtil.hashPassword("123456"));
                driver.setRole("driver");
                driver.setPhone("13900000001");
                driver.setFleetId(1L); // Belongs to Fleet 1
                driver.setStatus(1);
                userRepository.save(driver);
                System.out.println("Initialized Fleet Driver: driver1 / 123456 (Fleet 1)");
            });
            
            // 2.1 Init Freelance Driver User
            userRepository.findByUsername("driver2").ifPresentOrElse(existing -> {
                if (existing.getStatus() == null || existing.getStatus() != 1) {
                    existing.setStatus(1);
                    userRepository.save(existing);
                }
            }, () -> {
                User driver = new User();
                driver.setUsername("driver2");
                driver.setPasswordHash(SecurityUtil.hashPassword("123456"));
                driver.setRole("driver");
                driver.setPhone("13900000002");
                driver.setFleetId(null); // Freelance
                driver.setStatus(1);
                userRepository.save(driver);
                System.out.println("Initialized Freelance Driver: driver2 / 123456");
            });
            
            // 2.2 Init Station Admin
            final Long[] stationAdminIdHolder = new Long[1];
            userRepository.findByUsername("station1").ifPresentOrElse(existing -> {
                if (existing.getStatus() == null || existing.getStatus() != 1) {
                    existing.setStatus(1);
                    userRepository.save(existing);
                }
                stationAdminIdHolder[0] = existing.getId();
            }, () -> {
                User station = new User();
                station.setUsername("station1");
                station.setPasswordHash(SecurityUtil.hashPassword("123456"));
                station.setRole("station");
                station.setPhone("13900000003");
                station.setStatus(1);
                User saved = userRepository.save(station);
                stationAdminIdHolder[0] = saved.getId();
                System.out.println("Initialized Station Admin: station1 / 123456");
            });

            // 3. Init Individual User
            userRepository.findByUsername("user1").ifPresentOrElse(existing -> {
                if (existing.getStatus() == null || existing.getStatus() != 1) {
                    existing.setStatus(1);
                    userRepository.save(existing);
                }
            }, () -> {
                User user = new User();
                user.setUsername("user1");
                user.setPasswordHash(SecurityUtil.hashPassword("123456"));
                user.setRole("individual");
                user.setPhone("13700000002");
                user.setStatus(1);
                userRepository.save(user);
                System.out.println("Initialized Individual User: user1 / 123456");
            });

            // 3.1 Init Property Manager
            userRepository.findByUsername("prop1").ifPresentOrElse(existing -> {
                if (existing.getStatus() == null || existing.getStatus() != 1) {
                    existing.setStatus(1);
                    userRepository.save(existing);
                }
            }, () -> {
                User prop = new User();
                prop.setUsername("prop1");
                prop.setPasswordHash(SecurityUtil.hashPassword("123456"));
                prop.setRole("property");
                prop.setPhone("13600000003");
                prop.setStatus(1);
                userRepository.save(prop);
                System.out.println("Initialized Property Manager: prop1 / 123456");
            });

            // 4. Init Stations
            if (stationRepository.count() == 0) {
                Station s1 = new Station();
                s1.setName("朝阳区第一清运站");
                s1.setType(1);
                s1.setAddress("北京市朝阳区xx路88号");
                s1.setLat(new BigDecimal("39.9042"));
                s1.setLon(new BigDecimal("116.4074"));
                if (stationAdminIdHolder[0] != null) {
                    s1.setManagerId(stationAdminIdHolder[0]);
                }
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
