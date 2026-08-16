package com.mahindra.api.controller;

import com.mahindra.api.model.City;
import com.mahindra.api.model.PaginatedResponse;
import com.mahindra.api.service.CityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Cities", description = "Operations related to cities")
public class CityController {

    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @GetMapping("/countries/{countryId}/cities")
    @Operation(
            summary = "List cities for a country (paginated)",
            description = "Returns a paginated list of cities belonging to the specified country."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of cities"),
            @ApiResponse(responseCode = "404", description = "Country not found",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<PaginatedResponse<City>> listCitiesByCountry(
            @Parameter(description = "ID of the country", required = true)
            @PathVariable Long countryId,
            @Parameter(description = "Zero-based page index (default: 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of records per page (default: 10)")
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(cityService.getCitiesByCountry(countryId, page, size));
    }

    @GetMapping("/cities/{cityId}")
    @Operation(
            summary = "Get city details by ID",
            description = "Returns detailed information for a specific city."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "City details",
                    content = @Content(schema = @Schema(implementation = City.class))),
            @ApiResponse(responseCode = "404", description = "City not found",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    public ResponseEntity<City> getCityById(
            @Parameter(description = "ID of the city", required = true)
            @PathVariable Long cityId
    ) {
        return ResponseEntity.ok(cityService.getCityById(cityId));
    }
}
