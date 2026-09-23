package com.medimate.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "supply_shipments")
public class SupplyShipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Shipment tracking number is required")
    @Column(name = "shipment_number", nullable = false, unique = true, length = 64)
    private String shipmentNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "batch_id", nullable = false)
    private MedicineBatch batch;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "destination_pharmacy_id", nullable = false)
    private Pharmacy destinationPharmacy;

    @Column(name = "source_location", nullable = false, length = 120)
    private String sourceLocation;

    @Column(name = "carrier_name", nullable = false, length = 100)
    private String carrierName; // e.g. "CryoTrans ColdChain Fleet", "MediSpeed Freight"

    @Column(nullable = false, length = 40)
    private String status = "IN_TRANSIT"; // "SCHEDULED", "IN_TRANSIT", "DELIVERED", "TEMPERATURE_BREACH_ALERT"

    @Column(name = "transit_sla_hours", nullable = false)
    private Integer transitSlaHours = 24;

    @Column(name = "dispatched_at", nullable = false)
    private LocalDateTime dispatchedAt = LocalDateTime.now();

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "last_recorded_temp_celsius")
    private Double lastRecordedTemperature;

    @Column(name = "temperature_excursion", nullable = false)
    private boolean temperatureExcursion = false;

    @Column(name = "sla_breached", nullable = false)
    private boolean slaBreached = false;

    public SupplyShipment() {
    }

    public SupplyShipment(String shipmentNumber, MedicineBatch batch, Pharmacy destinationPharmacy,
                          String sourceLocation, String carrierName, Integer transitSlaHours) {
        this.shipmentNumber = shipmentNumber;
        this.batch = batch;
        this.destinationPharmacy = destinationPharmacy;
        this.sourceLocation = sourceLocation;
        this.carrierName = carrierName;
        this.transitSlaHours = transitSlaHours != null ? transitSlaHours : 24;
        this.status = "IN_TRANSIT";
        this.dispatchedAt = LocalDateTime.now();
        this.slaBreached = false;
        this.temperatureExcursion = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShipmentNumber() {
        return shipmentNumber;
    }

    public void setShipmentNumber(String shipmentNumber) {
        this.shipmentNumber = shipmentNumber;
    }

    public MedicineBatch getBatch() {
        return batch;
    }

    public void setBatch(MedicineBatch batch) {
        this.batch = batch;
    }

    public Pharmacy getDestinationPharmacy() {
        return destinationPharmacy;
    }

    public void setDestinationPharmacy(Pharmacy destinationPharmacy) {
        this.destinationPharmacy = destinationPharmacy;
    }

    public String getSourceLocation() {
        return sourceLocation;
    }

    public void setSourceLocation(String sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    public String getCarrierName() {
        return carrierName;
    }

    public void setCarrierName(String carrierName) {
        this.carrierName = carrierName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getTransitSlaHours() {
        return transitSlaHours;
    }

    public void setTransitSlaHours(Integer transitSlaHours) {
        this.transitSlaHours = transitSlaHours;
    }

    public LocalDateTime getDispatchedAt() {
        return dispatchedAt;
    }

    public void setDispatchedAt(LocalDateTime dispatchedAt) {
        this.dispatchedAt = dispatchedAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
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

    public boolean isSlaBreached() {
        return slaBreached;
    }

    public void setSlaBreached(boolean slaBreached) {
        this.slaBreached = slaBreached;
    }
}
