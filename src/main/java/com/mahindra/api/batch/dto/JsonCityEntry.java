package com.mahindra.api.batch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JsonCityEntry(
        String name,
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
