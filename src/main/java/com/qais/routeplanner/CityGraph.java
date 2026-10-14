package com.qais.routeplanner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A small undirected weighted graph with insertion-ordered locations. */
public final class CityGraph {
    private final Map<String, Location> locations = new LinkedHashMap<>();
    private final Map<String, List<Road>> adjacency = new LinkedHashMap<>();
    private final List<Road> roads = new ArrayList<>();

    public CityGraph addLocation(String name, int x, int y) {
        if (locations.containsKey(name)) throw new IllegalArgumentException("Duplicate location: " + name);
        Location location = new Location(name, x, y);
        locations.put(name, location);
        adjacency.put(name, new ArrayList<>());
        return this;
    }

    public CityGraph addRoad(String from, String to, double distanceKm, double risk) {
        requireLocation(from);
        requireLocation(to);
        Road road = new Road(from, to, distanceKm, risk);
        adjacency.get(from).add(road);
        adjacency.get(to).add(road);
        roads.add(road);
        return this;
    }

    public void requireLocation(String name) {
        if (!locations.containsKey(name)) throw new IllegalArgumentException("Unknown location: " + name);
    }

    public Location location(String name) {
        requireLocation(name);
        return locations.get(name);
    }

    public Collection<Location> locations() {
        return Collections.unmodifiableCollection(locations.values());
    }

    public List<Road> roads() {
        return Collections.unmodifiableList(roads);
    }

    public List<Road> adjacent(String location) {
        requireLocation(location);
        return Collections.unmodifiableList(adjacency.get(location));
    }
}
