package com.insa.coliffimo.router;

import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivities;
import com.graphhopper.util.InstructionList;
import com.insa.coliffimo.leaflet.LatLong;

import java.util.ArrayList;

public class RouteInfo {

    public final ArrayList<VehicleRoute> routes;
    public final ArrayList<LatLong> fullTracks;
    public final ArrayList<InstructionList> instructionLists;
    public final ArrayList<TourActivities> tourActivities;

    public RouteInfo(ArrayList<VehicleRoute> routes, ArrayList<LatLong> fullTracks, ArrayList<InstructionList> instructionLists, ArrayList<TourActivities> tourActivities) {
        this.routes = routes;
        this.fullTracks = fullTracks;
        this.instructionLists = instructionLists;
        this.tourActivities = tourActivities;
    }
}
