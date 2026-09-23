package com.medimate.dto;

import java.time.LocalDateTime;

public class StalledShipmentDTO {

    private Long shipmentId;
    private String shipmentNumber;
    private String batchNumber;
    private String medicineName;
    private String destinationPharmacy;
    private String carrierName;
    private LocalDateTime dispatchedAt;
    private long hoursInTransit;
    private int transitSlaHours;
    private long hoursOverdue;
    private boolean requiresColdChain;
    private Double lastRecordedTemperature;
    private boolean temperatureExcursion;
    private String bottleneckSeverity; // "MODERATE", "CRITICAL"

    public StalledShipmentDTO() {
    }

    public StalledShipmentDTO(Long shipmentId, String shipmentNumber, String batchNumber, String medicineName,
                              String destinationPharmacy, String carrierName, LocalDateTime dispatchedAt,
                              long hoursInTransit, int transitSlaHours, boolean requiresColdChain,
                              Double lastRecordedTemperature, boolean temperatureExcursion) {
        this.shipmentId = shipmentId;
        this.shipmentNumber = shipmentNumber;
        this.batchNumber = batchNumber;
        this.medicineName = medicineName;
        this.destinationPharmacy = destinationPharmacy;
        this.carrierName = carrierName;
        this.dispatchedAt = dispatchedAt;
        this.hoursInTransit = hoursInTransit;
        this.transitSlaHours = transitSlaHours;
        this.hoursOverdue = Math.max(0, hoursInTransit - transitSlaHours);
        this.requiresColdChain = requiresColdChain;
        this.lastRecordedTemperature = lastRecordedTemperature;
        this.temperatureExcursion = temperatureExcursion;
        this.bottleneckSeverity = (temperatureExcursion || hoursOverdue > (transitSlaHours / 2)) ? "CRITICAL" : "MODERATE";
    }

    public Long getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(Long shipmentId) {
        this.shipmentId = shipmentId;
    }

    public String getShipmentNumber() {
        return shipmentNumber;
    }

    public void setShipmentNumber(String shipmentNumber) {
        this.shipmentNumber = shipmentNumber;
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

    public String getDestinationPharmacy() {
        return destinationPharmacy;
    }

    public void setDestinationPharmacy(String destinationPharmacy) {
        this.destinationPharmacy = destinationPharmacy;
    }

    public String getCarrierName() {
        return carrierName;
    }

    public void setCarrierName(String carrierName) {
        this.carrierName = carrierName;
    }

    public LocalDateTime getDispatchedAt() {
        return dispatchedAt;
    }

    public void setDispatchedAt(LocalDateTime dispatchedAt) {
        this.dispatchedAt = dispatchedAt;
    }

    public long getHoursInTransit() {
        return hoursInTransit;
    }

    public void setHoursInTransit(long hoursInTransit) {
        this.hoursInTransit = hoursInTransit;
    }

    public int getTransitSlaHours() {
        return transitSlaHours;
    }

    public void setTransitSlaHours(int transitSlaHours) {
        this.transitSlaHours = transitSlaHours;
    }

    public long getHoursOverdue() {
        return hoursOverdue;
    }

    public void setHoursOverdue(long hoursOverdue) {
        this.hoursOverdue = hoursOverdue;
    }

    public boolean isRequiresColdChain() {
        return requiresColdChain;
    }

    public void setRequiresColdChain(boolean requiresColdChain) {
        this.requiresColdChain = requiresColdChain;
    }

    public Double getLastRecordedTemperature() {
        return lastRecordedTemperature;
    }

    public void setLastRecordedTemperature(Double lastRecordedTemperature) {
        this.lastRecordedTemperature = lastRecordedTemperature;
    }

    public boolean isTemperatureExcursion() {
        return temperatureExcursion;
    }

    public void setTemperatureExcursion(boolean temperatureExcursion) {
        this.temperatureExcursion = temperatureExcursion;
    }

    public String getBottleneckSeverity() {
        return bottleneckSeverity;
    }

    public void setBottleneckSeverity(String bottleneckSeverity) {
        this.bottleneckSeverity = bottleneckSeverity;
    }
}
