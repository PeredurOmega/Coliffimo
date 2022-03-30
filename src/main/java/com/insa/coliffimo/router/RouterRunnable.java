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
import com.graphhopper.util.Instruction;
import com.graphhopper.util.Translation;
import com.insa.coliffimo.leaflet.LatLong;
import com.insa.coliffimo.leaflet.LeafletMapView;
import com.insa.coliffimo.leaflet.markers.DeliveryMarker;
import com.insa.coliffimo.leaflet.markers.DepotMarker;
import com.insa.coliffimo.leaflet.markers.PickupMarker;
import com.insa.coliffimo.utils.JsonParser;
import com.insa.coliffimo.utils.ColorGenerator;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.*;

public class RouterRunnable implements Runnable {

    private final PlanningResource planningResource;
    private final ArrayList<Shipment> localShipments;
    private final LeafletMapView mapView;
    private final BorderPane rootPane;
    private VBox rightPane = new VBox();
    private VBox instructionBlocPane = null;
    HashMap<String, javafx.scene.paint.Color> locationIdMappedWithColor = new HashMap<>();
    HashMap<String, ShipmentType> locationIdMappedWithShipmentType = new HashMap<>();
    ScrollPane scrollPane = new ScrollPane();
    MFXButton collapseRightPanelButton;

    private final HashMap<String, ResponsePath> bestPathsCache = new HashMap<>();

    public enum ShipmentType {
        PICKUP,
        DELIVERY
    }

    public RouterRunnable(PlanningResource planningResource, LeafletMapView mapView, BorderPane rootPane, MFXButton collapseRightPanelButton, ArrayList<Shipment> localShipments) {
        this.planningResource = planningResource;
        this.mapView = mapView;
        this.rootPane = rootPane;
        this.collapseRightPanelButton = collapseRightPanelButton;
        this.localShipments = localShipments;
    }

    @Override
    public void run() {
        RouteInfo route = computeBestRoute();
        JsonParser.sauvegarder(route, "sauvegarde.JSON", this.planningResource.getPlanningRequest().getDepotDepartureLocalTime());
        Platform.runLater(() -> {
            Translation tr = RhoneAlpesGraphHopper.getGraphHopper().getTranslationMap().getWithFallBack(Locale.FRANCE);
            int instructionBlocPaneIndex = 0;

            initMapView(route);
            initRightPane();
            initInstructionBlocPane();
            HBox departureLine = getDepartureLine();
            instructionBlocPane.getChildren().add(departureLine);
            rightPane.getChildren().add(instructionBlocPane);

            initInstructionBlocPane();
            Button seePathDetailButton = getSeePathDetailButton(instructionBlocPaneIndex);
            instructionBlocPane.getChildren().add(seePathDetailButton);
            rightPane.getChildren().add(instructionBlocPane);

            initInstructionBlocPane();

            for (Instruction iti : route.getFullInstructions()) {
                String indication = StringUtils.uncapitalize(iti.getTurnDescription(tr));
                if (!indication.startsWith("arrivée")) {
                    HBox pathDetailLine = getPathDetailLine(indication, iti, instructionBlocPaneIndex);
                    instructionBlocPane.getChildren().add(pathDetailLine);
                } else {
                    // Find the closest shipment to get matching color and shipment type from hashmaps
                    String closestShipmentId = findClosestShipmentId(iti.getPoints().get(0).getLat(), iti.getPoints().get(0).getLon());

                    rightPane.getChildren().add(instructionBlocPane);
                    initInstructionBlocPane();
                    instructionBlocPaneIndex++;

                    HBox arrivalLine = getArrivalLine(closestShipmentId, iti);
                    instructionBlocPane.getChildren().add(arrivalLine);
                    seePathDetailButton = getSeePathDetailButton(instructionBlocPaneIndex);
                    instructionBlocPane.getChildren().add(seePathDetailButton);
                }
            }

            initInstructionBlocPane();
            HBox finalArrivalLine = getFinalArrivalLine();
            instructionBlocPane.getChildren().add(finalArrivalLine);
            rightPane.getChildren().add(instructionBlocPane);

            scrollPane = new ScrollPane();
            scrollPane.setContent(rightPane);
            rootPane.setRight(scrollPane);
        });
    }

