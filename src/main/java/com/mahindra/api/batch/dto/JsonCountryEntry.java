package com.mahindra.api.batch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JsonCountryEntry(
        String name,
        String iso2,
        String phonecode,
        String capital,
        String region,
        @JsonProperty("population") Long population,
        @JsonProperty("latitude")  String latitudeStr,
        @JsonProperty("longitude") String longitudeStr,
        String emoji,
        List<JsonStateEntry> states
) {
    public Double latitude() {
        try {
            return latitudeStr != null && !latitudeStr.isBlank() ? Double.parseDouble(latitudeStr) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Double longitude() {
        try {
            return longitudeStr != null && !longitudeStr.isBlank() ? Double.parseDouble(longitudeStr) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
