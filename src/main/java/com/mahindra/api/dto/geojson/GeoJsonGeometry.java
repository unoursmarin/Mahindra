package com.mahindra.api.dto.geojson;

import java.util.List;

/**
 * GeoJSON Geometry object.
 * IMPORTANT: GeoJSON coordinate order is [longitude, latitude] — NOT [latitude, longitude].
 */
public record GeoJsonGeometry(String type, List<Double> coordinates) {

    public static GeoJsonGeometry point(double longitude, double latitude) {
        return new GeoJsonGeometry("Point", List.of(longitude, latitude));
    }
}
