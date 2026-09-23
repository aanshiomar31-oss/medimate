package com.medimate.repository;

import com.medimate.model.BatchCustodyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchCustodyLogRepository extends JpaRepository<BatchCustodyLog, Long> {

    List<BatchCustodyLog> findByBatchIdOrderByTimestampAsc(Long batchId);

    List<BatchCustodyLog> findByBatchBatchNumberOrderByTimestampAsc(String batchNumber);

    List<BatchCustodyLog> findByTemperatureCompliant(boolean temperatureCompliant);

    List<BatchCustodyLog> findBySlaBreached(boolean slaBreached);

    @Query("SELECT l.toStatus, COUNT(l), AVG(l.timeSpentHours), MAX(l.timeSpentHours), SUM(CASE WHEN l.slaBreached = true THEN 1 ELSE 0 END) " +
           "FROM BatchCustodyLog l " +
           "WHERE l.timeSpentHours IS NOT NULL " +
           "GROUP BY l.toStatus")
    List<Object[]> findStageLatencyMetrics();
}
