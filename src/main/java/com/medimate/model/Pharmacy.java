package com.medimate.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "pharmacies")
public class Pharmacy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Pharmacy name is required")
    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "pharmacy_type", length = 50)
    private String pharmacyType; // e.g. "HOSPITAL_PHARMACY", "REGIONAL_DISPENSARY", "COMMUNITY_RETAIL"

    @NotBlank(message = "Pharmacy license number is required")
    @Column(name = "license_number", nullable = false, unique = true, length = 60)
    private String licenseNumber;

    @Column(length = 60)
    private String city;

    @Column(length = 150)
    private String address;

    @Column(name = "contact_person", length = 80)
    private String contactPerson;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Pharmacy() {
    }

    public Pharmacy(String name, String pharmacyType, String licenseNumber, String city, String address, String contactPerson, String contactEmail) {
        this.name = name;
        this.pharmacyType = pharmacyType;
        this.licenseNumber = licenseNumber;
        this.city = city;
        this.address = address;
        this.contactPerson = contactPerson;
        this.contactEmail = contactEmail;
        this.createdAt = LocalDateTime.now();
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

    public String getPharmacyType() {
        return pharmacyType;
    }

    public void setPharmacyType(String pharmacyType) {
        this.pharmacyType = pharmacyType;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
