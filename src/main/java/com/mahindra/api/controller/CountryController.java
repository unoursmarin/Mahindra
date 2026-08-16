package com.mahindra.api.controller;

import com.mahindra.api.model.Country;
import com.mahindra.api.service.CountryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/countries")
@Tag(name = "Countries", description = "Operations related to countries")
public class CountryController {

    private final CountryService countryService;

    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    @GetMapping
    @Operation(
            summary = "List all countries",
            description = "Returns a complete list of all available countries."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successful operation",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Country.class)))
    )
    public ResponseEntity<List<Country>> listCountries() {
        return ResponseEntity.ok(countryService.getAllCountries());
    }
}
