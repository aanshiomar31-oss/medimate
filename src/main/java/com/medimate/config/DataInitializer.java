package com.medimate.config;

import com.medimate.model.*;
import com.medimate.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final ManufacturerRepository manufacturerRepository;
    private final PharmacyRepository pharmacyRepository;
    private final MedicineRepository medicineRepository;
    private final MedicineBatchRepository batchRepository;
    private final SupplyShipmentRepository shipmentRepository;
    private final BatchCustodyLogRepository custodyLogRepository;

    public DataInitializer(ManufacturerRepository manufacturerRepository,
                           PharmacyRepository pharmacyRepository,
                           MedicineRepository medicineRepository,
                           MedicineBatchRepository batchRepository,
                           SupplyShipmentRepository shipmentRepository,
                           BatchCustodyLogRepository custodyLogRepository) {
        this.manufacturerRepository = manufacturerRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.medicineRepository = medicineRepository;
        this.batchRepository = batchRepository;
        this.shipmentRepository = shipmentRepository;
        this.custodyLogRepository = custodyLogRepository;
    }

    @Override
    public void run(String... args) {
        if (manufacturerRepository.count() > 0) {
            return;
        }

        log.info("--- Initializing MediMate Pharmaceutical Supply Chain Demo Data ---");

        // 1. Seed Manufacturers
        Manufacturer pfizer = manufacturerRepository.save(new Manufacturer("Pfizer Global Biologics", "FDA-MFG-94821", "United States", "supply@pfizer.com"));
        Manufacturer novartis = manufacturerRepository.save(new Manufacturer("Novartis Therapeutics AG", "EMA-MFG-33104", "Switzerland", "qa@novartis.com"));
        Manufacturer cipla = manufacturerRepository.save(new Manufacturer("Cipla Healthcare Ltd.", "CDSCO-MFG-77291", "India", "dispatch@cipla.com"));

        // 2. Seed Pharmacies
        Pharmacy apollo = pharmacyRepository.save(new Pharmacy("Apollo Hospital Central Pharmacy", "HOSPITAL_PHARMACY", "PHARM-APL-01", "Bangalore", "Bannerghatta Main Road", "Dr. Rajesh Nair", "rajesh.nair@apollohospitals.com"));
        Pharmacy manipal = pharmacyRepository.save(new Pharmacy("Manipal Critical Care Dispensary", "HOSPITAL_PHARMACY", "PHARM-MNP-04", "Bangalore", "HAL Airport Road", "Dr. Shalini Rao", "shalini.rao@manipal.edu"));
        Pharmacy medplus = pharmacyRepository.save(new Pharmacy("MedPlus Regional Distribution Hub", "COMMUNITY_RETAIL", "PHARM-MP-88", "Mumbai", "Andheri East Logistics Park", "Arun Verma", "arun.verma@medplus.com"));

        // 3. Seed Medicines
        Medicine insulin = medicineRepository.save(new Medicine("Humalog U-100", "Insulin Lispro", "Vial (10ml)", "Endocrinology", new BigDecimal("45.00"), true, 2.0, 8.0, 24));
        Medicine vaccine = medicineRepository.save(new Medicine("Comirnaty Bivalent", "COVID-19 mRNA Vaccine", "Vial (6 Doses)", "Immunization", new BigDecimal("28.00"), true, -25.0, -15.0, 12));
        Medicine augmentin = medicineRepository.save(new Medicine("Augmentin Duo 625mg", "Amoxicillin and Clavulanate", "Tablet Strip", "Antibiotics", new BigDecimal("12.50"), false, null, null, 36));
        Medicine lipitor = medicineRepository.save(new Medicine("Lipitor 20mg", "Atorvastatin Calcium", "Blister Pack", "Cardiovascular", new BigDecimal("22.00"), false, null, null, 36));

        LocalDateTime now = LocalDateTime.now();

        // 4. Batch 1: Completed Lifecycle (DELIVERED_TO_PHARMACY) with unbroken provenance
        MedicineBatch batch1 = new MedicineBatch(
                "MED-2026-PF01",
                insulin,
                pfizer,
                10000,
                LocalDate.now().minusMonths(3),
                LocalDate.now().plusMonths(21),
                "Apollo Hospital Central Pharmacy Cold Vault",
                "Dr. Rajesh Nair (Chief Pharmacist)"
        );
        batch1.setStatus(BatchStatus.DELIVERED_TO_PHARMACY);
        batch1.setCreatedAt(now.minusDays(10));
        batch1.setCurrentStageEnteredAt(now.minusDays(1));
        MedicineBatch savedBatch1 = batchRepository.save(batch1);

        custodyLogRepository.save(new BatchCustodyLog(savedBatch1, null, BatchStatus.MANUFACTURED, null, "Pfizer Production Line #4", "Pfizer Plant, Kalamazoo MI", 4.0, true, 0.0, false, "Manufactured and packaged", true));
        custodyLogRepository.save(new BatchCustodyLog(savedBatch1, BatchStatus.MANUFACTURED, BatchStatus.QUALITY_TESTED, "Pfizer Production Line #4", "Dr. Emily Watson (QA)", "Pfizer Analytical QC Lab", 4.2, true, 24.0, false, "Assay purity: 99.8%. Passed sterility testing", true));
        custodyLogRepository.save(new BatchCustodyLog(savedBatch1, BatchStatus.QUALITY_TESTED, BatchStatus.CENTRAL_DEPOT, "Dr. Emily Watson (QA)", "CryoLogistics Hub Lead", "National ColdChain Depot, Chicago", 4.1, true, 72.0, false, "Transferred to central cold vault", true));
        custodyLogRepository.save(new BatchCustodyLog(savedBatch1, BatchStatus.CENTRAL_DEPOT, BatchStatus.IN_TRANSIT, "CryoLogistics Hub Lead", "Carrier: AirPharm Express", "Flight QR-842 (Cold Container)", 3.8, true, 36.0, false, "Dispatched in active refrigerated container", true));
        custodyLogRepository.save(new BatchCustodyLog(savedBatch1, BatchStatus.IN_TRANSIT, BatchStatus.DELIVERED_TO_PHARMACY, "Carrier: AirPharm Express", "Dr. Rajesh Nair (Chief Pharmacist)", "Apollo Hospital Central Pharmacy Cold Vault", 4.5, true, 18.0, false, "Inspected on arrival. Datalogger verified 4.5°C", true));

        // 5. Batch 2: CRITICAL STALLED COLD-CHAIN SHIPMENT (Vaccine in transit > SLA with temperature breach!)
        MedicineBatch batch2 = new MedicineBatch(
                "MED-2026-CV02",
                vaccine,
                novartis,
                5000,
                LocalDate.now().minusMonths(1),
                LocalDate.now().plusMonths(11),
                "In Transit — CryoTruck #882 (En route to Manipal)",
                "Driver: Vikram Singh (CryoTrans Logistics)"
        );
        batch2.setStatus(BatchStatus.IN_TRANSIT);
        batch2.setCreatedAt(now.minusHours(45));
        batch2.setCurrentStageEnteredAt(now.minusHours(38)); // 38h > 24h SLA!
        batch2.setSlaBreached(true);
        MedicineBatch savedBatch2 = batchRepository.save(batch2);

        custodyLogRepository.save(new BatchCustodyLog(savedBatch2, null, BatchStatus.MANUFACTURED, null, "Novartis Biologics Facility", "Basel, Switzerland", -20.0, true, 0.0, false, "mRNA formulation completed", true));
        custodyLogRepository.save(new BatchCustodyLog(savedBatch2, BatchStatus.MANUFACTURED, BatchStatus.QUALITY_TESTED, "Novartis Biologics Facility", "Novartis QA Release", "Basel QC Center", -20.5, true, 12.0, false, "Cold integrity verified", true));
        custodyLogRepository.save(new BatchCustodyLog(savedBatch2, BatchStatus.QUALITY_TESTED, BatchStatus.CENTRAL_DEPOT, "Novartis QA Release", "Depot Supervisor", "Novartis Regional Cold Hub", -19.8, true, 48.0, false, "Packed with dry ice shippers", true));
        custodyLogRepository.save(new BatchCustodyLog(savedBatch2, BatchStatus.CENTRAL_DEPOT, BatchStatus.IN_TRANSIT, "Depot Supervisor", "Driver: Vikram Singh", "CryoTruck #882", -12.0, false, 38.0, true, "WARNING: Temperature spiked to -12.0°C (Limit: -15.0°C). Carrier stuck in customs delay", false));

        SupplyShipment shipment2 = new SupplyShipment("SHIP-202609-002", savedBatch2, manipal, "Novartis Regional Cold Hub", "CryoTrans Logistics", 24);
        shipment2.setDispatchedAt(now.minusHours(38));
        shipment2.setLastRecordedTemperature(-12.0);
        shipment2.setTemperatureExcursion(true);
        shipment2.setSlaBreached(true);
        shipmentRepository.save(shipment2);

        // 6. Batch 3: EXPIRING BATCH (Augmentin expiring within 35 days!)
        MedicineBatch batch3 = new MedicineBatch(
                "MED-2026-AG03",
                augmentin,
                cipla,
                25000,
                LocalDate.now().minusMonths(35),
                LocalDate.now().plusDays(35), // Expiring in 35 days!
                "Cipla Central Distribution Warehouse",
                "Warehouse Manager: Rajesh K."
        );
        batch3.setStatus(BatchStatus.CENTRAL_DEPOT);
        batch3.setCreatedAt(now.minusDays(60));
        batch3.setCurrentStageEnteredAt(now.minusDays(20));
        MedicineBatch savedBatch3 = batchRepository.save(batch3);

        custodyLogRepository.save(new BatchCustodyLog(savedBatch3, null, BatchStatus.MANUFACTURED, null, "Cipla Plant #2", "Goa, India", null, true, 0.0, false, "Standard solid dosage batch produced", true));
        custodyLogRepository.save(new BatchCustodyLog(savedBatch3, BatchStatus.MANUFACTURED, BatchStatus.QUALITY_TESTED, "Cipla Plant #2", "Cipla QC Team", "Goa Lab", null, true, 16.0, false, "Passed dissolution test", true));
        custodyLogRepository.save(new BatchCustodyLog(savedBatch3, BatchStatus.QUALITY_TESTED, BatchStatus.CENTRAL_DEPOT, "Cipla QC Team", "Warehouse Manager: Rajesh K.", "Cipla Central Distribution Warehouse", null, true, 120.0, false, "Stored in ambient warehouse", true));

        // 7. Batch 4: EMERGENCY RECALLED BATCH (Lipitor)
        MedicineBatch batch4 = new MedicineBatch(
                "MED-2026-LP04",
                lipitor,
                pfizer,
                40000,
                LocalDate.now().minusMonths(6),
                LocalDate.now().plusMonths(30),
                "Pfizer National Logistics Center [QUARANTINE ZONE]",
                "Regulatory Quarantine Custodian (FDA Liaison)"
        );
        batch4.setStatus(BatchStatus.RECALLED);
        batch4.setRecalled(true);
        batch4.setRecallReason("Voluntary recall due to sub-potency observed in accelerated 6-month stability testing");
        batch4.setCreatedAt(now.minusDays(180));
        batch4.setCurrentStageEnteredAt(now.minusHours(4));
        MedicineBatch savedBatch4 = batchRepository.save(batch4);

        custodyLogRepository.save(new BatchCustodyLog(savedBatch4, null, BatchStatus.MANUFACTURED, null, "Pfizer Production", "Pfizer Plant", null, true, 0.0, false, "Batch manufactured", true));
        custodyLogRepository.save(new BatchCustodyLog(savedBatch4, BatchStatus.MANUFACTURED, BatchStatus.QUALITY_TESTED, "Pfizer Production", "Pfizer QA", "Pfizer QC Lab", null, true, 20.0, false, "Initial release approved", true));
        custodyLogRepository.save(new BatchCustodyLog(savedBatch4, BatchStatus.QUALITY_TESTED, BatchStatus.CENTRAL_DEPOT, "Pfizer QA", "Warehouse Lead", "Pfizer Depot", null, true, 400.0, false, "In stock", true));
        custodyLogRepository.save(new BatchCustodyLog(savedBatch4, BatchStatus.CENTRAL_DEPOT, BatchStatus.RECALLED, "Warehouse Lead", "Regulatory Quarantine Custodian (FDA Liaison)", "Pfizer National Logistics Center [QUARANTINE ZONE]", null, false, 4.0, true, "URGENT RECALL: Sub-potency observed in accelerated 6-month stability testing", true));

        log.info("--- MediMate Initialized Successfully: 3 Manufacturers, 3 Pharmacies, 4 Medicines, 4 Batches Seeded ---");
    }
}
