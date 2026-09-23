package com.medimate.model;

/**
 * State machine defining the end-to-end lifecycle of a pharmaceutical medicine batch.
 */
public enum BatchStatus {
    MANUFACTURED,
    QUALITY_TESTED,
    CENTRAL_DEPOT,
    IN_TRANSIT,
    DELIVERED_TO_PHARMACY,
    DISPENSED,
    RECALLED,
    QUARANTINED;

    /**
     * Enforces legal supply chain transitions and prevents skipping mandatory custody checkpoints.
     */
    public boolean canTransitionTo(BatchStatus targetStatus) {
        if (this == targetStatus) {
            return false;
        }

        // Terminal or recalled status
        if (this == DISPENSED) {
            return false; // Once dispensed to patients, batch items cannot be reversed
        }

        // A batch can be recalled from any active stage if contaminated or regulatory notice issued
        if (targetStatus == RECALLED) {
            return this != DISPENSED;
        }

        // Quarantine can happen if temperature alert or missing chain-of-custody paperwork
        if (targetStatus == QUARANTINED) {
            return this == IN_TRANSIT || this == CENTRAL_DEPOT || this == DELIVERED_TO_PHARMACY;
        }

        // Release from quarantine back to previous legitimate stages
        if (this == QUARANTINED) {
            return targetStatus == CENTRAL_DEPOT || targetStatus == DELIVERED_TO_PHARMACY || targetStatus == RECALLED;
        }

        // Standard pharmaceutical supply chain happy path
        switch (this) {
            case MANUFACTURED:
                return targetStatus == QUALITY_TESTED;
            case QUALITY_TESTED:
                return targetStatus == CENTRAL_DEPOT;
            case CENTRAL_DEPOT:
                return targetStatus == IN_TRANSIT;
            case IN_TRANSIT:
                return targetStatus == DELIVERED_TO_PHARMACY;
            case DELIVERED_TO_PHARMACY:
                return targetStatus == DISPENSED;
            default:
                return false;
        }
    }
}
