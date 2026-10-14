package com.qais.routeplanner;

/** Undirected street segment; risk is a fictional additive score, not real-world safety data. */
public record Road(String from, String to, double distanceKm, double risk) {
    public Road {
        if (from == null || to == null || from.equals(to)) {
            throw new IllegalArgumentException("Road endpoints must be different");
        }
        if (!Double.isFinite(distanceKm) || distanceKm <= 0 || !Double.isFinite(risk) || risk < 0) {
            throw new IllegalArgumentException("Distance must be positive and risk must be nonnegative");
        }
    }

    public String other(String endpoint) {
        if (from.equals(endpoint)) return to;
        if (to.equals(endpoint)) return from;
        throw new IllegalArgumentException("Location " + endpoint + " is not on this road");
    }
}
