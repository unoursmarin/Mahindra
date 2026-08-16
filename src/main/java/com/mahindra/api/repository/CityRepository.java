package com.mahindra.api.repository;

import com.mahindra.api.model.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {

    List<City> findByCountryId(Long countryId);

    long countByCountryId(Long countryId);

    Optional<City> findByNameAndCountryIdAndStateCode(String name, Long countryId, String stateCode);
}
