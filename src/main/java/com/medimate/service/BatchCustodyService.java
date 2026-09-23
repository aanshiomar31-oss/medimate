package com.medimate.service;

import com.medimate.dto.CreateBatchRequest;
import com.medimate.dto.RecallRequest;
import com.medimate.dto.TransferCustodyRequest;
import com.medimate.model.*;
import com.medimate.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class BatchCustodyService {

    private final MedicineBatchRepository batchRepository;
    private final MedicineRepository medicineRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final BatchCustodyLogRepository custodyLogRepository;

    public BatchCustodyService(MedicineBatchRepository batchRepository,
                               MedicineRepository medicineRepository,
                               ManufacturerRepository manufacturerRepository,
                               BatchCustodyLogRepository custodyLogRepository) {
        this.batchRepository = batchRepository;
        this.medicineRepository = medicineRepository;
        this.manufacturerRepository = manufacturerRepository;
        this.custodyLogRepository = custodyLogRepository;
    }

    public MedicineBatch createBatch(CreateBatchRequest request) {
        Medicine medicine = medicineRepository.findById(request.getMedicineId())
                .orElseThrow(() -> new IllegalArgumentException("Medicine not found with ID: " + request.getMedicineId()));

        Manufacturer manufacturer = manufacturerRepository.findById(request.getManufacturerId())
                .orElseThrow(() -> new IllegalArgumentException("Manufacturer not found with ID: " + request.getManufacturerId()));

        String batchNumber = "BATCH-" + LocalDateTime.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        MedicineBatch batch = new MedicineBatch(
                batchNumber,
                medicine,
                manufacturer,
                request.getInitialQuantity(),
                request.getManufacturingDate(),
                request.getExpiryDate(),
                request.getCurrentLocation(),
                request.getCurrentHolder()
        );

        MedicineBatch savedBatch = batchRepository.save(batch);

        // Record initial manufacturing event in immutable audit log
        BatchCustodyLog initialLog = new BatchCustodyLog(
                savedBatch,
                null,
                BatchStatus.MANUFACTURED,
                null,
                request.getCurrentHolder(),
                request.getCurrentLocation(),
                medicine.isRequiresColdChain() ? medicine.getMinTemperatureCelsius() + 2.0 : null,
                true,
                0.0,
                false,
                "Batch manufactured and registered under license " + manufacturer.getLicenseNumber(),
                true
        );
        custodyLogRepository.save(initialLog);

        return savedBatch;
    }

    public MedicineBatch transferCustody(Long batchId, TransferCustodyRequest request) {
        MedicineBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Medicine batch not found with ID: " + batchId));

        if (batch.isRecalled()) {
            throw new IllegalStateException("Cannot transfer custody: Batch " + batch.getBatchNumber() + " is under active RECALL quarantine.");
        }

        BatchStatus currentStatus = batch.getStatus();
        BatchStatus targetStatus = request.getTargetStatus();

        if (!currentStatus.canTransitionTo(targetStatus)) {
            throw new IllegalStateException(String.format(
                    "Illegal supply chain transition: Cannot move batch %s from %s to %s",
                    batch.getBatchNumber(), currentStatus, targetStatus));
        }

        LocalDateTime now = LocalDateTime.now();
        double hoursSpent = Duration.between(batch.getCurrentStageEnteredAt(), now).toMinutes() / 60.0;
        hoursSpent = Math.round(hoursSpent * 10.0) / 10.0;

        // Cold-chain temperature compliance check
        boolean tempCompliant = batch.getMedicine().isTemperatureCompliant(request.getRecordedTemperature());
        boolean slaBreached = !tempCompliant;

        if (!tempCompliant) {
            batch.setSlaBreached(true);
        }

        String previousHolder = batch.getCurrentHolder();
        batch.setStatus(targetStatus);
        batch.setCurrentHolder(request.getToHolder());
        batch.setCurrentLocation(request.getNewLocation());
        batch.setCurrentStageEnteredAt(now);

        MedicineBatch updatedBatch = batchRepository.save(batch);

        BatchCustodyLog auditLog = new BatchCustodyLog(
                updatedBatch,
                currentStatus,
                targetStatus,
                previousHolder,
                request.getToHolder(),
                request.getNewLocation(),
                request.getRecordedTemperature(),
                tempCompliant,
                hoursSpent,
                slaBreached,
                request.getNotes() != null ? request.getNotes() : "Custody transferred to " + request.getToHolder(),
                true
        );
        custodyLogRepository.save(auditLog);

        return updatedBatch;
    }

    public MedicineBatch recallBatch(Long batchId, RecallRequest request) {
        MedicineBatch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new IllegalArgumentException("Medicine batch not found with ID: " + batchId));

        BatchStatus previousStatus = batch.getStatus();
        batch.setStatus(BatchStatus.RECALLED);
        batch.setRecalled(true);
        batch.setRecallReason(request.getRecallReason());
        batch.setCurrentStageEnteredAt(LocalDateTime.now());

        MedicineBatch updated = batchRepository.save(batch);

        BatchCustodyLog recallLog = new BatchCustodyLog(
                updated,
                previousStatus,
                BatchStatus.RECALLED,
                batch.getCurrentHolder(),
                "Regulatory Quarantine Custodian (" + request.getAuthorizedBy() + ")",
                batch.getCurrentLocation() + " [QUARANTINE ZONE]",
                null,
                false,
                0.0,
                true,
                "URGENT RECALL: " + request.getRecallReason() + " (Authorized by: " + request.getAuthorizedBy() + ")",
                true
        );
        custodyLogRepository.save(recallLog);

        return updated;
    }

    @Transactional(readOnly = true)
    public MedicineBatch getBatchById(Long id) {
        return batchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Medicine batch not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<MedicineBatch> getAllBatches(BatchStatus status, Boolean activeOnly) {
        if (Boolean.TRUE.equals(activeOnly)) {
            return batchRepository.findActiveBatches();
        }
        if (status != null) {
            return batchRepository.findByStatus(status);
        }
        return batchRepository.findAll();
    }
}
