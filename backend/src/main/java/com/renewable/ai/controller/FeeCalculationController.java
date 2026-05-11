package com.renewable.ai.controller;

import com.renewable.ai.common.ApiResponse;
import com.renewable.ai.entity.OrderFeeDetail;
import com.renewable.ai.repository.OrderFeeDetailRepository;
import com.renewable.ai.repository.OrderRepository;
import com.renewable.ai.service.FeeCalculationService;
import com.renewable.ai.service.StationMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class FeeCalculationController {
    private final FeeCalculationService feeCalculationService;
    private final StationMatchService stationMatchService;
    private final OrderFeeDetailRepository orderFeeDetailRepository;
    private final OrderRepository orderRepository;

    @PostMapping("/calculate-fee")
    public ApiResponse<FeeCalculationResultVO> calculateFee(@RequestBody FeeCalculationRequest request) {
        StationMatchService.StationMatchVO match = stationMatchService.matchSingleStation(
                request.getWasteType(), request.getPickupLat(), request.getPickupLon());

        FeeCalculationService.FeeCalculationResult result = feeCalculationService.calculateOrderFee(
                request.getPickupLat(), request.getPickupLon(),
                match.getStationId(), request.getWasteType(),
                request.getEstVolume(), request.isForkliftRequired(), request.getContainerCount());

        FeeCalculationResultVO vo = new FeeCalculationResultVO();
        vo.setStationId(result.getStationId());
        vo.setStationName(result.getStationName());
        vo.setDistance(result.getDistance());
        vo.setTransportFee(result.getTransportFee());
        vo.setDisposalFee(result.getDisposalFee());
        vo.setHandlingFee(result.getHandlingFee());
        vo.setTotalFee(result.getTotalFee());
        vo.setBreakdown(Map.of(
                "transportFee", result.getTransportFee(),
                "disposalFee", result.getDisposalFee(),
                "handlingFee", result.getHandlingFee(),
                "totalFee", result.getTotalFee()
        ));

        return ApiResponse.success(vo);
    }

    @PostMapping("/{orderId}/confirm-fee")
    public ApiResponse<OrderFeeDetail> confirmFee(
            @PathVariable Long orderId,
            @RequestBody FeeConfirmRequest request,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        OrderFeeDetail feeDetail = orderFeeDetailRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("费用明细不存在"));

        feeDetail.setIsConfirmed(true);
        feeDetail.setConfirmedAt(java.time.LocalDateTime.now());
        feeDetail.setConfirmedBy(userId);

        if (request.getConfirmedFee() != null) {
            feeDetail.setManualAdjustment(request.getConfirmedFee().subtract(feeDetail.getTotalFee()));
            feeDetail.setAdjustmentReason(request.getAdjustmentReason());
        }

        OrderFeeDetail saved = orderFeeDetailRepository.save(feeDetail);

        orderRepository.findById(orderId).ifPresent(order -> {
            order.setFeeCalculationStatus("CONFIRMED");
            orderRepository.save(order);
        });

        return ApiResponse.success(saved);
    }

    @GetMapping("/{orderId}/fee-detail")
    public ApiResponse<OrderFeeDetail> getFeeDetail(@PathVariable Long orderId) {
        return ApiResponse.success(orderFeeDetailRepository.findByOrderId(orderId).orElse(null));
    }

    @lombok.Data
    public static class FeeCalculationRequest {
        private String wasteType;
        private BigDecimal estVolume;
        private BigDecimal pickupLat;
        private BigDecimal pickupLon;
        private boolean forkliftRequired;
        private int containerCount;
    }

    @lombok.Data
    public static class FeeConfirmRequest {
        private BigDecimal confirmedFee;
        private String adjustmentReason;
    }

    @lombok.Data
    public static class FeeCalculationResultVO {
        private Long stationId;
        private String stationName;
        private BigDecimal distance;
        private BigDecimal transportFee;
        private BigDecimal disposalFee;
        private BigDecimal handlingFee;
        private BigDecimal totalFee;
        private Map<String, BigDecimal> breakdown;
    }
}
