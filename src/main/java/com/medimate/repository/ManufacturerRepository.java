package com.medimate.repository;

import com.medimate.model.Manufacturer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ManufacturerRepository extends JpaRepository<Manufacturer, Long> {
    Optional<Manufacturer> findByLicenseNumber(String licenseNumber);
    Optional<Manufacturer> findByNameIgnoreCase(String name);
}
