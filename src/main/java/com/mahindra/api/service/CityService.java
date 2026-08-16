package com.mahindra.api.service;

import com.mahindra.api.exception.ResourceNotFoundException;
import com.mahindra.api.model.City;
import com.mahindra.api.model.PaginatedResponse;
import com.mahindra.api.repository.CityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CityService {

    private final CityRepository cityRepository;
    private final CountryService countryService;

    public CityService(CityRepository cityRepository, CountryService countryService) {
        this.cityRepository = cityRepository;
        this.countryService = countryService;
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<City> getCitiesByCountry(Long countryId, int page, int size) {
        if (page < 0) throw new IllegalArgumentException("Page index must not be less than zero");
        if (size < 1) throw new IllegalArgumentException("Page size must not be less than one");

        // Validates the country exists
        countryService.getCountryById(countryId);

        List<City> allCities = cityRepository.findByCountryId(countryId);
        long totalItems = allCities.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);

        int fromIndex = page * size;
        if (fromIndex >= totalItems && totalItems > 0) {
            return new PaginatedResponse<>(List.of(), page, size, totalItems, totalPages);
        }

        int toIndex = (int) Math.min((long) fromIndex + size, totalItems);
        return new PaginatedResponse<>(allCities.subList(fromIndex, toIndex), page, size, totalItems, totalPages);
    }

    @Transactional(readOnly = true)
    public City getCityById(Long cityId) {
        return cityRepository.findById(cityId)
                .orElseThrow(() -> new ResourceNotFoundException("City not found with id " + cityId));
    }
}
