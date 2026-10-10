package com.sdem.citizen.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "citizens")
public class Citizen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String postcode;

    /** Serial number of the household smart meter. */
    @Column(nullable = false, unique = true)
    private String meterId;

    /** Id of the supplier in the Electricity Provider service. */
    @Column(nullable = false)
    private Long providerId;

    @OneToMany(mappedBy = "citizen", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("readingAt ASC")
    private List<MeterReading> readings = new ArrayList<>();

    public Citizen() {
    }

    public Citizen(String fullName, String email, String postcode, String meterId, Long providerId) {
        this.fullName = fullName;
        this.email = email;
        this.postcode = postcode;
        this.meterId = meterId;
        this.providerId = providerId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPostcode() { return postcode; }
    public void setPostcode(String postcode) { this.postcode = postcode; }

    public String getMeterId() { return meterId; }
    public void setMeterId(String meterId) { this.meterId = meterId; }

    public Long getProviderId() { return providerId; }
    public void setProviderId(Long providerId) { this.providerId = providerId; }

    public List<MeterReading> getReadings() { return readings; }

    public void addReading(MeterReading reading) {
        reading.setCitizen(this);
        readings.add(reading);
    }
}
