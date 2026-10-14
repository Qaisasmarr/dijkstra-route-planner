package com.qais.routeplanner;

import java.util.List;
import java.util.Locale;

public record Route(List<String> stops, double distanceKm, double totalRisk) {
    public Route {
        stops = List.copyOf(stops);
    }

    public String describe() {
        return String.format(Locale.ROOT, "%s%nDistance: %.2f km | Fictional risk score: %.2f",
                String.join("  ->  ", stops), distanceKm, totalRisk);
    }
}
