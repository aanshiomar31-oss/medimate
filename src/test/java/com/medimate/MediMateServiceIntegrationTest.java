package com.medimate;

import com.medimate.dto.*;
import com.medimate.model.*;
import com.medimate.repository.*;
import com.medimate.service.BatchCustodyService;
import com.medimate.service.SupplyChainAnalyticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MediMateServiceIntegrationTest {

    @Autowired
    private BatchCustodyService custodyService;

    @Autowired
    private SupplyChainAnalyticsService analyticsService;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    @Autowired
    private MedicineBatchRepository batchRepository;

    @Test
    @DisplayName("Should create batch and generate initial manufacturing audit log")
    void testCreateBatchAndAuditLog() {
        Medicine medicine = medicineRepository.findAll().get(0);
        Manufacturer manufacturer = manufacturerRepository.findAll().get(0);

        CreateBatchRequest request = new CreateBatchRequest(
                medicine.getId(),
                manufacturer.getId(),
                5000,
                LocalDate.now(),
                LocalDate.now().plusMonths(24),
                "Production Plant Alpha",
                "QA Chemist John"
        );

        MedicineBatch created = custodyService.createBatch(request);

        assertNotNull(created.getId());
        assertNotNull(created.getBatchNumber());
        assertEquals(BatchStatus.MANUFACTURED, created.getStatus());
        assertEquals(5000, created.getInitialQuantity());

        BatchVerificationDTO verification = analyticsService.verifyBatchProvenance(created.getBatchNumber());
        assertNotNull(verification);
        assertTrue(verification.isAuthentic());
        assertEquals(1, verification.getChainOfCustody().size());
        assertEquals(BatchStatus.MANUFACTURED, verification.getChainOfCustody().get(0).getToStatus());
    }

    @Test
    @DisplayName("Should successfully transfer custody and append transition audit log")
    void testTransferCustodySuccess() {
        Medicine medicine = medicineRepository.findAll().get(0);
        Manufacturer manufacturer = manufacturerRepository.findAll().get(0);

        CreateBatchRequest request = new CreateBatchRequest(
                medicine.getId(),
                manufacturer.getId(),
                2000,
                LocalDate.now(),
                LocalDate.now().plusMonths(18),
                "Plant Beta",
                "Production Supervisor"
        );
        MedicineBatch created = custodyService.createBatch(request);

        // Advance to QUALITY_TESTED
        TransferCustodyRequest transferReq = new TransferCustodyRequest(
                BatchStatus.QUALITY_TESTED,
                "Lead Analytical Scientist",
                "Quality Control Lab #3",
                4.2,
                "Assay tested, passed microbiological limits"
        );

        MedicineBatch updated = custodyService.transferCustody(created.getId(), transferReq);

        assertEquals(BatchStatus.QUALITY_TESTED, updated.getStatus());
        assertEquals("Lead Analytical Scientist", updated.getCurrentHolder());
        assertEquals("Quality Control Lab #3", updated.getCurrentLocation());

        BatchVerificationDTO verification = analyticsService.verifyBatchProvenance(created.getBatchNumber());
        assertEquals(2, verification.getChainOfCustody().size());
        assertEquals(BatchStatus.MANUFACTURED, verification.getChainOfCustody().get(1).getFromStatus());
        assertEquals(BatchStatus.QUALITY_TESTED, verification.getChainOfCustody().get(1).getToStatus());
        assertTrue(verification.isAuthentic());
    }

    @Test
    @DisplayName("Should reject illegal state transition with IllegalStateException")
    void testIllegalCustodyTransition() {
        Medicine medicine = medicineRepository.findAll().get(0);
        Manufacturer manufacturer = manufacturerRepository.findAll().get(0);

        CreateBatchRequest request = new CreateBatchRequest(
                medicine.getId(),
                manufacturer.getId(),
                1000,
                LocalDate.now(),
                LocalDate.now().plusMonths(12),
                "Factory",
                "Worker"
        );
        MedicineBatch created = custodyService.createBatch(request);

        // Attempting to skip directly to DELIVERED_TO_PHARMACY
        TransferCustodyRequest illegalReq = new TransferCustodyRequest(
                BatchStatus.DELIVERED_TO_PHARMACY,
                "Hospital Pharmacist",
                "Hospital Vault",
                4.0,
                "Attempting illegal skip"
        );

        assertThrows(IllegalStateException.class, () -> custodyService.transferCustody(created.getId(), illegalReq));
    }

    @Test
    @DisplayName("Should verify authentic unbroken provenance for seeded batch")
    void testVerifyBatchProvenanceAuthentic() {
        BatchVerificationDTO verification = analyticsService.verifyBatchProvenance("MED-2026-PF01");

        assertNotNull(verification);
        assertTrue(verification.isAuthentic());
        assertTrue(verification.isColdChainCompliant());
        assertEquals(5, verification.getChainOfCustody().size());
        assertEquals(BatchStatus.DELIVERED_TO_PHARMACY, verification.getCurrentStatus());
    }

    @Test
    @DisplayName("Should detect seeded stalled and temperature-breached shipment")
    void testDetectStalledShipments() {
        List<StalledShipmentDTO> stalled = analyticsService.findStalledShipments();

        assertNotNull(stalled);
        assertFalse(stalled.isEmpty(), "Should detect seeded stalled shipment");
        assertTrue(stalled.stream().anyMatch(s -> s.isTemperatureExcursion() || s.getHoursOverdue() > 0));
    }

    @Test
    @DisplayName("Should execute emergency batch recall and mark as quarantined")
    void testRecallBatch() {
        MedicineBatch batch = batchRepository.findByBatchNumber("MED-2026-PF01").orElseThrow();

        RecallRequest recallReq = new RecallRequest("Packaging seal defect reported", "Dr. A. Sharma (FDA Inspector)");
        MedicineBatch recalled = custodyService.recallBatch(batch.getId(), recallReq);

        assertEquals(BatchStatus.RECALLED, recalled.getStatus());
        assertTrue(recalled.isRecalled());
        assertEquals("Packaging seal defect reported", recalled.getRecallReason());

        BatchVerificationDTO verification = analyticsService.verifyBatchProvenance(batch.getBatchNumber());
        assertTrue(verification.isRecalled());
        assertFalse(verification.isAuthentic(), "Recalled batch should not be marked as validly authentic for use");
    }
}
