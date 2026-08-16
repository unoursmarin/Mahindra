package com.mahindra.api.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahindra.api.model.City;
import com.mahindra.api.model.Country;
import com.mahindra.api.repository.CityRepository;
import com.mahindra.api.repository.CountryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JsonSeedRunnerTest {

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private CityRepository cityRepository;

    private JsonSeedRunner runner;

    @BeforeEach
    void setUp() {
        runner = new JsonSeedRunner(countryRepository, cityRepository, new ObjectMapper());
        ReflectionTestUtils.setField(runner, "seedFromJson", true);
    }

    @Test
    void run_skipsWhenDisabled() throws Exception {
        ReflectionTestUtils.setField(runner, "seedFromJson", false);

        runner.run(new DefaultApplicationArguments());

        verifyNoInteractions(countryRepository, cityRepository);
    }

    @Test
    void run_skipsWhenDatabaseAlreadyPopulated() throws Exception {
        when(countryRepository.count()).thenReturn(195L);
        when(cityRepository.count()).thenReturn(152598L);

        runner.run(new DefaultApplicationArguments());

        verify(countryRepository, never()).save(any());
        verify(cityRepository, never()).saveAll(anyList());
    }

    @Test
    void run_seedsCountriesAndCitiesWhenDbEmpty() throws Exception {
        when(countryRepository.count()).thenReturn(0L);

        Country saved = new Country();
        saved.setId(1L);
        saved.setCca2("AF");
        when(countryRepository.save(any())).thenReturn(saved);
        when(cityRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        runner.run(new DefaultApplicationArguments());

        // At least one country must have been saved
        verify(countryRepository, atLeastOnce()).save(any(Country.class));
        // Cities should be batch-saved
        verify(cityRepository, atLeastOnce()).saveAll(anyList());
    }

    @Test
    void run_mapsCountryFieldsCorrectly() throws Exception {
        when(countryRepository.count()).thenReturn(0L);

        ArgumentCaptor<Country> captor = ArgumentCaptor.forClass(Country.class);
        Country saved = new Country();
        saved.setId(1L);
        when(countryRepository.save(captor.capture())).thenReturn(saved);
        when(cityRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        runner.run(new DefaultApplicationArguments());

        Country first = captor.getAllValues().get(0);
        assertThat(first.getCca2()).isNotBlank();
        assertThat(first.getName()).isNotBlank();
        assertThat(first.getFlagUrl()).contains("flagcdn.com");
        assertThat(first.getFlagUrl()).contains(first.getCca2().toLowerCase());
    }

    @Test
    void run_cityBatchContainsMappedFields() throws Exception {
        when(countryRepository.count()).thenReturn(0L);

        Country saved = new Country();
        saved.setId(42L);
        when(countryRepository.save(any())).thenReturn(saved);

        ArgumentCaptor<List<City>> batchCaptor = ArgumentCaptor.forClass(List.class);
        when(cityRepository.saveAll(batchCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        runner.run(new DefaultApplicationArguments());

        // At least one batch saved with cities referencing the country ID
        List<City> firstBatch = batchCaptor.getAllValues().get(0);
        assertThat(firstBatch).isNotEmpty();
        firstBatch.forEach(city -> {
            assertThat(city.getName()).isNotBlank();
            assertThat(city.getCountryId()).isEqualTo(42L);
        });
    }
}
