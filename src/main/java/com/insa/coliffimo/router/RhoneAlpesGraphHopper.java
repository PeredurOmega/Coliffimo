package com.insa.coliffimo.router;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;

public class RhoneAlpesGraphHopper {
    private static GraphHopper graphHopper;

    /**
     * Lazy load of graph hopper with rhone alpes import.
     *
     * @return Graph hopper instance for rhone alpes (using rhone alpes cache).
     */
    public static GraphHopper getGraphHopper() {
        if (graphHopper == null) {
            graphHopper = new GraphHopper();
            graphHopper.setOSMFile("./resources/rhone-alpes-latest.osm.pbf");
            graphHopper.setGraphHopperLocation("./resources/routing-graph-cache");
            // see docs/core/profiles.md to learn more about profiles
            graphHopper.setProfiles(new Profile("car"));
            graphHopper.importOrLoad();
        }
        return graphHopper;
    }
}
