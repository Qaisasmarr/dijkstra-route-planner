package com.qais.routeplanner;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/** Self-contained interactive desktop route planner with no external libraries. */
public final class RoutePlannerWindow extends JFrame {
    private final CityGraph graph = DemoCity.build();
    private final RouteEngine engine = new RouteEngine(graph);
    private final MapPanel map = new MapPanel(graph);
    private final JComboBox<String> start;
    private final JComboBox<String> end;
    private final JComboBox<String> mode = new JComboBox<>(new String[]{
            "Shortest distance", "Lowest risk", "Distance within risk budget"});
    private final JSpinner budget = new JSpinner(new SpinnerNumberModel(8.0, 0.0, 100.0, 0.5));
    private final JLabel result = new JLabel("Select a route to begin.");

    public RoutePlannerWindow() {
        super("RouteLab — Dijkstra + Safety-Constrained Routing");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(850, 670));
        setSize(1060, 730);
        setLocationRelativeTo(null);
        List<String> names = new ArrayList<>();
        graph.locations().forEach(loc -> names.add(loc.name()));
        start = new JComboBox<>(names.toArray(String[]::new));
        end = new JComboBox<>(names.toArray(String[]::new));
        start.setSelectedItem("Downtown");
        end.setSelectedItem("University");

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBorder(BorderFactory.createEmptyBorder(16, 18, 12, 18));
        top.setBackground(Color.WHITE);
        JLabel title = new JLabel("RouteLab  /  explore routes");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setAlignmentX(LEFT_ALIGNMENT);
        top.add(title);
        top.add(Box.createVerticalStrut(12));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        controls.setBackground(Color.WHITE);
        controls.add(new JLabel("From"));
        controls.add(start);
        controls.add(new JLabel("To"));
        controls.add(end);
        controls.add(new JLabel("Optimize"));
        controls.add(mode);
        controls.add(new JLabel("Risk limit"));
        controls.add(budget);
        JButton go = new JButton("Find route");
        go.addActionListener(event -> updateRoute());
        controls.add(go);
        top.add(controls);
        add(top, BorderLayout.NORTH);

        JScrollPane view = new JScrollPane(map);
        map.setPreferredSize(new Dimension(900, 530));
        view.setBorder(BorderFactory.createEmptyBorder());
        add(view, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createEmptyBorder(13, 20, 16, 20));
        result.setFont(new Font("SansSerif", Font.PLAIN, 14));
        footer.add(result, BorderLayout.CENTER);
        JLabel disclaimer = new JLabel("Demo map only: distances and risk scores are fictional; not navigation or safety advice.");
        disclaimer.setFont(new Font("SansSerif", Font.PLAIN, 11));
        disclaimer.setForeground(new Color(100, 108, 119));
        footer.add(disclaimer, BorderLayout.SOUTH);
        add(footer, BorderLayout.SOUTH);

        mode.addActionListener(event -> budget.setEnabled(mode.getSelectedIndex() == 2));
        budget.setEnabled(false);
        SwingUtilities.invokeLater(this::updateRoute);
    }

    private void updateRoute() {
        String from = (String) start.getSelectedItem();
        String to = (String) end.getSelectedItem();
        var found = switch (mode.getSelectedIndex()) {
            case 1 -> engine.safest(from, to);
            case 2 -> engine.withinRiskBudget(from, to, ((Number) budget.getValue()).doubleValue());
            default -> engine.shortest(from, to);
        };
        map.setRoute(found.orElse(null));
        if (found.isPresent()) {
            Route route = found.get();
            result.setText(String.format("<html><b>%.2f km</b> · Risk %.2f · %s</html>",
                    route.distanceKm(), route.totalRisk(), String.join(" → ", route.stops())));
        } else {
            result.setText("No route exists that meets these conditions. Try a higher risk budget.");
        }
    }
}
