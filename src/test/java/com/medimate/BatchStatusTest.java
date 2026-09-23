package com.medimate;

import com.medimate.model.BatchStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BatchStatusTest {

    @Test
    @DisplayName("Should allow valid sequential pharmaceutical supply chain transitions")
    void testValidTransitions() {
        assertTrue(BatchStatus.MANUFACTURED.canTransitionTo(BatchStatus.QUALITY_TESTED));
        assertTrue(BatchStatus.QUALITY_TESTED.canTransitionTo(BatchStatus.CENTRAL_DEPOT));
        assertTrue(BatchStatus.CENTRAL_DEPOT.canTransitionTo(BatchStatus.IN_TRANSIT));
        assertTrue(BatchStatus.IN_TRANSIT.canTransitionTo(BatchStatus.DELIVERED_TO_PHARMACY));
        assertTrue(BatchStatus.DELIVERED_TO_PHARMACY.canTransitionTo(BatchStatus.DISPENSED));
    }

    @Test
    @DisplayName("Should block illegal skips across the pharmaceutical custody chain")
    void testIllegalStateSkips() {
        // Cannot jump directly from MANUFACTURED to DELIVERED_TO_PHARMACY without testing & transit
        assertFalse(BatchStatus.MANUFACTURED.canTransitionTo(BatchStatus.DELIVERED_TO_PHARMACY));
        // Cannot bypass quality testing
        assertFalse(BatchStatus.MANUFACTURED.canTransitionTo(BatchStatus.CENTRAL_DEPOT));
        // Once dispensed to patients, batch cannot transition back into circulation
        assertFalse(BatchStatus.DISPENSED.canTransitionTo(BatchStatus.CENTRAL_DEPOT));
    }

    @Test
    @DisplayName("Should allow regulatory emergency recall from any pre-dispensed stage")
    void testRecallRules() {
        assertTrue(BatchStatus.MANUFACTURED.canTransitionTo(BatchStatus.RECALLED));
        assertTrue(BatchStatus.CENTRAL_DEPOT.canTransitionTo(BatchStatus.RECALLED));
        assertTrue(BatchStatus.IN_TRANSIT.canTransitionTo(BatchStatus.RECALLED));
        assertTrue(BatchStatus.DELIVERED_TO_PHARMACY.canTransitionTo(BatchStatus.RECALLED));
        assertFalse(BatchStatus.DISPENSED.canTransitionTo(BatchStatus.RECALLED));
    }

    @Test
    @DisplayName("Should allow quarantine upon suspected temperature breach or inspection")
    void testQuarantineRules() {
        assertTrue(BatchStatus.IN_TRANSIT.canTransitionTo(BatchStatus.QUARANTINED));
        assertTrue(BatchStatus.CENTRAL_DEPOT.canTransitionTo(BatchStatus.QUARANTINED));
        assertTrue(BatchStatus.QUARANTINED.canTransitionTo(BatchStatus.CENTRAL_DEPOT));
        assertTrue(BatchStatus.QUARANTINED.canTransitionTo(BatchStatus.RECALLED));
    }
}
