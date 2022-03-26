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
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.Translation;
import com.insa.coliffimo.business.Intersection;
import com.insa.coliffimo.leaflet.LatLong;
import com.insa.coliffimo.leaflet.LeafletMapView;
import com.insa.coliffimo.leaflet.markers.DeliveryMarker;
import com.insa.coliffimo.leaflet.markers.DepotMarker;
import com.insa.coliffimo.leaflet.markers.PickupMarker;
import com.insa.coliffimo.utils.ColorGenerator;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class RouterRunnable implements Runnable {

    private final PlanningResource planningResource;
    private final LeafletMapView mapView;
    private final BorderPane rootPane;
    private final MapResource mapResource;

    private final HashMap<String, ResponsePath> bestPathsCache = new HashMap<>();

    public enum ShipmentType {
        PICKUP,
        DELIVERY
    }

    public RouterRunnable(PlanningResource planningResource, LeafletMapView mapView, BorderPane rootPane, MapResource mapResource) {
        this.planningResource = planningResource;
        this.mapView = mapView;
        this.rootPane = rootPane;
        this.mapResource = mapResource;
    }

    @Override
    public void run() {
        RouteInfo route = computeBestRoute();

        Platform.runLater(() -> {
            mapView.addTrack(route.getFullTracks());

            Vehicle vehicle = planningResource.getVehicle();
            ArrayList<Shipment> shipments = planningResource.getShipments();
            ArrayList<Color> colors = new ColorGenerator().generateColorList(shipments.size());
            HashMap<String, Pair<ShipmentType, Color>> locationIdShipmentTypeColorHashMap = new HashMap<>();
            System.out.println("on genere "+shipments.size()+" couleurs");

            mapView.addMarker(from(vehicle.getStartLocation()), "Start/Arrival", new DepotMarker("#000000"), 1, "start", "0");
            int k = 0;
            for (Shipment shipment : shipments) {
                String idMarker = shipment.getId();
                System.out.println("[getPickupLocation="+shipment.getPickupLocation().getId()+"]");
                System.out.println("[getDeliveryLocation="+shipment.getDeliveryLocation().getId()+"]");
                //hashmap id shipment color
                String hex = "#" + Integer.toHexString(colors.get(k).getRGB()).substring(2);
                locationIdShipmentTypeColorHashMap.put(shipment.getPickupLocation().getId(), new Pair<>(ShipmentType.PICKUP, colors.get(k)));
                locationIdShipmentTypeColorHashMap.put(shipment.getDeliveryLocation().getId(), new Pair<>(ShipmentType.DELIVERY, colors.get(k)));
                mapView.addMarker(from(shipment.getPickupLocation()), "Pickup", new PickupMarker(hex), 1, "pickup", "pickup" + idMarker);
                mapView.addMarker(from(shipment.getDeliveryLocation()), "Delivery", new DeliveryMarker(hex), 1, "delivery", "delivery" + idMarker);
                k++;
            }

            Translation tr = RhoneAlpesGraphHopper.getGraphHopper().getTranslationMap().getWithFallBack(Locale.FRANCE);
            VBox rightPane = new VBox();
            Button seePathDetailButton = null;
            Label arrivalLabel = null;
            rightPane.getStyleClass().add("vbox");
            int i = 1;
            int instructionBlocPaneIndex = 0;
            VBox instructionBlocPane = new VBox();
            instructionBlocPane.getStyleClass().add("instruction-bloc");

            int finalInstructionBlocPaneIndex = instructionBlocPaneIndex;
            seePathDetailButton = new Button("Détails de l'itinéraire");
            seePathDetailButton.getStyleClass().add("see-detail-button");
            seePathDetailButton.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, new EventHandler<javafx.scene.input.MouseEvent>() {
                @Override
                public void handle(javafx.scene.input.MouseEvent e) {
                    Set<Node> nodes = rightPane.lookupAll(".instruction-line-in-bloc" + finalInstructionBlocPaneIndex);
                    nodes.forEach(n -> {
                        n.setVisible(!n.isVisible());
                        n.setManaged(!n.isManaged());
                    });
                    System.out.println(rightPane.getWidth());
                }
            });

            Circle pointCircle = new Circle();
            pointCircle.setTranslateY(4);
            pointCircle.setRadius(6);
            Label departureLabel = new Label("Départ");
            departureLabel.getStyleClass().add("departure-label");
            pointCircle.setFill(javafx.scene.paint.Color.rgb(0,0,0));
            HBox circleAndDeparture = new HBox(pointCircle, departureLabel);
            rightPane.getChildren().add(circleAndDeparture);
            rightPane.getChildren().add(seePathDetailButton);

            for (Instruction iti : route.getFullInstructions()) {
                HBox instructionLine = new HBox();
                instructionLine.setVisible(false);
                instructionLine.setManaged(false);
                instructionLine.getStyleClass().add("instruction-line-in-bloc");
                instructionLine.getStyleClass().add("instruction-line-in-bloc" + instructionBlocPaneIndex);

                String indication = StringUtils.uncapitalize(iti.getTurnDescription(tr));
                if (!indication.startsWith("arrivée")) {
                    int distance = (int) iti.getDistance();

                    if (indication.startsWith("continuez") && distance > 0) {
                        indication = indication + " pendant " + distance + " mètres";
                    } else if (!indication.startsWith("arrivée") && distance > 0) {
                        indication = indication + " et continuez sur " + distance + " mètres";
                    }

                    String imageFileName = "";
                    switch (iti.getSign()) {
                        case Instruction.CONTINUE_ON_STREET -> imageFileName = "arrow-up";
                        case Instruction.TURN_LEFT, Instruction.KEEP_LEFT, Instruction.TURN_SLIGHT_LEFT, Instruction.TURN_SHARP_LEFT -> imageFileName = "arrow-left";
                        case Instruction.TURN_RIGHT, Instruction.KEEP_RIGHT, Instruction.TURN_SLIGHT_RIGHT, Instruction.TURN_SHARP_RIGHT -> imageFileName = "arrow-right";
                        default -> imageFileName = "";
                    }
                    if (!imageFileName.isEmpty()) {
                        Image image = null;
                        try {
                            image = new Image(new FileInputStream(
                                    Paths.get(
                                            "src", "main", "resources", "img").toAbsolutePath() + "/" + imageFileName + ".png"));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        ImageView imageView = new ImageView(image);
                        imageView.setFitHeight(18);
                        imageView.setFitWidth(18);
                        imageView.setPreserveRatio(true);

                        instructionLine.getChildren().add(imageView);
                    }
                    instructionLine.getChildren().add(new Label(StringUtils.capitalize(indication)));
                    instructionBlocPane.getChildren().add(instructionLine);
                } else {
                    //trouver le shipment le + proche, et récup l'id de ce shipment
                    //aller chercher dans la hashmap la color correspondante à cet id
                    String closestShipmentId = findClosestShipmentId(iti.getPoints().get(0).getLat(), iti.getPoints().get(0).getLon());
                    String shipmentType = "";

                    rightPane.getChildren().add(instructionBlocPane);
                    instructionBlocPane = new VBox();
                    instructionBlocPane.getStyleClass().add("instruction-bloc");
                    seePathDetailButton = new Button("Détails de l'itinéraire");
                    seePathDetailButton.getStyleClass().add("see-detail-button");
                    Pair<ShipmentType, Color> locationIdShipmentTypeColor = locationIdShipmentTypeColorHashMap.get(closestShipmentId);
                    pointCircle = new Circle();
                    pointCircle.setTranslateY(4);
                    pointCircle.setRadius(6);
                    pointCircle.setFill(javafx.scene.paint.Color.rgb(
                            locationIdShipmentTypeColor.getValue().getRed(),
                            locationIdShipmentTypeColor.getValue().getGreen(),
                            locationIdShipmentTypeColor.getValue().getBlue()));
                    shipmentType = locationIdShipmentTypeColor.getKey() == ShipmentType.PICKUP ? " de retrait" : " de livraison";

                    arrivalLabel = new Label("Arrivée au point" + shipmentType + " (" + iti.getPoints().getLat(0) + ";" + iti.getPoints().getLon(0) + ")");
                    arrivalLabel.getStyleClass().add("arrival-label");
                    HBox circleAndArrival = new HBox(pointCircle, arrivalLabel);

                    instructionBlocPaneIndex++;
                    int finalInstructionBlocPaneIndex1 = instructionBlocPaneIndex;
                    seePathDetailButton.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, new EventHandler<javafx.scene.input.MouseEvent>() {
                        @Override
                        public void handle(javafx.scene.input.MouseEvent e) {
                            Set<Node> nodes = rightPane.lookupAll(".instruction-line-in-bloc" + finalInstructionBlocPaneIndex1);
                            nodes.forEach(n -> {
                                n.setVisible(!n.isVisible());
                                n.setManaged(!n.isManaged());
                            });
                        }
                    });
                    instructionBlocPane.getChildren().add(circleAndArrival);
                    instructionBlocPane.getChildren().add(seePathDetailButton);
                }
            }

            Label finalArrivalLabel = new Label("Arrivée");
            departureLabel.getStyleClass().add("final-arrival-label");
            pointCircle = new Circle();
            pointCircle.setTranslateY(4);
            pointCircle.setRadius(6);
            pointCircle.setFill(javafx.scene.paint.Color.rgb(0,0,0));
            HBox circleAndFinalArrival = new HBox(pointCircle, finalArrivalLabel);
            rightPane.getChildren().add(circleAndFinalArrival);

            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(rightPane);
            rootPane.setRight(scrollPane);
        });
    }

    /**
     * Find the closest shipment from the given latitude and longitude point
     *
     * @param lat : the latitude of the point to find a shipment
     * @param longi : the longitude of the point to find a shipment
     * @return id of the closest shipment
     */
    private String findClosestShipmentId(double lat, double longi){
        String closestShipmentId = null;
        double closestDistance = 100000;
        for (Shipment s : planningResource.getShipments()) {
            double distancePickupShipment = Math.pow(lat-s.getPickupLocation().getCoordinate().getX(),2.0) + Math.pow(longi-s.getPickupLocation().getCoordinate().getY(),2.0);
            double distanceDeliveryShipment = Math.pow(lat-s.getDeliveryLocation().getCoordinate().getX(),2.0) + Math.pow(longi-s.getDeliveryLocation().getCoordinate().getY(),2.0);
            if(distancePickupShipment<closestDistance) {
                closestShipmentId = s.getPickupLocation().getId();
                closestDistance = distancePickupShipment;
            } else if(distanceDeliveryShipment<closestDistance) {
                closestShipmentId = s.getDeliveryLocation().getId();
                closestDistance = distanceDeliveryShipment;
            }
        }

        return closestShipmentId;
    }

    private RouteInfo computeBestRoute() {
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

    private RouteInfo computeFullRoute(VehicleRoutingProblemSolution bestSolution) {
        ArrayList<Location> tracks = new ArrayList<>();
        bestSolution.getRoutes().forEach(vehicleRoute -> {
            tracks.add(vehicleRoute.getStart().getLocation());
            vehicleRoute.getActivities().forEach(tourActivity -> {
                tracks.add(tourActivity.getLocation());
            });
            tracks.add(vehicleRoute.getEnd().getLocation());
        });

        RouteInfo itinerary = new RouteInfo();
        for (int i = 0; i < tracks.size() - 1; i++) {
            Coordinate from = tracks.get(i).getCoordinate();
            Coordinate to = tracks.get(i + 1).getCoordinate();
            ResponsePath path = bestPathsCache.get(from.toString() + to);
            path.getPoints().forEach(p -> itinerary.getFullTracks().add(new LatLong(p.lat, p.lon)));
            path.getInstructions().forEach(iti -> itinerary.getFullInstructions().add(iti));
        }
        return itinerary;
    }

    private LatLong from(Location location) {
        return new LatLong(location.getCoordinate().getX(), location.getCoordinate().getY());
    }
}
