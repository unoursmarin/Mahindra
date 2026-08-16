package com.mahindra.api.batch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JsonStateEntry(
        String name,
        String iso2,
        List<JsonCityEntry> cities
) {}
