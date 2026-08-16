package com.mahindra.api.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CscCountryResponse(
        String name,
        String iso2,
        String capital,
        String region,
        String subregion,
        String phonecode,
        String emoji,
        @JsonProperty("latitude")  String latitudeStr,
        @JsonProperty("longitude") String longitudeStr
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
