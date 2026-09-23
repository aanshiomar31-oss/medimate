package com.medimate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Immutable Event Audit Log for pharmaceutical batch custody transitions.
 * Supports Process Mining analysis, SLA adherence, and anti-counterfeit verification.
 */
@Entity
@Table(name = "batch_custody_logs")
public class BatchCustodyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    @JsonIgnore
    private MedicineBatch batch;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 30)
    private BatchStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 30)
    private BatchStatus toStatus;

    @Column(name = "from_holder", length = 100)
    private String fromHolder;

    @Column(name = "to_holder", nullable = false, length = 100)
    private String toHolder;

    @Column(nullable = false, length = 120)
    private String location;

    @Column(name = "recorded_temp_celsius")
    private Double recordedTemperature;

    @Column(name = "temp_compliant")
    private boolean temperatureCompliant = true;

    @Column(name = "time_spent_hours")
    private Double timeSpentHours;

    @Column(name = "sla_breached")
    private boolean slaBreached = false;

    @Column(length = 255)
    private String notes;

    @Column(name = "verified_authentic", nullable = false)
    private boolean verifiedAuthentic = true;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public BatchCustodyLog() {
    }

    public BatchCustodyLog(MedicineBatch batch, BatchStatus fromStatus, BatchStatus toStatus, String fromHolder,
                           String toHolder, String location, Double recordedTemperature, boolean temperatureCompliant,
                           Double timeSpentHours, boolean slaBreached, String notes, boolean verifiedAuthentic) {
        this.batch = batch;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.fromHolder = fromHolder;
        this.toHolder = toHolder;
        this.location = location;
        this.recordedTemperature = recordedTemperature;
        this.temperatureCompliant = temperatureCompliant;
        this.timeSpentHours = timeSpentHours;
        this.slaBreached = slaBreached;
        this.notes = notes;
        this.verifiedAuthentic = verifiedAuthentic;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MedicineBatch getBatch() {
        return batch;
    }

    public void setBatch(MedicineBatch batch) {
        this.batch = batch;
    }

    public BatchStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(BatchStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public BatchStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(BatchStatus toStatus) {
        this.toStatus = toStatus;
    }

    public String getFromHolder() {
        return fromHolder;
    }

    public void setFromHolder(String fromHolder) {
        this.fromHolder = fromHolder;
    }

    public String getToHolder() {
        return toHolder;
    }

    public void setToHolder(String toHolder) {
        this.toHolder = toHolder;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getRecordedTemperature() {
        return recordedTemperature;
    }

    public void setRecordedTemperature(Double recordedTemperature) {
        this.recordedTemperature = recordedTemperature;
    }

    public boolean isTemperatureCompliant() {
        return temperatureCompliant;
    }

    public void setTemperatureCompliant(boolean temperatureCompliant) {
        this.temperatureCompliant = temperatureCompliant;
    }

    public Double getTimeSpentHours() {
        return timeSpentHours;
    }

    public void setTimeSpentHours(Double timeSpentHours) {
        this.timeSpentHours = timeSpentHours;
    }

    public boolean isSlaBreached() {
        return slaBreached;
    }

    public void setSlaBreached(boolean slaBreached) {
        this.slaBreached = slaBreached;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isVerifiedAuthentic() {
        return verifiedAuthentic;
    }

    public void setVerifiedAuthentic(boolean verifiedAuthentic) {
        this.verifiedAuthentic = verifiedAuthentic;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
