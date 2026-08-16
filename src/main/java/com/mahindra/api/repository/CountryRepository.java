package com.mahindra.api.repository;

import com.mahindra.api.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, Long> {

    Optional<Country> findByCca2(String cca2);
}
