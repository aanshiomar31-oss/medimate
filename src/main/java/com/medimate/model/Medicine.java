package com.medimate.model;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "medicines")
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Brand name is required")
    @Column(nullable = false, length = 120)
    private String name;

    @NotBlank(message = "Generic chemical name is required")
    @Column(name = "generic_name", nullable = false, length = 120)
    private String genericName;

    @Column(name = "dosage_form", length = 60)
    private String dosageForm; // e.g. "Vial (10ml)", "Tablet (500mg)", "Prefilled Syringe"

    @Column(length = 60)
    private String category; // e.g. "Vaccines", "Cardiovascular", "Antibiotics", "Endocrinology"

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "requires_cold_chain", nullable = false)
    private boolean requiresColdChain;

    @Column(name = "min_temp_celsius")
    private Double minTemperatureCelsius; // e.g. 2.0°C

    @Column(name = "max_temp_celsius")
    private Double maxTemperatureCelsius; // e.g. 8.0°C

    @Column(name = "shelf_life_months")
    private Integer standardShelfLifeMonths;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Medicine() {
    }

    public Medicine(String name, String genericName, String dosageForm, String category, BigDecimal unitPrice,
                    boolean requiresColdChain, Double minTemperatureCelsius, Double maxTemperatureCelsius, Integer standardShelfLifeMonths) {
        this.name = name;
        this.genericName = genericName;
        this.dosageForm = dosageForm;
        this.category = category;
        this.unitPrice = unitPrice;
        this.requiresColdChain = requiresColdChain;
        this.minTemperatureCelsius = minTemperatureCelsius;
        this.maxTemperatureCelsius = maxTemperatureCelsius;
        this.standardShelfLifeMonths = standardShelfLifeMonths;
        this.createdAt = LocalDateTime.now();
    }

    public boolean isTemperatureCompliant(Double temperature) {
        if (!requiresColdChain || temperature == null) {
            return true;
        }
        if (minTemperatureCelsius != null && temperature < minTemperatureCelsius) {
            return false;
        }
        if (maxTemperatureCelsius != null && temperature > maxTemperatureCelsius) {
            return false;
        }
        return true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public String getDosageForm() {
        return dosageForm;
    }

    public void setDosageForm(String dosageForm) {
        this.dosageForm = dosageForm;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public boolean isRequiresColdChain() {
        return requiresColdChain;
    }

    public void setRequiresColdChain(boolean requiresColdChain) {
        this.requiresColdChain = requiresColdChain;
    }

    public Double getMinTemperatureCelsius() {
        return minTemperatureCelsius;
    }

    public void setMinTemperatureCelsius(Double minTemperatureCelsius) {
        this.minTemperatureCelsius = minTemperatureCelsius;
    }

    public Double getMaxTemperatureCelsius() {
        return maxTemperatureCelsius;
    }

    public void setMaxTemperatureCelsius(Double maxTemperatureCelsius) {
        this.maxTemperatureCelsius = maxTemperatureCelsius;
    }

    public Integer getStandardShelfLifeMonths() {
        return standardShelfLifeMonths;
    }

    public void setStandardShelfLifeMonths(Integer standardShelfLifeMonths) {
        this.standardShelfLifeMonths = standardShelfLifeMonths;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
