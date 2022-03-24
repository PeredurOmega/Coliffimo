package com.insa.coliffimo.router;

import com.graphhopper.util.Instruction;
import com.insa.coliffimo.leaflet.LatLong;

import java.util.ArrayList;

public class RouteInfo {

    private final ArrayList<LatLong> fullTracks;
    private final ArrayList<Instruction> fullInstructions;

    public RouteInfo(){
        fullTracks = new ArrayList<>();
        fullInstructions = new ArrayList<>();
    }

    public ArrayList<LatLong> getFullTracks() {
        return fullTracks;
    }

    public ArrayList<Instruction> getFullInstructions() {
        return fullInstructions;
    }
}
