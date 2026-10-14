package com.qais.routeplanner;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** CLI and desktop entry point. Java 17+. */
public final class Main {
    private Main() { }

    public static void main(String[] args) {
        try {
            if (args.length == 0 || Arrays.asList(args).contains("--gui")) {
                if (GraphicsEnvironment.isHeadless()) {
                    System.out.println("No graphical display found. Use --help for CLI commands.");
                } else {
                    SwingUtilities.invokeLater(() -> new RoutePlannerWindow().setVisible(true));
                }
                return;
            }
            CityGraph city = DemoCity.build();
            List<String> parameters = Arrays.asList(args);
            if (parameters.contains("--help")) {
                help();
                return;
            }
            if (parameters.contains("--list")) {
                city.locations().forEach(place -> System.out.println("• " + place.name()));
                return;
            }
            String from = getArg(parameters, "--from", "Downtown");
            String to = getArg(parameters, "--to", "University");
            String mode = getArg(parameters, "--mode", "shortest").toLowerCase(Locale.ROOT);
            double budget = Double.parseDouble(getArg(parameters, "--risk-budget", "8"));
            RouteEngine engine = new RouteEngine(city);
            Optional<Route> route = switch (mode) {
                case "shortest" -> engine.shortest(from, to);
                case "safest" -> engine.safest(from, to);
                case "balanced" -> engine.withinRiskBudget(from, to, budget);
                default -> throw new IllegalArgumentException("Unknown mode: " + mode);
            };
            System.out.println(route.map(Route::describe)
                    .orElse("No route satisfies the requested conditions."));
        } catch (IllegalArgumentException error) {
            System.err.println("Error: " + error.getMessage());
            System.exit(2);
        }
    }

    private static String getArg(List<String> args, String flag, String fallback) {
        int at = args.indexOf(flag);
        if (at == -1) return fallback;
        if (at + 1 >= args.size() || args.get(at + 1).startsWith("--")) {
            throw new IllegalArgumentException("Missing value for " + flag);
        }
        return args.get(at + 1);
    }

    private static void help() {
        System.out.println("""
            RouteLab — Dijkstra route planning with a fictional city map
              java -cp build/classes com.qais.routeplanner.Main --gui
              java -cp build/classes com.qais.routeplanner.Main --list
              java -cp build/classes com.qais.routeplanner.Main --from Downtown --to University --mode shortest
              java -cp build/classes com.qais.routeplanner.Main --from Downtown --to University --mode safest
              java -cp build/classes com.qais.routeplanner.Main --from Downtown --to University --mode balanced --risk-budget 8
            Modes: shortest = minimum distance; safest = minimum risk;
                   balanced = shortest feasible path found with LARAC approximation.
            Use quotes for location names containing spaces. Values are illustrative only.
            """);
    }
}
