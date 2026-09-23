package com.medimate.repository;

import com.medimate.model.SupplyShipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplyShipmentRepository extends JpaRepository<SupplyShipment, Long> {

    Optional<SupplyShipment> findByShipmentNumber(String shipmentNumber);

    List<SupplyShipment> findByStatus(String status);

    List<SupplyShipment> findByTemperatureExcursion(boolean temperatureExcursion);

    List<SupplyShipment> findBySlaBreached(boolean slaBreached);

    @Query("SELECT s FROM SupplyShipment s WHERE s.status IN ('DISPATCHED', 'IN_TRANSIT', 'TEMPERATURE_BREACH_ALERT')")
    List<SupplyShipment> findActiveShipments();
}
