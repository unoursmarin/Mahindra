package com.mahindra.api.service;

import com.mahindra.api.dto.geojson.GeoJsonFeature;
import com.mahindra.api.dto.geojson.GeoJsonFeatureCollection;
import com.mahindra.api.model.Country;
import com.mahindra.api.repository.CountryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeoJsonServiceTest {

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private CityService cityService;

    @Mock
    private CountryService countryService;

    @InjectMocks
    private GeoJsonService geoJsonService;

    @Test
    void allCountriesAsGeoJson_returnsOnlyCountriesWithCoordinates() {
        Country france = new Country("France", "Paris", 67413000L, "Europe", 551695.0, "flag.png", "FR");
        france.setId(1L);
        france.setCapitalLat(48.8566);
        france.setCapitalLng(2.3522);

        Country noGeo = new Country("Unknown Land", null, null, null, null, null, "XX");
        noGeo.setId(2L);
        // capitalLat/Lng remain null

        when(countryRepository.findAll()).thenReturn(List.of(france, noGeo));

        GeoJsonFeatureCollection result = geoJsonService.allCountriesAsGeoJson();

        assertThat(result.type()).isEqualTo("FeatureCollection");
        assertThat(result.features()).hasSize(1);

        GeoJsonFeature feature = result.features().get(0);
        assertThat(feature.type()).isEqualTo("Feature");
        assertThat(feature.geometry().type()).isEqualTo("Point");

        // CRITICAL: GeoJSON coordinate order is [longitude, latitude]
        List<Double> coords = feature.geometry().coordinates();
        assertThat(coords.get(0)).isEqualTo(2.3522);  // longitude first
        assertThat(coords.get(1)).isEqualTo(48.8566); // latitude second
    }

    @Test
    void allCountriesAsGeoJson_includesCountryProperties() {
        Country france = new Country("France", "Paris", 67413000L, "Europe", 551695.0, "flag.png", "FR");
        france.setId(1L);
        france.setCapitalLat(48.8566);
        france.setCapitalLng(2.3522);
        when(countryRepository.findAll()).thenReturn(List.of(france));

        GeoJsonFeatureCollection result = geoJsonService.allCountriesAsGeoJson();

        var props = result.features().get(0).properties();
        assertThat(props.get("name")).isEqualTo("France");
        assertThat(props.get("capital")).isEqualTo("Paris");
        assertThat(props.get("cca2")).isEqualTo("FR");
        assertThat(props.get("id")).isEqualTo(1L);
    }

    @Test
    void allCountriesAsGeoJson_returnsEmptyCollection_whenNoCountriesHaveGeo() {
        Country noGeo = new Country(1L, "NoGeoland");
        when(countryRepository.findAll()).thenReturn(List.of(noGeo));

        GeoJsonFeatureCollection result = geoJsonService.allCountriesAsGeoJson();

        assertThat(result.features()).isEmpty();
    }
}
