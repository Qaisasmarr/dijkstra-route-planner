package com.qais.routeplanner;

/** Location on a fictional demonstration city map, in canvas coordinates. */
public record Location(String name, int x, int y) {
    public Location {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Location name must be nonempty");
        }
    }
}
