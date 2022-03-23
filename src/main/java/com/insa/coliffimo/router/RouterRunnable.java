package com.insa.coliffimo.router;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.termination.IterationWithoutImprovementTermination;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import com.insa.coliffimo.leaflet.LatLong;
import com.insa.coliffimo.leaflet.LeafletMapView;
import com.insa.coliffimo.leaflet.markers.DeliveryMarker;
import com.insa.coliffimo.leaflet.markers.DepotMarker;
import com.insa.coliffimo.leaflet.markers.PickupMarker;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class RouterRunnable implements Runnable {

    private final PlanningResource planningResource;
    private final LeafletMapView mapView;

    private final HashMap<String, ResponsePath> bestPathsCache = new HashMap<>();

    public RouterRunnable(PlanningResource planningResource, LeafletMapView mapView) {
        this.planningResource = planningResource;
        this.mapView = mapView;
    }

    @Override
    public void run() {
        ArrayList<LatLong> route = computeBestRoute();

        Platform.runLater(() -> {
            mapView.addTrack(route);

            Vehicle vehicle = planningResource.getVehicle();
            ArrayList<Shipment> shipments = planningResource.getShipments();
            //TODO IMPROVE AND USE CUSTOM COLOR
            mapView.addMarker(from(vehicle.getStartLocation()), "Start/Arrival", new DepotMarker("#660000"), 1);
            shipments.forEach(shipment -> {
                mapView.addMarker(from(shipment.getPickupLocation()), "Pickup", new PickupMarker("#66FF00"), 1);
                mapView.addMarker(from(shipment.getDeliveryLocation()), "Delivery", new DeliveryMarker("#000066"), 1);
            });
        });
    }

    private ArrayList<LatLong> computeBestRoute() {
        Vehicle vehicle = planningResource.getVehicle();
        ArrayList<Shipment> shipments = planningResource.getShipments();

        VehicleRoutingTransportCostsMatrix costMatrix = computeCostMatrix(vehicle, shipments);
        // Define the problem using the cost matrix, the vehicle and the shipments
        VehicleRoutingProblem problem = VehicleRoutingProblem.Builder.newInstance()
                .setRoutingCost(costMatrix)
                .addVehicle(vehicle)
                .addAllJobs(shipments)
                .build();

        // Define an algorithm out of the box - This creates a large neighborhood search algorithm
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);
        algorithm.setPrematureAlgorithmTermination(new IterationWithoutImprovementTermination(100));

        // Search solutions
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();

        // Get the best solution
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);

        // Compute all the points of the track from the best solution
        return computeFullRoute(bestSolution);
    }

    private VehicleRoutingTransportCostsMatrix computeCostMatrix(Vehicle vehicle, ArrayList<Shipment> shipments) {
        ArrayList<Location> locations = getAllLocations(vehicle, shipments);

        GraphHopper graphHopper = RhoneAlpesGraphHopper.getGraphHopper();

        VehicleRoutingTransportCostsMatrix.Builder costMatrixBuilder =
                VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);

        // We build the matrix containing distanceInMeters and timeInMs between two points (i->j), not necessarily
        // the points of the same delivery
        for (int i = 0; i < locations.size(); i++) {
            for (Location location : locations) {
                Coordinate from = locations.get(i).getCoordinate();
                Coordinate to = location.getCoordinate();
                GHRequest req = new GHRequest(from.getX(), from.getY(), to.getX(), to.getY())
                        // Note that we have to specify which profile we are using even when there is only one like here
                        .setProfile("car");
                // GHResponse : store possible routes and returns the best with getBest()
                GHResponse rsp = graphHopper.route(req);

                // Handle errors
                if (rsp.hasErrors()) {
                    throw new RuntimeException(rsp.getErrors().toString());
                }

                // We get the best possible path according to GraphHopper
                ResponsePath path = rsp.getBest();

                // We put this path in the cache
                bestPathsCache.put(from.toString() + to, path);

                // We get the distance of the path in meters
                double distanceInMeters = path.getDistance();

                // We get the time to follow the path with the vehicle in milliseconds
                long timeInMs = path.getTime();

                // We add time and distanceInMs to the cost matrix
                costMatrixBuilder.addTransportDistance(from.toString(), to.toString(), distanceInMeters);
                costMatrixBuilder.addTransportTime(from.toString(), to.toString(), timeInMs);
            }
        }
        return costMatrixBuilder.build();
    }

    private ArrayList<Location> getAllLocations(Vehicle vehicle, ArrayList<Shipment> shipments) {
        ArrayList<Location> locations = new ArrayList<>();
        shipments.forEach(shipment -> {
            locations.add(shipment.getPickupLocation());
            locations.add(shipment.getDeliveryLocation());
        });
        locations.add(vehicle.getStartLocation());
        locations.add(vehicle.getEndLocation());
        return locations;
    }

    private ArrayList<LatLong> computeFullRoute(VehicleRoutingProblemSolution bestSolution) {
        ArrayList<Location> tracks = new ArrayList<>();
        bestSolution.getRoutes().forEach(vehicleRoute -> {
            tracks.add(vehicleRoute.getStart().getLocation());
            vehicleRoute.getActivities().forEach(tourActivity -> tracks.add(tourActivity.getLocation()));
            tracks.add(vehicleRoute.getEnd().getLocation());
        });

        ArrayList<LatLong> fullTracks = new ArrayList<>();
        for (int i = 0; i < tracks.size() - 1; i++) {
            Coordinate from = tracks.get(i).getCoordinate();
            Coordinate to = tracks.get(i + 1).getCoordinate();
            bestPathsCache.get(from.toString() + to).getPoints().forEach(p -> fullTracks.add(new LatLong(p.lat, p.lon)));
        }
        return fullTracks;
    }

    private LatLong from(Location location) {
        return new LatLong(location.getCoordinate().getX(), location.getCoordinate().getY());
    }
}
