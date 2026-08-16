package com.mahindra.api.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CscCityResponse(
        String name,
        @JsonProperty("latitude")  String latitudeStr,
        @JsonProperty("longitude") String longitudeStr,
        @JsonProperty("state_code") String stateCode,
        @JsonProperty("state_name") String stateName
) {
    /** Parses latitude from the API's string format; returns null if blank/invalid. */
    public Double latitude() {
        try {
            return latitudeStr != null && !latitudeStr.isBlank() ? Double.parseDouble(latitudeStr) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Parses longitude from the API's string format; returns null if blank/invalid. */
    public Double longitude() {
        try {
            return longitudeStr != null && !longitudeStr.isBlank() ? Double.parseDouble(longitudeStr) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
