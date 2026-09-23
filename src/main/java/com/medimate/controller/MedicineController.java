package com.medimate.controller;

import com.medimate.dto.ApiResponse;
import com.medimate.model.Manufacturer;
import com.medimate.model.Medicine;
import com.medimate.model.Pharmacy;
import com.medimate.service.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Medicine & Partner Registry", description = "Endpoints for pharmaceutical catalog, FDA manufacturers, and licensed pharmacies")
public class MedicineController {

    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @GetMapping("/medicines")
    @Operation(summary = "List medicines with optional category or cold-chain filters")
    public ResponseEntity<ApiResponse<List<Medicine>>> getMedicines(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean coldChainOnly) {

        List<Medicine> medicines = medicineService.getAllMedicines(category, coldChainOnly);
        return ResponseEntity.ok(ApiResponse.ok("Retrieved " + medicines.size() + " medicines", medicines));
    }

    @GetMapping("/medicines/{id}")
    @Operation(summary = "Get medicine details by ID")
    public ResponseEntity<ApiResponse<Medicine>> getMedicineById(@PathVariable Long id) {
        return medicineService.getMedicineById(id)
                .map(m -> ResponseEntity.ok(ApiResponse.ok("Medicine retrieved", m)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Medicine not found with ID: " + id)));
    }

    @PostMapping("/medicines")
    @Operation(summary = "Register a new pharmaceutical drug")
    public ResponseEntity<ApiResponse<Medicine>> createMedicine(@Valid @RequestBody Medicine medicine) {
        Medicine saved = medicineService.createMedicine(medicine);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Medicine registered successfully", saved));
    }

    @GetMapping("/manufacturers")
    @Operation(summary = "List licensed pharmaceutical manufacturers")
    public ResponseEntity<ApiResponse<List<Manufacturer>>> getManufacturers() {
        List<Manufacturer> manufacturers = medicineService.getAllManufacturers();
        return ResponseEntity.ok(ApiResponse.ok("Retrieved " + manufacturers.size() + " manufacturers", manufacturers));
    }

    @PostMapping("/manufacturers")
    @Operation(summary = "Register a new pharmaceutical manufacturer")
    public ResponseEntity<ApiResponse<Manufacturer>> createManufacturer(@Valid @RequestBody Manufacturer manufacturer) {
        Manufacturer saved = medicineService.createManufacturer(manufacturer);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Manufacturer registered successfully", saved));
    }

    @GetMapping("/pharmacies")
    @Operation(summary = "List licensed hospital and retail pharmacies")
    public ResponseEntity<ApiResponse<List<Pharmacy>>> getPharmacies(@RequestParam(required = false) String type) {
        List<Pharmacy> pharmacies = medicineService.getAllPharmacies(type);
        return ResponseEntity.ok(ApiResponse.ok("Retrieved " + pharmacies.size() + " pharmacies", pharmacies));
    }

    @PostMapping("/pharmacies")
    @Operation(summary = "Register a new receiving pharmacy")
    public ResponseEntity<ApiResponse<Pharmacy>> createPharmacy(@Valid @RequestBody Pharmacy pharmacy) {
        Pharmacy saved = medicineService.createPharmacy(pharmacy);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Pharmacy registered successfully", saved));
    }
}
