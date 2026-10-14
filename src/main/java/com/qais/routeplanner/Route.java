package com.qais.routeplanner;

import java.util.List;
import java.util.Locale;

/** Immutable route result with ordered stops, total distance, and fictional risk.
 * The formatter produces a human-readable explanation for the command-line interface.
 */
public record Route(List<String> stops, double distanceKm, double totalRisk) {
    public Route {
        stops = List.copyOf(stops);
    }

    public String describe() {
        return String.format(Locale.ROOT, "%s%nDistance: %.2f km | Fictional risk score: %.2f",
                String.join("  ->  ", stops), distanceKm, totalRisk);
    }
}
