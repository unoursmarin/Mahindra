package com.mahindra.api.service;

import com.mahindra.api.exception.ResourceNotFoundException;
import com.mahindra.api.model.City;
import com.mahindra.api.model.Country;
import com.mahindra.api.model.PaginatedResponse;
import com.mahindra.api.repository.CityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CityServiceTest {

    @Mock
    private CityRepository cityRepository;

    @Mock
    private CountryService countryService;

    @InjectMocks
    private CityService cityService;

    private final City mumbai    = new City(1L, "Mumbai",    1L, 20_000_000L, "400001", "Financial capital");
    private final City delhi     = new City(2L, "Delhi",     1L, 32_000_000L, "110001", "Capital city");
    private final City bangalore = new City(3L, "Bangalore", 1L, 13_000_000L, "560001", "IT hub");
    private final City chennai   = new City(4L, "Chennai",   1L,  7_000_000L, "600001", "South gateway");
    private final City hyderabad = new City(5L, "Hyderabad", 1L, 10_000_000L, "500001", "City of Pearls");

    private final List<City> indiaCities = List.of(mumbai, delhi, bangalore, chennai, hyderabad);

    @BeforeEach
    void setUp() {
        lenient().when(countryService.getCountryById(1L)).thenReturn(new Country(1L, "India"));
        lenient().when(cityRepository.findByCountryId(1L)).thenReturn(indiaCities);
    }

    @Test
    void getCitiesByCountry_returnsFirstPage() {
        PaginatedResponse<City> result = cityService.getCitiesByCountry(1L, 0, 3);

        assertThat(result.content()).hasSize(3).containsExactly(mumbai, delhi, bangalore);
        assertThat(result.page()).isEqualTo(0);
        assertThat(result.size()).isEqualTo(3);
        assertThat(result.totalItems()).isEqualTo(5L);
        assertThat(result.totalPages()).isEqualTo(2);
    }

    @Test
    void getCitiesByCountry_returnsSecondPage() {
        PaginatedResponse<City> result = cityService.getCitiesByCountry(1L, 1, 3);

        assertThat(result.content()).hasSize(2).containsExactly(chennai, hyderabad);
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(2);
    }

    @Test
    void getCitiesByCountry_returnsEmptyContent_whenPageBeyondRange() {
        PaginatedResponse<City> result = cityService.getCitiesByCountry(1L, 10, 3);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalItems()).isEqualTo(5L);
        assertThat(result.totalPages()).isEqualTo(2);
    }

    @Test
    void getCitiesByCountry_returnsFullPage_whenSizeExceedsTotal() {
        PaginatedResponse<City> result = cityService.getCitiesByCountry(1L, 0, 100);

        assertThat(result.content()).hasSize(5);
        assertThat(result.totalPages()).isEqualTo(1);
    }

    @Test
    void getCitiesByCountry_neverCallsExternalApi() {
        cityService.getCitiesByCountry(1L, 0, 10);

        // Verify only DB calls were made — no external API interactions
        verify(cityRepository).findByCountryId(1L);
        verifyNoMoreInteractions(cityRepository);
    }

    @Test
    void getCitiesByCountry_throwsNotFoundException_whenCountryNotFound() {
        when(countryService.getCountryById(999L))
                .thenThrow(new ResourceNotFoundException("Country not found with id 999"));

        assertThatThrownBy(() -> cityService.getCitiesByCountry(999L, 0, 10))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void getCitiesByCountry_throwsIllegalArgument_whenPageNegative() {
        assertThatThrownBy(() -> cityService.getCitiesByCountry(1L, -1, 10))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getCitiesByCountry_throwsIllegalArgument_whenSizeZero() {
        assertThatThrownBy(() -> cityService.getCitiesByCountry(1L, 0, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getCityById_returnsCity_whenExists() {
        when(cityRepository.findById(1L)).thenReturn(Optional.of(mumbai));

        City result = cityService.getCityById(1L);

        assertThat(result).isEqualTo(mumbai);
    }

    @Test
    void getCityById_throwsNotFoundException_whenNotFound() {
        when(cityRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cityService.getCityById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }
}
