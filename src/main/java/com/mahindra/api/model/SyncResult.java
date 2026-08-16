package com.mahindra.api.model;

import java.util.List;

public record SyncResult(
        int countriesInserted,
        int countriesSkipped,
        int citiesInserted,
        int citiesSkipped,
        List<String> errors,
        String timestamp
) {
    /** Total countries examined (inserted + skipped). */
    public int countriesSynced() {
        return countriesInserted + countriesSkipped;
    }

    /** Total cities examined (inserted + skipped). */
    public int citiesSynced() {
        return citiesInserted + citiesSkipped;
    }
}
