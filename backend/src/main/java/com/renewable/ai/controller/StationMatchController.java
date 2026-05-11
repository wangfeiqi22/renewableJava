package com.renewable.ai.controller;

import com.renewable.ai.common.ApiResponse;
import com.renewable.ai.service.StationMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
public class StationMatchController {
    private final StationMatchService stationMatchService;

    @GetMapping("/match")
    public ApiResponse<List<StationMatchService.StationMatchVO>> matchStations(
            @RequestParam String wasteType,
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lon,
            @RequestParam(defaultValue = "5") int limit) {
        List<StationMatchService.StationMatchVO> matches = stationMatchService.findBestStations(wasteType, lat, lon, limit);
        return ApiResponse.success(matches);
    }
}
