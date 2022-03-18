package com.insa.coliffimo;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.Profile;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.termination.IterationWithoutImprovementTermination;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import com.insa.coliffimo.leaflet.*;
import com.insa.coliffimo.leaflet.markers.DeliveryMarker;
import com.insa.coliffimo.leaflet.markers.DepotMarker;
import com.insa.coliffimo.leaflet.markers.PickupMarker;
import com.insa.coliffimo.metier.Intersection;
import com.insa.coliffimo.metier.Map;
import com.insa.coliffimo.metier.PlanningRequest;
import com.insa.coliffimo.metier.Request;
import com.insa.coliffimo.utils.XmlParser;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class HelloController implements Initializable {

    private static final String XML_MAP_RESOURCE_DIRECTORY_PATH = Paths.get("src", "main", "resources", "map").toAbsolutePath() + "/";
    private static final String XML_PLANNING_REQUEST_RESOURCE_DIRECTORY_PATH = Paths.get("src", "main", "resources", "planningRequest").toAbsolutePath() + "/";
    //private static final Logger logger = LoggerFactory.getLogger(Controller.)
    @FXML
    private Label welcomeText;

    /**
     * the MapView containing the map
     */
    @FXML
    private LeafletMapView mapView;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    private GraphHopper hopper = new GraphHopper();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        MapConfig initialMap = new MapConfig(Arrays.asList(MapLayer.values()),
                new ZoomControlConfig(true, ControlPosition.BOTTOM_LEFT),
                new ScaleControlConfig(true, ControlPosition.BOTTOM_LEFT, true));
        CompletableFuture<Worker.State> cfMapLoadState = mapView.displayMap(initialMap);

        // display Berlin initially after map has been loaded
        cfMapLoadState.whenComplete((workerState, error) -> {
            if (workerState == Worker.State.SUCCEEDED) {
                mapView.setView(initialMap.getInitialCenter(), initialMap.getInitialZoom());
                loadRequest("mediumMap.xml", "requestsMedium5.xml");
            }
        });


        /*
        positionTooltip.setAutoHide(true)

        slPosition.valueProperty().addListener { _, oldValue, newValue ->
            if (oldValue.toInt() != newValue.toInt()) {
                movePositionMarker()
            }
        }*/
    }

    private void traceRoute(Vehicle vehicle, ArrayList<Shipment> shipments) {
        VehicleRoutingTransportCostsMatrix costMatrixBuilder = computeCostMatrix(vehicle, shipments);
        // Calcule le meilleur itinéraire en se servant de la matrice de coûts costMatrixBuilder
        VehicleRoutingProblem problem = VehicleRoutingProblem.Builder.newInstance()
                .setRoutingCost(costMatrixBuilder)
                .addVehicle(vehicle)
                .addAllJobs(shipments)
                .build();
        System.out.println("costMatrixBuilder");

        // define an algorithm out of the box - this creates a large neighborhood search algorithm
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);
        algorithm.setPrematureAlgorithmTermination(new IterationWithoutImprovementTermination(100));
        // search solutions
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();

        // get best
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);
        solutions.forEach((solution) -> {
            System.out.println("SOLUTION" + solution.getCost());
        });
        ArrayList<LatLong> tracks = new ArrayList<>();
        bestSolution.getRoutes().forEach(vehicleRoute -> {
            tracks.add(from(vehicleRoute.getStart().getLocation()));
            vehicleRoute.getActivities().forEach(tourActivity -> {
                tracks.add(from(tourActivity.getLocation()));
            });
            tracks.add(from(vehicleRoute.getEnd().getLocation()));
        });

        ArrayList<LatLong> fullTracks = new ArrayList<>();
        for (int i = 0; i < tracks.size() - 1; i++) {
            GHRequest req = new GHRequest(tracks.get(i).getLatitude(), tracks.get(i).getLongitude(), tracks.get(i + 1).getLatitude(), tracks.get(i + 1).getLongitude())
                    // note that we have to specify which profile we are using even when there is only one like here
                    .setProfile("car");
            //GHResponse : stocke les différents chemins possibles pour une GHRequest, et renvoie le meilleur avec getBest()
            GHResponse rsp = hopper.route(req);
            // handle errors
            if (rsp.hasErrors()) {
                //System.out.println("point i : "+ locations.get(i) + " et j : "+ locations.get(j));
                throw new RuntimeException(rsp.getErrors().toString());
            }
            // use the best path, see the GHResponse class for more possibilities.
            ResponsePath path = rsp.getBest();
            path.getPoints().forEach(p -> {
                fullTracks.add(new LatLong(p.lat, p.lon));
            });
        }

        mapView.addTrack(fullTracks);
        mapView.addMarker(from(vehicle.getStartLocation()), "Start/Arrival", new DepotMarker("#660000"), 1);
        shipments.forEach(shipment -> {
            mapView.addMarker(from(shipment.getPickupLocation()), "Pickup", new PickupMarker("#66FF00"), 1);
            mapView.addMarker(from(shipment.getDeliveryLocation()), "Delivery", new DeliveryMarker("#000066"), 1);
        });
        SolutionPrinter.print(bestSolution);
    }

    private VehicleRoutingTransportCostsMatrix computeCostMatrix(Vehicle vehicle, ArrayList<Shipment> shipments) {
        ArrayList<Location> locations = new ArrayList<>();
        shipments.forEach(shipment -> {
            locations.add(shipment.getPickupLocation());
            locations.add(shipment.getDeliveryLocation());
        });
        locations.add(vehicle.getStartLocation());
        locations.add(vehicle.getEndLocation());

        hopper.setOSMFile("./resources/rhone-alpes-latest.osm.pbf");
        hopper.setGraphHopperLocation("./resources/routing-graph-cache");
        // see docs/core/profiles.md to learn more about profiles
        hopper.setProfiles(new Profile("car"));
        hopper.importOrLoad();

        VehicleRoutingTransportCostsMatrix.Builder costMatrixBuilder =
                VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);

        for (int i = 0; i < locations.size(); i++) {
            for (int j = 0; j < locations.size(); j++) {
                // Chaque case de la matrice est une distance/temps entre deux endroits (i->j), pas forcément des endroits d'une même livraison
                // GHRequest(double fromLat, double fromLon, double toLat, double toLon)
                Coordinate from = locations.get(i).getCoordinate();
                Coordinate to = locations.get(j).getCoordinate();
                GHRequest req = new GHRequest(from.getX(), from.getY(), to.getX(), to.getY())
                        // note that we have to specify which profile we are using even when there is only one like here
                        .setProfile("car");
                //GHResponse : stocke les différents chemins possibles pour une GHRequest, et renvoie le meilleur avec getBest()
                GHResponse rsp = hopper.route(req);
                // handle errors
                if (rsp.hasErrors()) {
                    //System.out.println("point i : "+ locations.get(i) + " et j : "+ locations.get(j));
                    throw new RuntimeException(rsp.getErrors().toString());
                }
                // use the best path, see the GHResponse class for more possibilities.
                ResponsePath path = rsp.getBest();
                System.out.println("point i : " + locations.get(i) + " et j : " + locations.get(j));
                System.out.println(rsp.toString());

                // points, distance in meters and time in millis of the full path
                //PointList pointList = path.getPoints();
                double distance = path.getDistance();
                long timeInMs = path.getTime();


                //On met ces valeurs dans la matrice de coûts
                System.out.println("DISTANCE FROM " + locations.get(i).getCoordinate());
                System.out.println("DISTANCE TO " + locations.get(j).getCoordinate());
                System.out.println("DISTANCE TEST " + timeInMs + " " + distance);
                /*costMatrixBuilder.addTransportDistance(Integer.toString(i), Integer.toString(j), distance);
                costMatrixBuilder.addTransportTime(Integer.toString(i), Integer.toString(j), timeInMs);*/
                costMatrixBuilder.addTransportDistance(from.toString(), to.toString(), distance);
                costMatrixBuilder.addTransportTime(from.toString(), to.toString(), timeInMs);
            }
        }
        return costMatrixBuilder.build();
    }

    private LatLong from(Location location) {
        return new LatLong(location.getCoordinate().getX(), location.getCoordinate().getY());
    }

    public void loadRequest(String xmlMapResourceFileName, String xmlPlanningRequestResourceFileName) {
        mapView.clearMarkersAndTracks();

        XmlParser xmlParser = new XmlParser();

        String xmlMapResourceFilePath = XML_MAP_RESOURCE_DIRECTORY_PATH + xmlMapResourceFileName;
        Map map = xmlParser.ConvertXmlToMap(new File(xmlMapResourceFilePath));

        String xmlPlanningRequestResourceFilePath = XML_PLANNING_REQUEST_RESOURCE_DIRECTORY_PATH + xmlPlanningRequestResourceFileName;
        PlanningRequest planningRequest = xmlParser.ConvertXmlToPlanningRequest(new File(xmlPlanningRequestResourceFilePath));

        HashMap<Long, Intersection> intersections = map.getListIntersections();
        ArrayList<Request> listRequests = planningRequest.getListRequests();
        Intersection depotCoord = intersections.get(planningRequest.getDepotAddress());
        Location depotLocation = Location.newInstance(depotCoord.getLatitude(), depotCoord.getLongitude());

        VehicleTypeImpl vehicleType = VehicleTypeImpl.Builder.newInstance("carType")
                .setCostPerDistance(0)
                .setCostPerTransportTime(1)
                .build();

        VehicleImpl vehicle1 = VehicleImpl.Builder.newInstance("vehicle1Id")
                .setStartLocation(depotLocation)
                .setType(vehicleType)
                .setEndLocation(depotLocation)
                .build();

        ArrayList<Shipment> shipments = new ArrayList<>();

        listRequests.forEach((request) -> {
            Intersection pickupCoord = intersections.get(request.getPickupAddress());
            Intersection deliveryCoord = intersections.get(request.getDeliveryAddress());

            Shipment shipment = Shipment.Builder.newInstance(UUID.randomUUID().toString())
                    .setPickupLocation(Location.newInstance(pickupCoord.getLatitude(), pickupCoord.getLongitude()))
                    .setDeliveryLocation(Location.newInstance(deliveryCoord.getLatitude(), deliveryCoord.getLongitude()))
                    .setDeliveryServiceTime(request.getDeliveryDuration() * 1000)
                    .setPickupServiceTime(request.getDeliveryDuration() * 1000)
                    .build();

            shipments.add(shipment);
            /*mapView.addMarker(new LatLong(pickupCoord.getLatitude(), pickupCoord.getLongitude()),
                    "pickup"+idx, ColorMarker.GREY_MARKER, 0);
            mapView.addMarker(new LatLong(deliveryCoord.getLatitude(), deliveryCoord.getLongitude()),
                    "delivery"+idx, ColorMarker.GREY_MARKER, 0);*/
        });
        traceRoute(vehicle1, shipments);
    }
}