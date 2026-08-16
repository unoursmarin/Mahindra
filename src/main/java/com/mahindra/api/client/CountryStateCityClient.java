package com.mahindra.api.client;

import com.mahindra.api.client.dto.CscCityResponse;
import com.mahindra.api.client.dto.CscCountryResponse;
import com.mahindra.api.exception.ExternalApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Service
public class CountryStateCityClient {

    private static final Logger log = LoggerFactory.getLogger(CountryStateCityClient.class);

    private final WebClient webClient;

    public CountryStateCityClient(@Qualifier("countryStateCityWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public List<CscCountryResponse> fetchAllCountries() {
        try {
            List<CscCountryResponse> result = webClient.get()
                    .uri("/v1/countries")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<CscCountryResponse>>() {})
                    .block();
            return result != null ? result : List.of();
        } catch (WebClientResponseException ex) {
            log.error("CSC API error fetching all countries: {} {}", ex.getStatusCode(), ex.getMessage());
            throw new ExternalApiException(ex.getStatusCode().value(),
                    "CountryStateCity API returned " + ex.getStatusCode().value());
        } catch (Exception ex) {
            log.error("Failed to reach CountryStateCity API for all countries", ex);
            throw new ExternalApiException(503, "Failed to connect to CountryStateCity API: " + ex.getMessage());
        }
    }

    public List<CscCityResponse> fetchCitiesByCountry(String cca2) {
        return fetchList("/v1/countries/" + cca2 + "/cities", CscCityResponse.class);
    }

    public CscCountryResponse fetchCountry(String cca2) {
        try {
            return webClient.get()
                    .uri("/v1/countries/" + cca2)
                    .retrieve()
                    .bodyToMono(CscCountryResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("CSC API error fetching country {}: {} {}", cca2, ex.getStatusCode(), ex.getMessage());
            throw new ExternalApiException(ex.getStatusCode().value(),
                    "CountryStateCity API returned " + ex.getStatusCode().value());
        } catch (Exception ex) {
            log.error("Failed to reach CountryStateCity API for country {}", cca2, ex);
            throw new ExternalApiException(503, "Failed to connect to CountryStateCity API: " + ex.getMessage());
        }
    }

    private <T> List<T> fetchList(String uri, Class<T> type) {
        try {
            List<T> result = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToFlux(type)
                    .collectList()
                    .block();
            return result != null ? result : List.of();
        } catch (WebClientResponseException ex) {
            log.error("CSC API error at {}: {} {}", uri, ex.getStatusCode(), ex.getMessage());
            throw new ExternalApiException(ex.getStatusCode().value(),
                    "CountryStateCity API returned " + ex.getStatusCode().value());
        } catch (Exception ex) {
            log.error("Failed to reach CountryStateCity API at {}", uri, ex);
            throw new ExternalApiException(503, "Failed to connect to CountryStateCity API: " + ex.getMessage());
        }
    }
}
