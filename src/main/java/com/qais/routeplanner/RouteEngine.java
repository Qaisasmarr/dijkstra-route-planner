package com.qais.routeplanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.ToDoubleFunction;

/** Dijkstra shortest paths plus LARAC-style risk-constrained routing. */
public final class RouteEngine {
    private static final double EPS = 1e-9;
    private static final int MAX_LARAC_ITERATIONS = 200;
    private final CityGraph graph;

    public RouteEngine(CityGraph graph) {
        this.graph = graph;
    }

    /** Find minimum distance in kilometers (Dijkstra). */
    public Optional<Route> shortest(String source, String destination) {
        return dijkstra(source, destination, Road::distanceKm);
    }

    /** Find minimum total fictional risk (Dijkstra with another edge weight). */
    public Optional<Route> safest(String source, String destination) {
        return dijkstra(source, destination, Road::risk);
    }

    /**
     * LARAC (Lagrangian Relaxation based Aggregated Cost) approximation.
     * The objective is minimum distance subject to sum of risk <= budget.
     * LARAC uses two Dijkstra routes as bounds and repeats Dijkstra with a
     * blended distance + lambda*risk edge cost. Returns a feasible path, but
     * is not guaranteed to find the globally optimal constrained path.
     */
    public Optional<Route> withinRiskBudget(String source, String destination, double budget) {
        if (!Double.isFinite(budget) || budget < 0) {
            throw new IllegalArgumentException("Risk budget must be a finite nonnegative number");
        }
        Optional<Route> direct = shortest(source, destination);
        if (direct.isEmpty() || direct.get().totalRisk() <= budget + EPS) return direct;
        Optional<Route> leastRisk = safest(source, destination);
        if (leastRisk.isEmpty() || leastRisk.get().totalRisk() > budget + EPS) return Optional.empty();

        Route infeasible = direct.get();
        Route feasible = leastRisk.get();
        Set<List<String>> seen = new HashSet<>();
        seen.add(infeasible.stops());
        seen.add(feasible.stops());
        for (int i = 0; i < MAX_LARAC_ITERATIONS; i++) {
            double denominator = infeasible.totalRisk() - feasible.totalRisk();
            if (denominator <= EPS) break;
            double lambda = (feasible.distanceKm() - infeasible.distanceKm()) / denominator;
            if (lambda < 0) lambda = 0;
            final double penalty = lambda;
            Optional<Route> next = dijkstra(source, destination, road -> road.distanceKm() + penalty * road.risk());
            if (next.isEmpty()) break;
            Route candidate = next.get();
            if (!seen.add(candidate.stops())) break;
            if (candidate.totalRisk() <= budget + EPS) {
                if (candidate.distanceKm() < feasible.distanceKm() - EPS) feasible = candidate;
            } else {
                infeasible = candidate;
            }
        }
        return Optional.of(feasible);
    }

    private record QueueItem(String name, double cost, long order) implements Comparable<QueueItem> {
        public int compareTo(QueueItem other) {
            int cmp = Double.compare(cost, other.cost);
            return cmp != 0 ? cmp : Long.compare(order, other.order);
        }
    }

    /** Nonnegative edges only; O((V+E) log V) with a binary heap. */
    private Optional<Route> dijkstra(String source, String destination, ToDoubleFunction<Road> costFn) {
        graph.requireLocation(source);
        graph.requireLocation(destination);
        if (source.equals(destination)) return Optional.of(new Route(List.of(source), 0, 0));
        Map<String, Double> distances = new HashMap<>();
        Map<String, String> parent = new HashMap<>();
        Map<String, Road> previousRoad = new HashMap<>();
        PriorityQueue<QueueItem> queue = new PriorityQueue<>();
        Set<String> visited = new HashSet<>();
        long order = 0;
        distances.put(source, 0.0);
        queue.add(new QueueItem(source, 0, order++));
        while (!queue.isEmpty()) {
            QueueItem current = queue.poll();
            if (!visited.add(current.name())) continue;
            if (current.name().equals(destination)) break;
            for (Road road : graph.adjacent(current.name())) {
                String neighbor = road.other(current.name());
                double weight = costFn.applyAsDouble(road);
                if (!Double.isFinite(weight) || weight < 0) {
                    throw new IllegalArgumentException("Dijkstra requires finite, nonnegative edge weights");
                }
                double newCost = current.cost() + weight;
                if (!visited.contains(neighbor) && newCost + EPS < distances.getOrDefault(neighbor, Double.POSITIVE_INFINITY)) {
                    distances.put(neighbor, newCost);
                    parent.put(neighbor, current.name());
                    previousRoad.put(neighbor, road);
                    queue.add(new QueueItem(neighbor, newCost, order++));
                }
            }
        }
        if (!distances.containsKey(destination)) return Optional.empty();
        List<String> nodes = new ArrayList<>();
        double totalDistance = 0;
        double totalRisk = 0;
        String node = destination;
        while (!node.equals(source)) {
            nodes.add(node);
            Road road = previousRoad.get(node);
            totalDistance += road.distanceKm();
            totalRisk += road.risk();
            node = parent.get(node);
        }
        nodes.add(source);
        Collections.reverse(nodes);
        return Optional.of(new Route(nodes, totalDistance, totalRisk));
    }
}
