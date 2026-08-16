package com.mahindra.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cities",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_city_name_country_state",
                columnNames = {"name", "country_id", "state_code"}))
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "country_id")
    private Long countryId;

    private Long population;

    @Column(name = "zip_code")
    private String zipCode;

    private String description;
    private Double latitude;
    private Double longitude;

    @Column(name = "state_code")
    private String stateCode;

    @Column(name = "state_name")
    private String stateName;

    /** Required by JPA. */
    public City() {}

    /** Backward-compatible constructor matching the original record shape. */
    public City(Long id, String name, Long countryId, Long population,
                String zipCode, String description) {
        this.id = id;
        this.name = name;
        this.countryId = countryId;
        this.population = population;
        this.zipCode = zipCode;
        this.description = description;
    }

    public Long getId()         { return id; }
    public void setId(Long id)  { this.id = id; }

    public String getName()             { return name; }
    public void setName(String name)    { this.name = name; }

    public Long getCountryId()                  { return countryId; }
    public void setCountryId(Long countryId)    { this.countryId = countryId; }

    public Long getPopulation()                 { return population; }
    public void setPopulation(Long population)  { this.population = population; }

    public String getZipCode()              { return zipCode; }
    public void setZipCode(String zipCode)  { this.zipCode = zipCode; }

    public String getDescription()                  { return description; }
    public void setDescription(String description)  { this.description = description; }

    public Double getLatitude()                 { return latitude; }
    public void setLatitude(Double latitude)    { this.latitude = latitude; }

    public Double getLongitude()                    { return longitude; }
    public void setLongitude(Double longitude)      { this.longitude = longitude; }

    public String getStateCode()                { return stateCode; }
    public void setStateCode(String stateCode)  { this.stateCode = stateCode; }

    public String getStateName()                { return stateName; }
    public void setStateName(String stateName)  { this.stateName = stateName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof City other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
