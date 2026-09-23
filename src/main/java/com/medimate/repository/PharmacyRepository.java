package com.medimate.repository;

import com.medimate.model.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PharmacyRepository extends JpaRepository<Pharmacy, Long> {
    Optional<Pharmacy> findByLicenseNumber(String licenseNumber);
    List<Pharmacy> findByPharmacyType(String pharmacyType);
}