    private void initMapView(RouteInfo route) {
        mapView.clearMarkersAndTracks();
        mapView.addTrack(route.getFullTracks());

        Vehicle vehicle = planningResource.getVehicle();
        ArrayList<Shipment> shipments = getAllShipments();
        ArrayList<Color> colors = new ColorGenerator().generateColorList(shipments.size());

        mapView.addMarker(from(vehicle.getStartLocation()), "Start/Arrival", new DepotMarker("#000000"), 1, "start", "0");
        int k = 0;
        for (Shipment shipment : shipments) {
            String idMarker = shipment.getId();
            String hex = "#" + Integer.toHexString(colors.get(k).getRGB()).substring(2);
            javafx.scene.paint.Color color = javafx.scene.paint.Color.rgb(colors.get(k).getRed(), colors.get(k).getGreen(), colors.get(k).getBlue());
            locationIdMappedWithColor.put(shipment.getPickupLocation().getId(), color);
            locationIdMappedWithColor.put(shipment.getDeliveryLocation().getId(), color);
            locationIdMappedWithShipmentType.put(shipment.getPickupLocation().getId(), ShipmentType.PICKUP);
            locationIdMappedWithShipmentType.put(shipment.getDeliveryLocation().getId(), ShipmentType.DELIVERY);
            mapView.addMarker(from(shipment.getPickupLocation()), "Pickup", new PickupMarker(hex), 1, "pickup", "pickup" + idMarker);
            mapView.addMarker(from(shipment.getDeliveryLocation()), "Delivery", new DeliveryMarker(hex), 1, "delivery", "delivery" + idMarker);
            k++;
        }
    }

