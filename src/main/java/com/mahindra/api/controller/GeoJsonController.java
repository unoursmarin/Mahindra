package com.mahindra.api.controller;

import com.mahindra.api.dto.geojson.GeoJsonFeature;
import com.mahindra.api.dto.geojson.GeoJsonFeatureCollection;
import com.mahindra.api.service.GeoJsonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/countries")
@Tag(name = "GeoJSON", description = "GeoJSON endpoints for map visualization")
public class GeoJsonController {

    private final GeoJsonService geoJsonService;

    public GeoJsonController(GeoJsonService geoJsonService) {
        this.geoJsonService = geoJsonService;
    }

    @GetMapping("/geojson")
    @Operation(summary = "All countries as GeoJSON FeatureCollection",
               description = "Returns capital city coordinates for all countries with geo data.")
    @ApiResponse(responseCode = "200", description = "GeoJSON FeatureCollection")
    public ResponseEntity<GeoJsonFeatureCollection> allCountries() {
        return ResponseEntity.ok(geoJsonService.allCountriesAsGeoJson());
    }

    @GetMapping("/{id}/geojson")
    @Operation(summary = "Single country as GeoJSON Feature",
               description = "Returns capital coordinates and metadata for one country. Triggers geo-enrichment if needed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "GeoJSON Feature"),
            @ApiResponse(responseCode = "404", description = "Country not found")
    })
    public ResponseEntity<GeoJsonFeature> oneCountry(
            @Parameter(description = "Country ID") @PathVariable Long id) {
        return ResponseEntity.ok(geoJsonService.countryAsGeoJson(id));
    }

    @GetMapping("/{id}/cities/geojson")
    @Operation(summary = "Cities for a country as GeoJSON FeatureCollection",
               description = "Returns city coordinates. Triggers lazy enrichment from CSC API if no cities cached.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "GeoJSON FeatureCollection of city Points"),
            @ApiResponse(responseCode = "404", description = "Country not found")
    })
    public ResponseEntity<GeoJsonFeatureCollection> citiesForCountry(
            @Parameter(description = "Country ID") @PathVariable Long id) {
        return ResponseEntity.ok(geoJsonService.citiesAsGeoJson(id));
    }
}
