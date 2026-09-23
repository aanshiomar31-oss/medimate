package com.medimate.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "medicine_batches")
public class MedicineBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Batch number is required")
    @Column(name = "batch_number", nullable = false, unique = true, length = 64)
    private String batchNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "manufacturer_id", nullable = false)
    private Manufacturer manufacturer;

    @Min(value = 1, message = "Initial quantity must be at least 1")
    @Column(name = "initial_quantity", nullable = false)
    private Integer initialQuantity;

    @Min(value = 0, message = "Available quantity cannot be negative")
    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @Column(name = "manufacturing_date", nullable = false)
    private LocalDate manufacturingDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BatchStatus status = BatchStatus.MANUFACTURED;

    @Column(name = "current_location", nullable = false, length = 120)
    private String currentLocation;

    @Column(name = "current_holder", nullable = false, length = 100)
    private String currentHolder;

    @Column(name = "current_stage_entered_at", nullable = false)
    private LocalDateTime currentStageEnteredAt = LocalDateTime.now();

    @Column(name = "sla_breached", nullable = false)
    private boolean slaBreached = false;

    @Column(name = "is_recalled", nullable = false)
    private boolean isRecalled = false;

    @Column(name = "recall_reason", length = 255)
    private String recallReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("timestamp ASC")
    @JsonIgnoreProperties("batch")
    private List<BatchCustodyLog> custodyLogs = new ArrayList<>();

    public MedicineBatch() {
    }

    public MedicineBatch(String batchNumber, Medicine medicine, Manufacturer manufacturer,
                         Integer initialQuantity, LocalDate manufacturingDate, LocalDate expiryDate,
                         String currentLocation, String currentHolder) {
        this.batchNumber = batchNumber;
        this.medicine = medicine;
        this.manufacturer = manufacturer;
        this.initialQuantity = initialQuantity;
        this.availableQuantity = initialQuantity;
        this.manufacturingDate = manufacturingDate;
        this.expiryDate = expiryDate;
        this.currentLocation = currentLocation;
        this.currentHolder = currentHolder;
        this.status = BatchStatus.MANUFACTURED;
        this.currentStageEnteredAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.slaBreached = false;
        this.isRecalled = false;
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(this.expiryDate);
    }

    public boolean isExpiringWithinDays(int days) {
        LocalDate threshold = LocalDate.now().plusDays(days);
        return !isExpired() && this.expiryDate.isBefore(threshold);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
    }

    public Manufacturer getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(Manufacturer manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Integer getInitialQuantity() {
        return initialQuantity;
    }

    public void setInitialQuantity(Integer initialQuantity) {
        this.initialQuantity = initialQuantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public LocalDate getManufacturingDate() {
        return manufacturingDate;
    }

    public void setManufacturingDate(LocalDate manufacturingDate) {
        this.manufacturingDate = manufacturingDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public BatchStatus getStatus() {
        return status;
    }

    public void setStatus(BatchStatus status) {
        this.status = status;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public String getCurrentHolder() {
        return currentHolder;
    }

    public void setCurrentHolder(String currentHolder) {
        this.currentHolder = currentHolder;
    }

    public LocalDateTime getCurrentStageEnteredAt() {
        return currentStageEnteredAt;
    }

    public void setCurrentStageEnteredAt(LocalDateTime currentStageEnteredAt) {
        this.currentStageEnteredAt = currentStageEnteredAt;
    }

    public boolean isSlaBreached() {
        return slaBreached;
    }

    public void setSlaBreached(boolean slaBreached) {
        this.slaBreached = slaBreached;
    }

    public boolean isRecalled() {
        return isRecalled;
    }

    public void setRecalled(boolean recalled) {
        isRecalled = recalled;
    }

    public String getRecallReason() {
        return recallReason;
    }

    public void setRecallReason(String recallReason) {
        this.recallReason = recallReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<BatchCustodyLog> getCustodyLogs() {
        return custodyLogs;
    }

    public void setCustodyLogs(List<BatchCustodyLog> custodyLogs) {
        this.custodyLogs = custodyLogs;
    }
}
