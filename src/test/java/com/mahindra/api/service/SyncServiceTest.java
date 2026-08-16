package com.mahindra.api.service;

import com.mahindra.api.client.CountryStateCityClient;
import com.mahindra.api.client.dto.CscCityResponse;
import com.mahindra.api.client.dto.CscCountryResponse;
import com.mahindra.api.exception.ExternalApiException;
import com.mahindra.api.model.City;
import com.mahindra.api.model.Country;
import com.mahindra.api.model.SyncResult;
import com.mahindra.api.repository.CityRepository;
import com.mahindra.api.repository.CountryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncServiceTest {

    @Mock
    private CountryStateCityClient cscClient;

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private CityRepository cityRepository;

    private SyncService syncService;

    @BeforeEach
    void setUp() {
        syncService = new SyncService(cscClient, countryRepository, cityRepository);
    }

    private static CscCountryResponse countryDto(String name, String iso2) {
        return new CscCountryResponse(name, iso2, "Capital", "Europe", "Western Europe",
                "+33", "🇫🇷", "48.0", "2.0");
    }

    private static CscCityResponse cityDto(String name, String stateCode) {
        return new CscCityResponse(name, "48.85", "2.35", stateCode, "Île-de-France");
    }

    @Test
    void syncFromExternalApi_insertsNewCountriesAndCities() {
        when(cscClient.fetchAllCountries()).thenReturn(List.of(countryDto("France", "FR")));
        when(countryRepository.findByCca2("FR")).thenReturn(Optional.empty());

        Country savedCountry = new Country("France", "Capital", null, "Europe", null, null, "FR");
        savedCountry.setId(1L);
        when(countryRepository.save(any())).thenReturn(savedCountry);

        when(cscClient.fetchCitiesByCountry("FR")).thenReturn(List.of(cityDto("Paris", "IDF")));
        when(cityRepository.findByNameAndCountryIdAndStateCode("Paris", 1L, "IDF"))
                .thenReturn(Optional.empty());
        when(cityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SyncResult result = syncService.syncFromExternalApi();

        assertThat(result.countriesInserted()).isEqualTo(1);
        assertThat(result.countriesSkipped()).isEqualTo(0);
        assertThat(result.citiesInserted()).isEqualTo(1);
        assertThat(result.citiesSkipped()).isEqualTo(0);
        assertThat(result.errors()).isEmpty();
        assertThat(result.timestamp()).isNotBlank();
    }

    @Test
    void syncFromExternalApi_skipsExistingCountryAndCity() {
        when(cscClient.fetchAllCountries()).thenReturn(List.of(countryDto("France", "FR")));

        Country existing = new Country("France", "Paris", null, "Europe", null, null, "FR");
        existing.setId(42L);
        when(countryRepository.findByCca2("FR")).thenReturn(Optional.of(existing));

        when(cscClient.fetchCitiesByCountry("FR")).thenReturn(List.of(cityDto("Paris", "IDF")));

        City existingCity = new City();
        existingCity.setId(99L);
        existingCity.setName("Paris");
        when(cityRepository.findByNameAndCountryIdAndStateCode("Paris", 42L, "IDF"))
                .thenReturn(Optional.of(existingCity));

        SyncResult result = syncService.syncFromExternalApi();

        assertThat(result.countriesInserted()).isEqualTo(0);
        assertThat(result.countriesSkipped()).isEqualTo(1);
        assertThat(result.citiesInserted()).isEqualTo(0);
        assertThat(result.citiesSkipped()).isEqualTo(1);

        // Existing entities must never be modified or saved
        verify(countryRepository, never()).save(any());
        verify(cityRepository, never()).save(any());
        verify(countryRepository, never()).deleteAll();
        verify(cityRepository, never()).deleteAll();
    }

    @Test
    void syncFromExternalApi_skipsCountriesWithNoIso2() {
        CscCountryResponse noIso = new CscCountryResponse("Unknown", null, null, null, null, null, null, null, null);
        when(cscClient.fetchAllCountries()).thenReturn(List.of(noIso));

        SyncResult result = syncService.syncFromExternalApi();

        assertThat(result.countriesSynced()).isEqualTo(0);
        verify(countryRepository, never()).save(any());
    }

    @Test
    void syncFromExternalApi_recordsErrorWhenCityFetchFails_butContinues() {
        when(cscClient.fetchAllCountries()).thenReturn(List.of(
                countryDto("France", "FR"),
                countryDto("Germany", "DE")
        ));
        when(countryRepository.findByCca2(anyString())).thenReturn(Optional.empty());

        Country savedFr = new Country("France", "Paris", null, "Europe", null, null, "FR");
        savedFr.setId(1L);
        Country savedDe = new Country("Germany", "Berlin", null, "Europe", null, null, "DE");
        savedDe.setId(2L);
        when(countryRepository.save(any()))
                .thenReturn(savedFr)
                .thenReturn(savedDe);

        when(cscClient.fetchCitiesByCountry("FR"))
                .thenThrow(new ExternalApiException(503, "timeout"));
        when(cscClient.fetchCitiesByCountry("DE"))
                .thenReturn(List.of(cityDto("Berlin", "BE")));
        when(cityRepository.findByNameAndCountryIdAndStateCode(anyString(), anyLong(), anyString()))
                .thenReturn(Optional.empty());
        when(cityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SyncResult result = syncService.syncFromExternalApi();

        assertThat(result.countriesSynced()).isEqualTo(2);
        assertThat(result.errors()).hasSize(1);
        assertThat(result.errors().get(0)).contains("FR");
        assertThat(result.citiesInserted()).isEqualTo(1); // Berlin still synced
    }

    @Test
    void syncFromExternalApi_propagatesException_whenAllCountriesFetchFails() {
        when(cscClient.fetchAllCountries())
                .thenThrow(new ExternalApiException(503, "Service unavailable"));

        assertThatThrownBy(() -> syncService.syncFromExternalApi())
                .isInstanceOf(ExternalApiException.class)
                .hasMessageContaining("Service unavailable");

        verify(countryRepository, never()).save(any());
    }
}
