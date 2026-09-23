package com.medimate.repository;

import com.medimate.model.BatchStatus;
import com.medimate.model.MedicineBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineBatchRepository extends JpaRepository<MedicineBatch, Long> {

    Optional<MedicineBatch> findByBatchNumber(String batchNumber);

    List<MedicineBatch> findByStatus(BatchStatus status);

    List<MedicineBatch> findByMedicineId(Long medicineId);

    List<MedicineBatch> findByIsRecalled(boolean isRecalled);

    @Query("SELECT b FROM MedicineBatch b WHERE b.status NOT IN (com.medimate.model.BatchStatus.DISPENSED, com.medimate.model.BatchStatus.RECALLED)")
    List<MedicineBatch> findActiveBatches();

    @Query("SELECT b FROM MedicineBatch b WHERE b.expiryDate <= :cutoffDate AND b.status NOT IN (com.medimate.model.BatchStatus.DISPENSED, com.medimate.model.BatchStatus.RECALLED)")
    List<MedicineBatch> findExpiringBatches(LocalDate cutoffDate);

    @Query("SELECT COUNT(b) FROM MedicineBatch b WHERE b.status = :status")
    long countByStatus(BatchStatus status);
}
