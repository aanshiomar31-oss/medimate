package com.medimate.service;

import com.medimate.model.Manufacturer;
import com.medimate.model.Medicine;
import com.medimate.model.Pharmacy;
import com.medimate.repository.ManufacturerRepository;
import com.medimate.repository.MedicineRepository;
import com.medimate.repository.PharmacyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final PharmacyRepository pharmacyRepository;

    public MedicineService(MedicineRepository medicineRepository,
                           ManufacturerRepository manufacturerRepository,
                           PharmacyRepository pharmacyRepository) {
        this.medicineRepository = medicineRepository;
        this.manufacturerRepository = manufacturerRepository;
        this.pharmacyRepository = pharmacyRepository;
    }

    @Transactional(readOnly = true)
    public List<Medicine> getAllMedicines(String category, Boolean requiresColdChain) {
        if (category != null && !category.isBlank()) {
            return medicineRepository.findByCategoryIgnoreCase(category);
        }
        if (requiresColdChain != null) {
            return medicineRepository.findByRequiresColdChain(requiresColdChain);
        }
        return medicineRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Medicine> getMedicineById(Long id) {
        return medicineRepository.findById(id);
    }

    public Medicine createMedicine(Medicine medicine) {
        return medicineRepository.save(medicine);
    }

    @Transactional(readOnly = true)
    public List<Manufacturer> getAllManufacturers() {
        return manufacturerRepository.findAll();
    }

    public Manufacturer createManufacturer(Manufacturer manufacturer) {
        return manufacturerRepository.save(manufacturer);
    }

    @Transactional(readOnly = true)
    public List<Pharmacy> getAllPharmacies(String pharmacyType) {
        if (pharmacyType != null && !pharmacyType.isBlank()) {
            return pharmacyRepository.findByPharmacyType(pharmacyType);
        }
        return pharmacyRepository.findAll();
    }

    public Pharmacy createPharmacy(Pharmacy pharmacy) {
        return pharmacyRepository.save(pharmacy);
    }
}
