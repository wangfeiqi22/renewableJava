package com.renewable.ai.controller;

import com.renewable.ai.entity.Station;
import com.renewable.ai.service.StationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stations")
@CrossOrigin(origins = "*")
public class StationController {

    @Autowired
    private StationService stationService;

    @PostMapping
    public ResponseEntity<Station> createStation(@RequestBody Station station) {
        return ResponseEntity.ok(stationService.createStation(station));
    }

    @GetMapping
    public ResponseEntity<List<Station>> getAllStations() {
        return ResponseEntity.ok(stationService.getAllStations());
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Station>> getStationsByType(@PathVariable Integer type) {
        return ResponseEntity.ok(stationService.getStationsByType(type));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Station> getStationById(@PathVariable Long id) {
        return stationService.getStationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 清运站管理员获取自己负责的站点信息。
     */
    @GetMapping("/me")
    public ResponseEntity<Station> getMyStation(javax.servlet.http.HttpServletRequest request) {
        Object uidAttr = request.getAttribute("userId");
        if (!(uidAttr instanceof Long)) {
            return ResponseEntity.status(401).build();
        }
        Long userId = (Long) uidAttr;
        return stationService.getStationByManagerId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 清运站管理员维护自己的公告 / 介绍 / 库存概况。
     * 简化起见，这里通过请求体直接提交完整 Station 对象的相关字段。
     */
    @PutMapping("/{id}/profile")
    public ResponseEntity<Station> updateStationProfile(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(stationService.updateStationProfile(id, payload));
    }

    /**
     * 清运站管理员更新自己站点的公告/介绍/库存。
     */
    @PutMapping("/me/profile")
    public ResponseEntity<Station> updateMyStationProfile(@RequestBody Map<String, String> payload,
                                                          javax.servlet.http.HttpServletRequest request) {
        Object uidAttr = request.getAttribute("userId");
        if (!(uidAttr instanceof Long)) {
            return ResponseEntity.status(401).build();
        }
        Long userId = (Long) uidAttr;
        Station mine = stationService.getStationByManagerId(userId)
                .orElseThrow(() -> new RuntimeException("未找到当前账号管理的清运站"));
        return ResponseEntity.ok(stationService.updateStationProfile(mine.getId(), payload));
    }
}
