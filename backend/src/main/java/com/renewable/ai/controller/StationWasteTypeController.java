package com.renewable.ai.controller;

import com.renewable.ai.common.ApiResponse;
import com.renewable.ai.entity.*;
import com.renewable.ai.service.StationWasteTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
public class StationWasteTypeController {
    private final StationWasteTypeService stationWasteTypeService;

    @GetMapping("/waste-types")
    public ApiResponse<List<WasteType>> getAllWasteTypes() {
        return ApiResponse.success(stationWasteTypeService.getAllActiveWasteTypes());
    }

    @GetMapping("/{stationId}/waste-types")
    public ApiResponse<List<StationWasteType>> getStationWasteTypes(@PathVariable Long stationId) {
        return ApiResponse.success(stationWasteTypeService.getStationWasteTypes(stationId));
    }

    @PostMapping("/{stationId}/waste-types")
    public ApiResponse<StationWasteType> addWasteType(
            @PathVariable Long stationId,
            @RequestBody WasteTypeRequest request) {
        StationWasteType result = stationWasteTypeService.addWasteTypeToStation(
                stationId, request.getWasteTypeId(), request.getPricePerCubic());
        return ApiResponse.success(result);
    }

    @PutMapping("/{stationId}/waste-types/{id}/price")
    public ApiResponse<StationWasteType> updatePrice(
            @PathVariable Long stationId,
            @PathVariable Long id,
            @RequestBody PriceUpdateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        StationWasteType result = stationWasteTypeService.updatePrice(
                id, request.getNewPrice(), userId, request.getReason());
        return ApiResponse.success(result);
    }

    @DeleteMapping("/{stationId}/waste-types/{id}")
    public ApiResponse<Void> removeWasteType(
            @PathVariable Long stationId,
            @PathVariable Long id) {
        stationWasteTypeService.removeWasteTypeFromStation(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/{stationId}/waste-types/{id}/price-history")
    public ApiResponse<List<WastePriceHistory>> getPriceHistory(
            @PathVariable Long stationId,
            @PathVariable Long id) {
        return ApiResponse.success(stationWasteTypeService.getPriceHistory(id));
    }

    @lombok.Data
    public static class WasteTypeRequest {
        private Long wasteTypeId;
        private BigDecimal pricePerCubic;
    }

    @lombok.Data
    public static class PriceUpdateRequest {
        private BigDecimal newPrice;
        private String reason;
    }
}
