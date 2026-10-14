package com.qais.routeplanner;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Swing map renderer for roads, neighborhoods, and highlighted route stops.
 * Drawing stays separate from the routing calculations performed by RouteEngine.
 */
public final class MapPanel extends JPanel {
    private static final Color BG = new Color(247, 249, 252);
    private static final Color ROAD = new Color(194, 203, 214);
    private static final Color HIGHLIGHT = new Color(35, 100, 205);
    private static final Color LABEL = new Color(40, 51, 67);
    private static final Color PIN = new Color(50, 72, 96);
    private final CityGraph graph;
    private Route route;

    public MapPanel(CityGraph graph) {
        this.graph = graph;
        setBackground(BG);
    }

    public void setRoute(Route next) {
        route = next;
        repaint();
    }

    private String roadKey(String a, String b) {
        return a.compareTo(b) < 0 ? a + "|" + b : b + "|" + a;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D canvas = (Graphics2D) g.create();
        try {
            canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double scale = Math.min((getWidth() - 40) / 850.0, (getHeight() - 50) / 530.0);
            if (scale <= 0) return;
            canvas.translate((getWidth() - 850 * scale) / 2, (getHeight() - 530 * scale) / 2);
            canvas.scale(scale, scale);

            Set<String> selectedRoads = new HashSet<>();
            if (route != null) {
                List<String> stops = route.stops();
                for (int i = 0; i + 1 < stops.size(); i++) {
                    selectedRoads.add(roadKey(stops.get(i), stops.get(i + 1)));
                }
            }

            canvas.setStroke(new BasicStroke(9, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            canvas.setColor(ROAD);
            for (Road road : graph.roads()) {
                Location a = graph.location(road.from());
                Location b = graph.location(road.to());
                canvas.drawLine(a.x(), a.y(), b.x(), b.y());
            }

            canvas.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            canvas.setColor(HIGHLIGHT);
            for (Road road : graph.roads()) {
                if (selectedRoads.contains(roadKey(road.from(), road.to()))) {
                    Location a = graph.location(road.from());
                    Location b = graph.location(road.to());
                    canvas.drawLine(a.x(), a.y(), b.x(), b.y());
                }
            }

            canvas.setFont(new Font("SansSerif", Font.BOLD, 14));
            FontMetrics metrics = canvas.getFontMetrics();
            for (Location loc : graph.locations()) {
                boolean isSelected = route != null && route.stops().contains(loc.name());
                canvas.setColor(isSelected ? HIGHLIGHT : PIN);
                canvas.fillOval(loc.x() - 9, loc.y() - 9, 18, 18);
                canvas.setColor(Color.WHITE);
                canvas.fillOval(loc.x() - 3, loc.y() - 3, 6, 6);
                int tw = metrics.stringWidth(loc.name());
                int tx = loc.x() - tw / 2;
                int ty = loc.y() - 19;
                canvas.setColor(BG);
                canvas.fillRoundRect(tx - 5, ty - 15, tw + 10, 19, 8, 8);
                canvas.setColor(LABEL);
                canvas.drawString(loc.name(), tx, ty);
            }
        } finally {
            canvas.dispose();
        }
    }
}
