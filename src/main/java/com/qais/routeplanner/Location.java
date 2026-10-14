package com.qais.routeplanner;

/** A named location on the fictional city map.
 * Canvas coordinates control rendering positions, not shortest-path distances.
 */
public record Location(String name, int x, int y) {
    public Location {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Location name must be nonempty");
        }
    }
}
