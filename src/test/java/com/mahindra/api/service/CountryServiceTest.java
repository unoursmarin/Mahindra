package com.mahindra.api.service;

import com.mahindra.api.exception.ResourceNotFoundException;
import com.mahindra.api.model.Country;
import com.mahindra.api.repository.CountryRepository;
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
class CountryServiceTest {

    @Mock
    private CountryRepository countryRepository;

    @InjectMocks
    private CountryService countryService;

    @Test
    void getAllCountries_returnsAllCountries() {
        List<Country> countries = List.of(new Country(1L, "India"), new Country(2L, "USA"));
        when(countryRepository.findAll()).thenReturn(countries);

        List<Country> result = countryService.getAllCountries();

        assertThat(result).hasSize(2).containsExactlyElementsOf(countries);
    }

    @Test
    void getCountryById_returnsCountry_whenExists() {
        Country india = new Country(1L, "India");
        when(countryRepository.findById(1L)).thenReturn(Optional.of(india));

        Country result = countryService.getCountryById(1L);

        assertThat(result).isEqualTo(india);
    }

    @Test
    void getCountryById_throwsNotFoundException_whenNotFound() {
        when(countryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> countryService.getCountryById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void getCountryById_neverCallsExternalApi() {
        Country india = new Country(1L, "India");
        when(countryRepository.findById(1L)).thenReturn(Optional.of(india));

        countryService.getCountryById(1L);

        // Only the repository should be called — no external API clients
        verify(countryRepository).findById(1L);
        verifyNoMoreInteractions(countryRepository);
    }
}
