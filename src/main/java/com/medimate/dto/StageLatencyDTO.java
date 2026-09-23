package com.medimate.dto;

import com.medimate.model.BatchStatus;

public class StageLatencyDTO {

    private BatchStatus stage;
    private long totalTransitions;
    private double averageDurationHours;
    private double maxDurationHours;
    private long slaBreachCount;
    private double slaBreachPercentage;

    public StageLatencyDTO() {
    }

    public StageLatencyDTO(BatchStatus stage, long totalTransitions, double averageDurationHours,
                           double maxDurationHours, long slaBreachCount) {
        this.stage = stage;
        this.totalTransitions = totalTransitions;
        this.averageDurationHours = Math.round(averageDurationHours * 10.0) / 10.0;
        this.maxDurationHours = Math.round(maxDurationHours * 10.0) / 10.0;
        this.slaBreachCount = slaBreachCount;
        this.slaBreachPercentage = (totalTransitions > 0)
                ? Math.round(((double) slaBreachCount / totalTransitions * 100.0) * 10.0) / 10.0
                : 0.0;
    }

    public BatchStatus getStage() {
        return stage;
    }

    public void setStage(BatchStatus stage) {
        this.stage = stage;
    }

    public long getTotalTransitions() {
        return totalTransitions;
    }

    public void setTotalTransitions(long totalTransitions) {
        this.totalTransitions = totalTransitions;
    }

    public double getAverageDurationHours() {
        return averageDurationHours;
    }

    public void setAverageDurationHours(double averageDurationHours) {
        this.averageDurationHours = averageDurationHours;
    }

    public double getMaxDurationHours() {
        return maxDurationHours;
    }

    public void setMaxDurationHours(double maxDurationHours) {
        this.maxDurationHours = maxDurationHours;
    }

    public long getSlaBreachCount() {
        return slaBreachCount;
    }

    public void setSlaBreachCount(long slaBreachCount) {
        this.slaBreachCount = slaBreachCount;
    }

    public double getSlaBreachPercentage() {
        return slaBreachPercentage;
    }

    public void setSlaBreachPercentage(double slaBreachPercentage) {
        this.slaBreachPercentage = slaBreachPercentage;
    }
}
