package com.medimate.controller;

import com.medimate.dto.ApiResponse;
import com.medimate.dto.StageLatencyDTO;
import com.medimate.dto.StalledShipmentDTO;
import com.medimate.model.MedicineBatch;
import com.medimate.service.SupplyChainAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Supply Chain Process Analytics & Telemetry", description = "Endpoints for stalled shipments, cold-chain temperature alerts, expiring batches, and process latency metrics")
public class AnalyticsController {

    private final SupplyChainAnalyticsService analyticsService;

    public AnalyticsController(SupplyChainAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/stalled-shipments")
    @Operation(summary = "Detect active shipments breaching transit SLAs or suffering temperature excursions")
    public ResponseEntity<ApiResponse<List<StalledShipmentDTO>>> getStalledShipments() {
        List<StalledShipmentDTO> stalled = analyticsService.findStalledShipments();
        return ResponseEntity.ok(ApiResponse.ok("Identified " + stalled.size() + " stalled/at-risk shipments", stalled));
    }

    @GetMapping("/expiring-batches")
    @Operation(summary = "Identify medicine batches approaching expiration date")
    public ResponseEntity<ApiResponse<List<MedicineBatch>>> getExpiringBatches(
            @RequestParam(defaultValue = "60") int daysThreshold) {

        List<MedicineBatch> expiring = analyticsService.findExpiringBatches(daysThreshold);
        return ResponseEntity.ok(ApiResponse.ok("Found " + expiring.size() + " batches expiring within " + daysThreshold + " days", expiring));
    }

    @GetMapping("/latency")
    @Operation(summary = "Process Mining stage latency analysis to reveal supply chain bottlenecks")
    public ResponseEntity<ApiResponse<List<StageLatencyDTO>>> getStageLatency() {
        List<StageLatencyDTO> latency = analyticsService.getStageLatencyAnalytics();
        return ResponseEntity.ok(ApiResponse.ok("Stage latency calculated across " + latency.size() + " stages", latency));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Executive KPI health summary: active batches, transit compliance, recalls, and alerts")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard() {
        Map<String, Object> summary = analyticsService.getDashboardSummary();
        return ResponseEntity.ok(ApiResponse.ok("Executive dashboard generated", summary));
    }
}