    private void initRightPane() {
        rightPane = new VBox();
        rightPane.getStyleClass().add("vbox");
        this.collapseRightPanelButton.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, collapseRightPanel());
    }

    private void initInstructionBlocPane() {
        instructionBlocPane = new VBox();
        instructionBlocPane.getStyleClass().add("instruction-bloc");
    }

    private Button getSeePathDetailButton(int finalInstructionBlocPaneIndex) {
        Button seePathDetailButton = new Button("Détails de l'itinéraire");
        seePathDetailButton.getStyleClass().add("see-detail-button");
        seePathDetailButton.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, collapsePathDetail(finalInstructionBlocPaneIndex));

        return seePathDetailButton;
    }

    private ImageView getDirectionIcon(int directionSign) {
        String imageFileName;
        ImageView imageView = null;

        switch (directionSign) {
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
            imageView = new ImageView(image);
            imageView.setFitHeight(18);
            imageView.setFitWidth(18);
            imageView.setPreserveRatio(true);
        }

        return imageView;
    }

    private HBox getPathDetailLine(String indication, Instruction instruction, int instructionBlocPaneIndex) {
        HBox pathDetailLine = new HBox();
        pathDetailLine.setVisible(false);
        pathDetailLine.setManaged(false);
        pathDetailLine.getStyleClass().add("path-detail-line");
        pathDetailLine.getStyleClass().add("path-detail-line" + instructionBlocPaneIndex);
        int distance = (int) instruction.getDistance();

        if (indication.startsWith("continuez") && distance > 0) {
            indication = indication + " pendant " + distance + " mètres";
        } else if (!indication.startsWith("arrivée") && distance > 0) {
            indication = indication + " et continuez sur " + distance + " mètres";
        }

        ImageView imageView = getDirectionIcon(instruction.getSign());
        if (imageView != null) {
            pathDetailLine.getChildren().add(imageView);
        }
        pathDetailLine.getChildren().add(new Label(StringUtils.capitalize(indication)));

        return pathDetailLine;
    }

    private HBox getDepartureLine() {
        Circle pointCircle = new Circle();
        pointCircle.setTranslateY(4);
        pointCircle.setRadius(6);
        Label departureLabel = new Label("  Départ");
        pointCircle.setFill(javafx.scene.paint.Color.rgb(0, 0, 0));
        HBox departureLine = new HBox(pointCircle, departureLabel);
        departureLine.getStyleClass().add("departure-line");

        return departureLine;
    }

    private HBox getArrivalLine(String closestShipmentId, Instruction instruction) {
        javafx.scene.paint.Color shipmentColor = locationIdMappedWithColor.get(closestShipmentId);
        Circle pointCircle = new Circle();
        pointCircle.setTranslateY(4);
        pointCircle.setRadius(6);
        pointCircle.setFill(shipmentColor);
        String shipmentType = locationIdMappedWithShipmentType.get(closestShipmentId) == ShipmentType.PICKUP ? " de retrait" : " de livraison";

        Label arrivalLabel = new Label("  Arrivée au point" + shipmentType + " (" + instruction.getPoints().getLat(0) + ";" + instruction.getPoints().getLon(0) + ")");
        HBox arrivalLine = new HBox(pointCircle, arrivalLabel);
        arrivalLine.getStyleClass().add("arrival-line");

        return arrivalLine;

    }

    private HBox getFinalArrivalLine() {
        Label finalArrivalLabel = new Label("  Arrivée");
        Circle pointCircle = new Circle();
        pointCircle.setTranslateY(4);
        pointCircle.setRadius(6);
        pointCircle.setFill(javafx.scene.paint.Color.rgb(0, 0, 0));
        HBox finalArrivalLine = new HBox(pointCircle, finalArrivalLabel);
        HBox.setMargin(finalArrivalLine, new Insets(100, 100, 100, 100));

        finalArrivalLine.getStyleClass().add("final-arrival-line");

        return finalArrivalLine;
    }

    /**
     * Set event handler to collapse an instruction bloc pane
     *
     * @param instructionBlocPaneIndex the index of the instruction bloc pane to collapse
     * @return event handler to collapse matching instruction bloc pane
     */
    private EventHandler<javafx.scene.input.MouseEvent> collapsePathDetail(int instructionBlocPaneIndex) {
        return e -> {
            Set<Node> nodes = rightPane.lookupAll(".path-detail-line" + instructionBlocPaneIndex);
            nodes.forEach(n -> {
                n.setVisible(!n.isVisible());
                n.setManaged(!n.isManaged());
            });
        };
    }

    /**
     * Set event handler to collapse right panel
     *
     * @return event handler to collapse right panel
     */
    private EventHandler<javafx.scene.input.MouseEvent> collapseRightPanel() {
        return e -> {
            if (rootPane.getRight() != null) rootPane.setRight(null);
            else rootPane.setRight(scrollPane);
        };
    }

    /**
     * Find the closest shipment from the given latitude and longitude point
     *
     * @param lat   : the latitude of the point to find a shipment
     * @param longi : the longitude of the point to find a shipment
     * @return id of the closest shipment
     */
    private String findClosestShipmentId(double lat, double longi) {
        String closestShipmentId = null;
        double closestDistance = 100000;
        for (Shipment s : planningResource.getShipments()) {
            double distancePickupShipment = Math.pow(lat - s.getPickupLocation().getCoordinate().getX(), 2.0) + Math.pow(longi - s.getPickupLocation().getCoordinate().getY(), 2.0);
            double distanceDeliveryShipment = Math.pow(lat - s.getDeliveryLocation().getCoordinate().getX(), 2.0) + Math.pow(longi - s.getDeliveryLocation().getCoordinate().getY(), 2.0);
            if (distancePickupShipment < closestDistance) {
                closestShipmentId = s.getPickupLocation().getId();
                closestDistance = distancePickupShipment;
            } else if (distanceDeliveryShipment < closestDistance) {
                closestShipmentId = s.getDeliveryLocation().getId();
                closestDistance = distanceDeliveryShipment;
            }
        }

        return closestShipmentId;
    }

    private ArrayList<Shipment> getAllShipments() {
        ArrayList<Shipment> shipments = new ArrayList<>();
        shipments.addAll(planningResource.getShipments());
        shipments.addAll(localShipments);
        return shipments;
    }

    private RouteInfo computeBestRoute() {
        Vehicle vehicle = planningResource.getVehicle();
        ArrayList<Shipment> shipments = getAllShipments();

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
            vehicleRoute.getActivities().forEach(tourActivity -> tracks.add(tourActivity.getLocation()));
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
