package com.mahindra.api.batch;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahindra.api.batch.dto.JsonCityEntry;
import com.mahindra.api.batch.dto.JsonCountryEntry;
import com.mahindra.api.batch.dto.JsonStateEntry;
import com.mahindra.api.model.City;
import com.mahindra.api.model.Country;
import com.mahindra.api.repository.CityRepository;
import com.mahindra.api.repository.CountryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Seeds the database from the bundled countries+states+cities.json when the DB is empty.
 * Runs as Order(1) — before the API sync EventListener which fires after all ApplicationRunners complete.
 */
@Component
@Order(1)
public class JsonSeedRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(JsonSeedRunner.class);
    private static final int CITY_BATCH_SIZE = 500;
    private static final String JSON_FILE = "countries+states+cities.json";

    @Value("${countrystatecity.seed-from-json:true}")
    private boolean seedFromJson;

    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final ObjectMapper objectMapper;

    public JsonSeedRunner(CountryRepository countryRepository,
                          CityRepository cityRepository,
                          ObjectMapper objectMapper) {
        this.countryRepository = countryRepository;
        this.cityRepository = cityRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!seedFromJson) {
            log.info("JSON seed disabled (countrystatecity.seed-from-json=false)");
            return;
        }
        if (countryRepository.count() > 0 && cityRepository.count() > 0) {
            log.info("Database already populated ({} countries, {} cities) — skipping JSON seed",
                    countryRepository.count(), cityRepository.count());
            return;
        }

        log.info("Database is empty — seeding from {}", JSON_FILE);
        long start = System.currentTimeMillis();

        List<JsonCountryEntry> entries;
        try (InputStream in = new ClassPathResource(JSON_FILE).getInputStream()) {
            entries = objectMapper.readValue(in, new TypeReference<List<JsonCountryEntry>>() {});
        }

        log.info("Parsed {} countries from JSON file", entries.size());

        int countriesLoaded = 0;
        int citiesLoaded = 0;
        int citiesSkipped = 0;
        List<City> cityBatch = new ArrayList<>(CITY_BATCH_SIZE);
        // Tracks (name|countryId|stateCode) keys already queued in this seed run
        // to guard against duplicate entries within the JSON file itself.
        Set<String> seenCityKeys = new HashSet<>();

        for (int i = 0; i < entries.size(); i++) {
            JsonCountryEntry entry = entries.get(i);
            if (entry.iso2() == null || entry.iso2().isBlank()) continue;

            Country country = countryRepository.findByCca2(entry.iso2())
                    .orElseGet(() -> {
                        Country saved = countryRepository.save(buildCountry(entry));
                        return saved;
                    });
            countriesLoaded++;

            if (entry.states() != null) {
                for (JsonStateEntry state : entry.states()) {
                    if (state.cities() == null) continue;
                    String stateCode = state.iso2() != null ? state.iso2() : "";
                    String stateName = state.name() != null ? state.name() : "";

                    for (JsonCityEntry cityEntry : state.cities()) {
                        if (cityEntry.name() == null || cityEntry.name().isBlank()) continue;

                        String key = cityEntry.name() + "|" + country.getId() + "|" + stateCode;
                        if (!seenCityKeys.add(key)) {
                            citiesSkipped++;
                            continue; // duplicate within the JSON — skip
                        }

                        cityBatch.add(buildCity(cityEntry, country.getId(), stateCode, stateName));

                        if (cityBatch.size() >= CITY_BATCH_SIZE) {
                            cityRepository.saveAll(cityBatch);
                            citiesLoaded += cityBatch.size();
                            cityBatch.clear();
                        }
                    }
                }
            }

            if ((i + 1) % 50 == 0) {
                log.info("Seeded {}/{} countries, {} cities so far…", i + 1, entries.size(), citiesLoaded);
            }
        }

        if (!cityBatch.isEmpty()) {
            cityRepository.saveAll(cityBatch);
            citiesLoaded += cityBatch.size();
        }

        long elapsed = System.currentTimeMillis() - start;
        log.info("JSON seed complete: {} countries, {} cities loaded, {} duplicate city entries skipped, in {}ms",
                countriesLoaded, citiesLoaded, citiesSkipped, elapsed);
    }

    private Country buildCountry(JsonCountryEntry entry) {
        Country country = new Country();
        country.setName(entry.name());
        country.setCca2(entry.iso2());
        country.setCapital(entry.capital());
        country.setRegion(entry.region());
        country.setPhoneCode(entry.phonecode());
        country.setEmoji(entry.emoji());
        country.setPopulation(entry.population());
        country.setCapitalLat(entry.latitude());
        country.setCapitalLng(entry.longitude());
        country.setFlagUrl("https://flagcdn.com/w320/" + entry.iso2().toLowerCase() + ".png");
        return country;
    }

    private City buildCity(JsonCityEntry entry, Long countryId, String stateCode, String stateName) {
        City city = new City();
        city.setName(entry.name());
        city.setCountryId(countryId);
        city.setStateCode(stateCode);
        city.setStateName(stateName);
        city.setLatitude(entry.latitude());
        city.setLongitude(entry.longitude());
        return city;
    }
}
