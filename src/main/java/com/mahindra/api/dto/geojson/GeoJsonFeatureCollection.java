package com.mahindra.api.dto.geojson;

import java.util.List;

public record GeoJsonFeatureCollection(String type, List<GeoJsonFeature> features) {

    public static GeoJsonFeatureCollection of(List<GeoJsonFeature> features) {
        return new GeoJsonFeatureCollection("FeatureCollection", features);
    }
}
