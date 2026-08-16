package com.mahindra.api.service;

import com.mahindra.api.dto.geojson.GeoJsonFeature;
import com.mahindra.api.dto.geojson.GeoJsonFeatureCollection;
import com.mahindra.api.dto.geojson.GeoJsonGeometry;
import com.mahindra.api.model.City;
import com.mahindra.api.model.Country;
import com.mahindra.api.repository.CountryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeoJsonService {

    private final CountryRepository countryRepository;
    private final CityService cityService;
    private final CountryService countryService;

    public GeoJsonService(CountryRepository countryRepository,
                          CityService cityService,
                          CountryService countryService) {
        this.countryRepository = countryRepository;
        this.cityService = cityService;
        this.countryService = countryService;
    }

    @Transactional(readOnly = true)
    public GeoJsonFeatureCollection allCountriesAsGeoJson() {
        List<GeoJsonFeature> features = countryRepository.findAll().stream()
                .filter(c -> c.getCapitalLat() != null && c.getCapitalLng() != null)
                .map(this::countryToFeature)
                .toList();
        return GeoJsonFeatureCollection.of(features);
    }

    @Transactional
    public GeoJsonFeature countryAsGeoJson(Long countryId) {
        Country country = countryService.getCountryById(countryId);
        return countryToFeature(country);
    }

    @Transactional
    public GeoJsonFeatureCollection citiesAsGeoJson(Long countryId) {
        // Validate country exists (throws 404 if not)
        countryService.getCountryById(countryId);

        // getCitiesByCountry triggers lazy enrichment; we need page 0 large enough
        var paginated = cityService.getCitiesByCountry(countryId, 0, Integer.MAX_VALUE);

        List<GeoJsonFeature> features = paginated.content().stream()
                .filter(c -> c.getLatitude() != null && c.getLongitude() != null)
                .map(this::cityToFeature)
                .toList();

        return GeoJsonFeatureCollection.of(features);
    }

    private GeoJsonFeature countryToFeature(Country c) {
        GeoJsonGeometry geometry = (c.getCapitalLat() != null && c.getCapitalLng() != null)
                ? GeoJsonGeometry.point(c.getCapitalLng(), c.getCapitalLat()) // [lng, lat]
                : null;

        Map<String, Object> props = new HashMap<>();
        props.put("id", c.getId());
        props.put("name", c.getName());
        props.put("capital", c.getCapital());
        props.put("population", c.getPopulation());
        props.put("region", c.getRegion());
        props.put("area", c.getArea());
        props.put("flagUrl", c.getFlagUrl());
        props.put("cca2", c.getCca2());
        props.put("emoji", c.getEmoji());
        props.put("phoneCode", c.getPhoneCode());

        return GeoJsonFeature.of(geometry, props);
    }

    private GeoJsonFeature cityToFeature(City c) {
        // coordinates: [longitude, latitude] per GeoJSON spec
        GeoJsonGeometry geometry = GeoJsonGeometry.point(c.getLongitude(), c.getLatitude());

        Map<String, Object> props = new HashMap<>();
        props.put("id", c.getId());
        props.put("name", c.getName());
        props.put("countryId", c.getCountryId());
        props.put("population", c.getPopulation());
        props.put("stateCode", c.getStateCode());
        props.put("stateName", c.getStateName());

        return GeoJsonFeature.of(geometry, props);
    }
}
