package com.qais.routeplanner;

/** A made-up 9-neighborhood map for learning and demonstrations. Not live map data. */
public final class DemoCity {
    private DemoCity() { }

    public static CityGraph build() {
        CityGraph graph = new CityGraph();
        graph.addLocation("Downtown", 95, 220)
             .addLocation("Old Port", 200, 390)
             .addLocation("Museum", 270, 120)
             .addLocation("Central Station", 395, 250)
             .addLocation("Park", 445, 75)
             .addLocation("University", 625, 165)
             .addLocation("Market", 535, 385)
             .addLocation("Hospital", 735, 335)
             .addLocation("Riverside", 325, 460);

        graph.addRoad("Downtown", "Old Port", 1.6, 1)
             .addRoad("Downtown", "Museum", 2.0, 2)
             .addRoad("Downtown", "Central Station", 2.4, 9)
             .addRoad("Old Port", "Central Station", 2.7, 2)
             .addRoad("Old Port", "Riverside", 1.4, 1)
             .addRoad("Riverside", "Market", 2.1, 1)
             .addRoad("Museum", "Park", 1.8, 1)
             .addRoad("Museum", "Central Station", 1.6, 4)
             .addRoad("Park", "University", 2.0, 2)
             .addRoad("Park", "Central Station", 1.7, 3)
             .addRoad("Central Station", "University", 2.1, 7)
             .addRoad("Central Station", "Market", 1.7, 4)
             .addRoad("Market", "University", 2.4, 2)
             .addRoad("Market", "Hospital", 2.0, 2)
             .addRoad("University", "Hospital", 1.8, 3);
        return graph;
    }
}
