package com.medimate.dto;

import com.medimate.model.BatchStatus;

import java.time.LocalDateTime;

public class CustodyEventDTO {

    private BatchStatus fromStatus;
    private BatchStatus toStatus;
    private String fromHolder;
    private String toHolder;
    private String location;
    private Double recordedTemperature;
    private boolean temperatureCompliant;
    private Double timeSpentHours;
    private boolean slaBreached;
    private String notes;
    private boolean verifiedAuthentic;
    private LocalDateTime timestamp;

    public CustodyEventDTO() {
    }

    public CustodyEventDTO(BatchStatus fromStatus, BatchStatus toStatus, String fromHolder, String toHolder,
                           String location, Double recordedTemperature, boolean temperatureCompliant,
                           Double timeSpentHours, boolean slaBreached, String notes, boolean verifiedAuthentic, LocalDateTime timestamp) {
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
        this.timestamp = timestamp;
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
