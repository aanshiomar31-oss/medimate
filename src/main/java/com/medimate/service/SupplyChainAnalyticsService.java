package com.medimate.service;

import com.medimate.dto.BatchVerificationDTO;
import com.medimate.dto.CustodyEventDTO;
import com.medimate.dto.StageLatencyDTO;
import com.medimate.dto.StalledShipmentDTO;
import com.medimate.model.BatchCustodyLog;
import com.medimate.model.BatchStatus;
import com.medimate.model.MedicineBatch;
import com.medimate.model.SupplyShipment;
import com.medimate.repository.BatchCustodyLogRepository;
import com.medimate.repository.MedicineBatchRepository;
import com.medimate.repository.SupplyShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SupplyChainAnalyticsService {

    private final MedicineBatchRepository batchRepository;
    private final BatchCustodyLogRepository custodyLogRepository;
    private final SupplyShipmentRepository shipmentRepository;

    public SupplyChainAnalyticsService(MedicineBatchRepository batchRepository,
                                       BatchCustodyLogRepository custodyLogRepository,
                                       SupplyShipmentRepository shipmentRepository) {
        this.batchRepository = batchRepository;
        this.custodyLogRepository = custodyLogRepository;
        this.shipmentRepository = shipmentRepository;
    }

    /**
     * Anti-Counterfeit Verification: verifies unbroken provenance chain and cold-chain compliance.
     */
    public BatchVerificationDTO verifyBatchProvenance(String batchNumber) {
        MedicineBatch batch = batchRepository.findByBatchNumber(batchNumber)
                .orElseThrow(() -> new IllegalArgumentException("No pharmaceutical batch found with number: " + batchNumber));

        List<BatchCustodyLog> logs = custodyLogRepository.findByBatchBatchNumberOrderByTimestampAsc(batchNumber);

        boolean continuousChain = true;
        boolean coldChainCompliant = true;

        if (logs.isEmpty() || logs.get(0).getToStatus() != BatchStatus.MANUFACTURED) {
            continuousChain = false;
        }

        for (int i = 1; i < logs.size(); i++) {
            BatchCustodyLog prev = logs.get(i - 1);
            BatchCustodyLog current = logs.get(i);

            // Verify continuous unbroken chain-of-custody
            if (current.getFromStatus() != prev.getToStatus()) {
                continuousChain = false;
            }

            if (!current.isTemperatureCompliant()) {
                coldChainCompliant = false;
            }
        }

        List<CustodyEventDTO> eventDTOs = logs.stream()
                .map(l -> new CustodyEventDTO(
                        l.getFromStatus(),
                        l.getToStatus(),
                        l.getFromHolder(),
                        l.getToHolder(),
                        l.getLocation(),
                        l.getRecordedTemperature(),
                        l.isTemperatureCompliant(),
                        l.getTimeSpentHours(),
                        l.isSlaBreached(),
                        l.getNotes(),
                        l.isVerifiedAuthentic(),
                        l.getTimestamp()))
                .collect(Collectors.toList());

        return new BatchVerificationDTO(
                batch.getBatchNumber(),
                batch.getMedicine().getName(),
                batch.getMedicine().getGenericName(),
                batch.getMedicine().getCategory(),
                batch.getManufacturer().getName(),
                batch.getManufacturer().getLicenseNumber(),
                batch.getStatus(),
                batch.getCurrentLocation(),
                batch.getCurrentHolder(),
                batch.getManufacturingDate(),
                batch.getExpiryDate(),
                batch.isExpired(),
                batch.isRecalled(),
                batch.getRecallReason(),
                continuousChain && !batch.isRecalled(),
                coldChainCompliant,
                eventDTOs
        );
    }

    /**
     * Identifies shipments delayed in transit or experiencing cold-chain temperature excursions.
     */
    public List<StalledShipmentDTO> findStalledShipments() {
        List<SupplyShipment> activeShipments = shipmentRepository.findActiveShipments();
        LocalDateTime now = LocalDateTime.now();
        List<StalledShipmentDTO> stalledList = new ArrayList<>();

        for (SupplyShipment shipment : activeShipments) {
            long hoursInTransit = Math.max(0, Duration.between(shipment.getDispatchedAt(), now).toHours());
            int slaHours = shipment.getTransitSlaHours();

            boolean isOverdue = hoursInTransit > slaHours;
            boolean tempBreach = shipment.isTemperatureExcursion();

            if (isOverdue || tempBreach) {
                stalledList.add(new StalledShipmentDTO(
                        shipment.getId(),
                        shipment.getShipmentNumber(),
                        shipment.getBatch().getBatchNumber(),
                        shipment.getBatch().getMedicine().getName(),
                        shipment.getDestinationPharmacy().getName(),
                        shipment.getCarrierName(),
                        shipment.getDispatchedAt(),
                        hoursInTransit,
                        slaHours,
                        shipment.getBatch().getMedicine().isRequiresColdChain(),
                        shipment.getLastRecordedTemperature(),
                        tempBreach
                ));
            }
        }

        // Sort by hours overdue descending
        stalledList.sort((a, b) -> Long.compare(b.getHoursOverdue(), a.getHoursOverdue()));
        return stalledList;
    }

    /**
     * Finds active batches approaching expiration within the given number of days.
     */
    public List<MedicineBatch> findExpiringBatches(int days) {
        LocalDate cutoff = LocalDate.now().plusDays(days);
        return batchRepository.findExpiringBatches(cutoff);
    }

    /**
     * Process Mining latency metrics across manufacturing, warehousing, transit, and dispensing.
     */
    public List<StageLatencyDTO> getStageLatencyAnalytics() {
        List<Object[]> rawMetrics = custodyLogRepository.findStageLatencyMetrics();
        List<StageLatencyDTO> dtoList = new ArrayList<>();

        for (Object[] row : rawMetrics) {
            BatchStatus stage = (BatchStatus) row[0];
            long count = ((Number) row[1]).longValue();
            double avgHours = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
            double maxHours = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
            long breaches = row[4] != null ? ((Number) row[4]).longValue() : 0L;

            dtoList.add(new StageLatencyDTO(stage, count, avgHours, maxHours, breaches));
        }

        // Sort by average duration descending to highlight bottlenecks
        dtoList.sort((a, b) -> Double.compare(b.getAverageDurationHours(), a.getAverageDurationHours()));
        return dtoList;
    }

    /**
     * Executive health summary of the pharmaceutical supply network.
     */
    public Map<String, Object> getDashboardSummary() {
        List<MedicineBatch> activeBatches = batchRepository.findActiveBatches();
        List<StalledShipmentDTO> stalledShipments = findStalledShipments();
        List<MedicineBatch> expiringBatches = findExpiringBatches(60);
        long recalledCount = batchRepository.findByIsRecalled(true).size();
        long activeShipmentsCount = shipmentRepository.findActiveShipments().size();

        double complianceRate = activeShipmentsCount == 0 ? 100.0 :
                Math.round(((double) (activeShipmentsCount - stalledShipments.size()) / activeShipmentsCount * 100.0) * 10.0) / 10.0;

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("activeBatchesCount", activeBatches.size());
        summary.put("activeShipmentsCount", activeShipmentsCount);
        summary.put("stalledShipmentsCount", stalledShipments.size());
        summary.put("transitComplianceRatePercent", complianceRate);
        summary.put("expiringWithin60DaysCount", expiringBatches.size());
        summary.put("activeRecallsCount", recalledCount);
        summary.put("supplyChainStatus", stalledShipments.isEmpty() && recalledCount == 0 ? "OPTIMAL" : "ACTION_REQUIRED");

        return summary;
    }
}
