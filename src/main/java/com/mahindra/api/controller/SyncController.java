package com.mahindra.api.controller;

import com.mahindra.api.model.SyncResult;
import com.mahindra.api.service.SyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sync")
@Tag(name = "Sync", description = "Triggers additive synchronisation with the CountryStateCity external API")
public class SyncController {

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping
    @Operation(
            summary = "Sync countries and cities from CountryStateCity API",
            description = "Fetches all countries and cities from the CountryStateCity API and inserts " +
                          "only entries missing from the database. Existing records are never modified or deleted."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sync completed successfully",
                    content = @Content(schema = @Schema(implementation = SyncResult.class))),
            @ApiResponse(responseCode = "502", description = "RestCountries.com API unreachable or returned an error")
    })
    public ResponseEntity<SyncResult> sync() {
        return ResponseEntity.ok(syncService.syncFromExternalApi());
    }
}
