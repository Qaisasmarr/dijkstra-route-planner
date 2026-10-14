package com.qais.routeplanner;

import java.util.List;

/** Dependency-free assertions for the CLI, compatible with Java 17+. */
public final class RouteEngineTest {
    private static int checks = 0;
    private RouteEngineTest() { }

    public static void main(String[] args) {
        shortestChoosesDirectRoute();
        safetyConstraintsChangeRoute();
        intermediateTradeoffGetsSelected();
        impossibleBudgetReturnsEmpty();
        disconnectedGraphReturnsEmpty();
        sameNodeReturnsZero();
        rejectsUnknownLocationAndNegativeWeights();
        detectsZeroRiskPath();
        demoMapOffersValidRoutes();
        System.out.println("PASS — " + checks + " checks");
    }

    private static CityGraph fixture() {
        // A-B-D: shorter but riskier (2 km, risk 10)
        // A-C-D: longer and lower risk (4 km, risk 2)
        return new CityGraph().addLocation("A", 0, 0).addLocation("B", 1, 1)
                .addLocation("C", 2, 2).addLocation("D", 3, 3)
                .addRoad("A", "B", 1, 5).addRoad("B", "D", 1, 5)
                .addRoad("A", "C", 2, 1).addRoad("C", "D", 2, 1);
    }

    private static void shortestChoosesDirectRoute() {
        Route result = new RouteEngine(fixture()).shortest("A", "D").orElseThrow();
        assertEquals(List.of("A", "B", "D"), result.stops(), "minimum distance path");
        assertNear(2, result.distanceKm(), "minimum distance");
        assertNear(10, result.totalRisk(), "shortest route risk");
    }

    private static void safetyConstraintsChangeRoute() {
        RouteEngine engine = new RouteEngine(fixture());
        Route safest = engine.safest("A", "D").orElseThrow();
        assertEquals(List.of("A", "C", "D"), safest.stops(), "least risk route");
        Route balanced = engine.withinRiskBudget("A", "D", 3).orElseThrow();
        assertEquals(List.of("A", "C", "D"), balanced.stops(), "constrained route");
        assertNear(4, balanced.distanceKm(), "constrained distance");
        Route openBudget = engine.withinRiskBudget("A", "D", 10).orElseThrow();
        assertNear(2, openBudget.distanceKm(), "unconstrained if within budget");
    }

    private static void intermediateTradeoffGetsSelected() {
        // Three alternative paths: fast/risky (2,10), medium (3,4), slow/safe (5,1).
        CityGraph graph = new CityGraph()
                .addLocation("A", 0, 0).addLocation("B", 1, 1)
                .addLocation("C", 2, 2).addLocation("E", 3, 3).addLocation("D", 4, 4)
                .addRoad("A", "B", 1, 5).addRoad("B", "D", 1, 5)
                .addRoad("A", "C", 2.5, 0.5).addRoad("C", "D", 2.5, 0.5)
                .addRoad("A", "E", 1.5, 2).addRoad("E", "D", 1.5, 2);
        Route found = new RouteEngine(graph).withinRiskBudget("A", "D", 5).orElseThrow();
        assertEquals(List.of("A", "E", "D"), found.stops(), "intermediate Pareto route");
        assertNear(3, found.distanceKm(), "intermediate distance");
        assertNear(4, found.totalRisk(), "intermediate risk");
    }

    private static void impossibleBudgetReturnsEmpty() {
        assertTrue(new RouteEngine(fixture()).withinRiskBudget("A", "D", 1).isEmpty(),
                "unachievable risk limit");
    }

    private static void disconnectedGraphReturnsEmpty() {
        CityGraph graph = new CityGraph().addLocation("A", 0, 0).addLocation("B", 10, 10);
        RouteEngine engine = new RouteEngine(graph);
        assertTrue(engine.shortest("A", "B").isEmpty(), "disconnected shortest");
        assertTrue(engine.safest("A", "B").isEmpty(), "disconnected safest");
        assertTrue(engine.withinRiskBudget("A", "B", 100).isEmpty(), "disconnected balanced");
    }

    private static void sameNodeReturnsZero() {
        Route result = new RouteEngine(fixture()).shortest("A", "A").orElseThrow();
        assertEquals(List.of("A"), result.stops(), "same source and target");
        assertNear(0, result.distanceKm(), "zero distance");
    }

    private static void rejectsUnknownLocationAndNegativeWeights() {
        assertThrows(() -> new RouteEngine(fixture()).shortest("Missing", "A"),
                "unknown source rejected");
        assertThrows(() -> fixture().addRoad("A", "D", -1, 0), "negative distance rejected");
        assertThrows(() -> fixture().addRoad("A", "D", 1, -1), "negative risk rejected");
        assertThrows(() -> new RouteEngine(fixture()).withinRiskBudget("A", "D", -1),
                "invalid budget rejected");
    }

    private static void detectsZeroRiskPath() {
        CityGraph graph = new CityGraph().addLocation("A", 0, 0).addLocation("B", 10, 10)
                .addRoad("A", "B", 2.5, 0);
        assertNear(2.5, new RouteEngine(graph).withinRiskBudget("A", "B", 0)
                .orElseThrow().distanceKm(), "zero risk edge");
    }

    private static void demoMapOffersValidRoutes() {
        RouteEngine engine = new RouteEngine(DemoCity.build());
        Route shortRoute = engine.shortest("Downtown", "University").orElseThrow();
        Route safeRoute = engine.safest("Downtown", "University").orElseThrow();
        Route budgetRoute = engine.withinRiskBudget("Downtown", "University", 6).orElseThrow();
        assertTrue(shortRoute.distanceKm() <= safeRoute.distanceKm() + 1e-9, "shortest is no longer than safest");
        assertTrue(budgetRoute.totalRisk() <= 6 + 1e-9, "demo route respects risk budget");
        assertEquals("Downtown", budgetRoute.stops().get(0), "route begins at source");
        assertEquals("University", budgetRoute.stops().get(budgetRoute.stops().size() - 1), "route ends at target");
    }

    private static void assertNear(double expected, double actual, String description) {
        assertTrue(Math.abs(expected - actual) < 1e-7, description + " (expected " + expected + ", got " + actual + ")");
    }

    private static void assertEquals(Object expected, Object actual, String description) {
        assertTrue(expected.equals(actual), description + " (expected " + expected + ", got " + actual + ")");
    }

    private static void assertThrows(Runnable action, String description) {
        boolean thrown = false;
        try { action.run(); } catch (IllegalArgumentException exception) { thrown = true; }
        assertTrue(thrown, description);
    }

    private static void assertTrue(boolean passes, String description) {
        checks++;
        if (!passes) throw new AssertionError(description);
    }
}
