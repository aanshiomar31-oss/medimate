package com.medimate.controller;

import com.medimate.dto.*;
import com.medimate.model.BatchStatus;
import com.medimate.model.MedicineBatch;
import com.medimate.service.BatchCustodyService;
import com.medimate.service.SupplyChainAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/batches")
@Tag(name = "Batch Custody & Anti-Counterfeit Verification", description = "Endpoints for pharmaceutical batch creation, custody transfers, recall quarantine, and provenance verification")
public class BatchController {

    private final BatchCustodyService custodyService;
    private final SupplyChainAnalyticsService analyticsService;

    public BatchController(BatchCustodyService custodyService,
                           SupplyChainAnalyticsService analyticsService) {
        this.custodyService = custodyService;
        this.analyticsService = analyticsService;
    }

    @PostMapping
    @Operation(summary = "Register a newly manufactured medicine batch")
    public ResponseEntity<ApiResponse<MedicineBatch>> createBatch(@Valid @RequestBody CreateBatchRequest request) {
        MedicineBatch batch = custodyService.createBatch(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Batch registered successfully with initial audit log", batch));
    }

    @GetMapping
    @Operation(summary = "List medicine batches with optional status and active filters")
    public ResponseEntity<ApiResponse<List<MedicineBatch>>> getBatches(
            @RequestParam(required = false) BatchStatus status,
            @RequestParam(required = false) Boolean activeOnly) {

        List<MedicineBatch> batches = custodyService.getAllBatches(status, activeOnly);
        return ResponseEntity.ok(ApiResponse.ok("Retrieved " + batches.size() + " batches", batches));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get batch details by ID")
    public ResponseEntity<ApiResponse<MedicineBatch>> getBatchById(@PathVariable Long id) {
        MedicineBatch batch = custodyService.getBatchById(id);
        return ResponseEntity.ok(ApiResponse.ok("Batch retrieved", batch));
    }

    @PostMapping("/{id}/transfer")
    @Operation(summary = "Transfer batch custody with temperature logging and state machine validation")
    public ResponseEntity<ApiResponse<MedicineBatch>> transferCustody(
            @PathVariable Long id,
            @Valid @RequestBody TransferCustodyRequest request) {

        MedicineBatch updated = custodyService.transferCustody(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Custody transferred to " + request.getToHolder(), updated));
    }

    @PostMapping("/{id}/recall")
    @Operation(summary = "Trigger emergency regulatory recall and quarantine a batch")
    public ResponseEntity<ApiResponse<MedicineBatch>> recallBatch(
            @PathVariable Long id,
            @Valid @RequestBody RecallRequest request) {

        MedicineBatch recalled = custodyService.recallBatch(id, request);
        return ResponseEntity.ok(ApiResponse.ok("EMERGENCY RECALL executed for batch " + recalled.getBatchNumber(), recalled));
    }

    @GetMapping("/{batchNumber}/verify")
    @Operation(summary = "Anti-Counterfeit Verification: inspect unbroken chain-of-custody, authenticity, and cold-chain compliance")
    public ResponseEntity<ApiResponse<BatchVerificationDTO>> verifyBatch(@PathVariable String batchNumber) {
        BatchVerificationDTO verification = analyticsService.verifyBatchProvenance(batchNumber);
        String msg = verification.isAuthentic() ? "Batch verified as authentic" : "WARNING: Batch authenticity compromised or recalled";
        return ResponseEntity.ok(ApiResponse.ok(msg, verification));
    }
}
