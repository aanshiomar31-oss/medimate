package com.medimate.dto;

import com.medimate.model.BatchStatus;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class TransferCustodyRequest {

    @NotNull(message = "Target status is required")
    private BatchStatus targetStatus;

    @NotBlank(message = "New custodian/holder name is required")
    private String toHolder;

    @NotBlank(message = "New physical location is required")
    private String newLocation;

    private Double recordedTemperature;

    private String notes;

    public TransferCustodyRequest() {
    }

    public TransferCustodyRequest(BatchStatus targetStatus, String toHolder, String newLocation, Double recordedTemperature, String notes) {
        this.targetStatus = targetStatus;
        this.toHolder = toHolder;
        this.newLocation = newLocation;
        this.recordedTemperature = recordedTemperature;
        this.notes = notes;
    }

    public BatchStatus getTargetStatus() {
        return targetStatus;
    }

    public void setTargetStatus(BatchStatus targetStatus) {
        this.targetStatus = targetStatus;
    }

    public String getToHolder() {
        return toHolder;
    }

    public void setToHolder(String toHolder) {
        this.toHolder = toHolder;
    }

    public String getNewLocation() {
        return newLocation;
    }

    public void setNewLocation(String newLocation) {
        this.newLocation = newLocation;
    }

    public Double getRecordedTemperature() {
        return recordedTemperature;
    }

    public void setRecordedTemperature(Double recordedTemperature) {
        this.recordedTemperature = recordedTemperature;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
