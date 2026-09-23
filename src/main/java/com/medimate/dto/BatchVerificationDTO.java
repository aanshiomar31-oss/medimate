package com.medimate.dto;

import com.medimate.model.BatchStatus;

import java.time.LocalDate;
import java.util.List;

public class BatchVerificationDTO {

    private String batchNumber;
    private String medicineName;
    private String genericName;
    private String category;
    private String manufacturerName;
    private String manufacturerLicense;
    private BatchStatus currentStatus;
    private String currentLocation;
    private String currentHolder;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
    private boolean isExpired;
    private boolean isRecalled;
    private String recallReason;
    private boolean isAuthentic;
    private boolean coldChainCompliant;
    private List<CustodyEventDTO> chainOfCustody;

    public BatchVerificationDTO() {
    }

    public BatchVerificationDTO(String batchNumber, String medicineName, String genericName, String category,
                                String manufacturerName, String manufacturerLicense, BatchStatus currentStatus,
                                String currentLocation, String currentHolder, LocalDate manufacturingDate,
                                LocalDate expiryDate, boolean isExpired, boolean isRecalled, String recallReason,
                                boolean isAuthentic, boolean coldChainCompliant, List<CustodyEventDTO> chainOfCustody) {
        this.batchNumber = batchNumber;
        this.medicineName = medicineName;
        this.genericName = genericName;
        this.category = category;
        this.manufacturerName = manufacturerName;
        this.manufacturerLicense = manufacturerLicense;
        this.currentStatus = currentStatus;
        this.currentLocation = currentLocation;
        this.currentHolder = currentHolder;
        this.manufacturingDate = manufacturingDate;
        this.expiryDate = expiryDate;
        this.isExpired = isExpired;
        this.isRecalled = isRecalled;
        this.recallReason = recallReason;
        this.isAuthentic = isAuthentic;
        this.coldChainCompliant = coldChainCompliant;
        this.chainOfCustody = chainOfCustody;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    public String getManufacturerLicense() {
        return manufacturerLicense;
    }

    public void setManufacturerLicense(String manufacturerLicense) {
        this.manufacturerLicense = manufacturerLicense;
    }

    public BatchStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(BatchStatus currentStatus) {
        this.currentStatus = currentStatus;
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

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean expired) {
        isExpired = expired;
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

    public boolean isAuthentic() {
        return isAuthentic;
    }

    public void setAuthentic(boolean authentic) {
        isAuthentic = authentic;
    }

    public boolean isColdChainCompliant() {
        return coldChainCompliant;
    }

    public void setColdChainCompliant(boolean coldChainCompliant) {
        this.coldChainCompliant = coldChainCompliant;
    }

    public List<CustodyEventDTO> getChainOfCustody() {
        return chainOfCustody;
    }

    public void setChainOfCustody(List<CustodyEventDTO> chainOfCustody) {
        this.chainOfCustody = chainOfCustody;
    }
}
