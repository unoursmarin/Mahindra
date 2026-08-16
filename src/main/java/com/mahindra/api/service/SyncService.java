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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class SyncService {

    private static final Logger log = LoggerFactory.getLogger(SyncService.class);

    @Value("${countrystatecity.sync-on-startup:true}")
    private boolean syncOnStartup;

    @Value("${countrystatecity.inter-call-delay-ms:150}")
    private long interCallDelayMs;

    private final CountryStateCityClient cscClient;
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;

    public SyncService(CountryStateCityClient cscClient,
                       CountryRepository countryRepository,
                       CityRepository cityRepository) {
        this.cscClient = cscClient;
        this.countryRepository = countryRepository;
        this.cityRepository = cityRepository;
    }

    /**
     * Fires after all ApplicationRunners (including JsonSeedRunner) complete.
     * Adds any entries missing from the DB compared to the external API.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        if (!syncOnStartup) return;
        try {
            SyncResult result = syncFromExternalApi();
            log.info("Startup sync completed: {} countries ({} new, {} already present), " +
                            "{} cities ({} new, {} already present), {} errors",
                    result.countriesSynced(), result.countriesInserted(), result.countriesSkipped(),
                    result.citiesSynced(), result.citiesInserted(), result.citiesSkipped(),
                    result.errors().size());
        } catch (Exception ex) {
            log.warn("Startup sync failed. Reason: {}", ex.getMessage());
        }
    }

    /**
     * Additive-only sync: fetches all countries and cities from the CountryStateCity API
     * and inserts only entries not already present in the database.
     * Existing records are never modified or deleted.
     */
    @Transactional
    public SyncResult syncFromExternalApi() {
        List<CscCountryResponse> allCountries = cscClient.fetchAllCountries();
        log.info("Fetched {} countries from CountryStateCity API", allCountries.size());

        int countriesInserted = 0;
        int countriesSkipped = 0;
        int citiesInserted = 0;
        int citiesSkipped = 0;
        List<String> errors = new ArrayList<>();

        int total = allCountries.size();
        boolean cityApiAccessible = true;

        for (int i = 0; i < total; i++) {
            CscCountryResponse dto = allCountries.get(i);
            if (dto.iso2() == null || dto.iso2().isBlank()) continue;

            log.debug("Syncing country {}/{}: {} ({})", i + 1, total, dto.name(), dto.iso2());

            // Additive-only: skip country if already present
            Country country = countryRepository.findByCca2(dto.iso2()).orElse(null);
            if (country == null) {
                Country newCountry = new Country();
                newCountry.setName(dto.name());
                newCountry.setCca2(dto.iso2());
                newCountry.setCapital(dto.capital());
                newCountry.setRegion(dto.region());
                newCountry.setPhoneCode(dto.phonecode());
                newCountry.setEmoji(dto.emoji());
                newCountry.setCapitalLat(dto.latitude());
                newCountry.setCapitalLng(dto.longitude());
                if (dto.iso2() != null) {
                    newCountry.setFlagUrl("https://flagcdn.com/w320/" + dto.iso2().toLowerCase() + ".png");
                }
                country = countryRepository.save(newCountry);
                countriesInserted++;
            } else {
                countriesSkipped++;
            }

            // Additive-only: fetch cities and insert only missing ones.
            // Skip city sync entirely if a previous call already returned 403
            // (the API key lacks city endpoint access — no point retrying for every country).
            if (!cityApiAccessible) continue;

            try {
                List<CscCityResponse> cityDtos = cscClient.fetchCitiesByCountry(dto.iso2());
                for (CscCityResponse cityDto : cityDtos) {
                    if (cityDto.name() == null || cityDto.name().isBlank()) continue;
                    String stateCode = cityDto.stateCode() != null ? cityDto.stateCode() : "";

                    boolean exists = cityRepository
                            .findByNameAndCountryIdAndStateCode(cityDto.name(), country.getId(), stateCode)
                            .isPresent();

                    if (exists) {
                        citiesSkipped++;
                    } else {
                        City city = new City();
                        city.setName(cityDto.name());
                        city.setCountryId(country.getId());
                        city.setStateCode(stateCode);
                        city.setStateName(cityDto.stateName());
                        city.setLatitude(cityDto.latitude());
                        city.setLongitude(cityDto.longitude());
                        cityRepository.save(city);
                        citiesInserted++;
                    }
                }
            } catch (ExternalApiException ex) {
                if (ex.getStatusCode() == 403) {
                    log.warn("City sync aborted: API returned 403 Forbidden for {} — " +
                            "the API key does not have access to city endpoints. " +
                            "Countries sync will continue; cities will not be updated via API.", dto.iso2());
                    cityApiAccessible = false;
                } else {
                    String msg = "Failed to sync cities for " + dto.iso2() + ": " + ex.getMessage();
                    log.warn(msg);
                    errors.add(msg);
                }
            } catch (Exception ex) {
                String msg = "Failed to sync cities for " + dto.iso2() + ": " + ex.getMessage();
                log.warn(msg);
                errors.add(msg);
            }

            // Respect API rate limits
            if (interCallDelayMs > 0 && i < total - 1) {
                try {
                    Thread.sleep(interCallDelayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        return new SyncResult(countriesInserted, countriesSkipped, citiesInserted, citiesSkipped,
                List.copyOf(errors), Instant.now().toString());
    }
}
