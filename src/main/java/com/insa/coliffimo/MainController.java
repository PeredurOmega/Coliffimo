package com.insa.coliffimo;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.insa.coliffimo.business.AdditionalLocalMarkers;
import com.insa.coliffimo.leaflet.*;
import com.insa.coliffimo.leaflet.markers.DeliveryMarker;
import com.insa.coliffimo.leaflet.markers.PickupMarker;
import com.insa.coliffimo.router.MapResource;
import com.insa.coliffimo.router.PlanningResource;
import com.insa.coliffimo.router.RouterRunnable;
import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MainController implements Initializable {

    private static final String XML_MAP_RESOURCE_DIRECTORY_PATH = Paths.get("src", "main", "resources", "map").toAbsolutePath() + "/";
    private static final String XML_PLANNING_REQUEST_RESOURCE_DIRECTORY_PATH = Paths.get("src", "main", "resources", "planningRequest").toAbsolutePath() + "/";


    private String xmlMapFile = XML_MAP_RESOURCE_DIRECTORY_PATH + "mediumMap.xml";
    private String xmlRequestFile = XML_PLANNING_REQUEST_RESOURCE_DIRECTORY_PATH + "requestsMedium5.xml";

    private MapResource mapResource = null;
    private PlanningResource planningResource = null;

    private boolean firstAdded = false;
    private LatLong firstCoordinate = null;

    private boolean buttonHandler = false;

    @FXML
    public BorderPane rootPane;

    @FXML
    public HBox topPane;

    @FXML
    public Label infoLabel;

    @FXML
    MFXButton collapseRightPanelButton;

    ProgressIndicator itiProgress = new ProgressIndicator();
    /**
     * the MapView containing the map
     */
    @FXML
    private LeafletMapView mapView;

    private final FileChooser fileChooser = new FileChooser();
    private AdditionalLocalMarkers additionalLocalMarkers = new AdditionalLocalMarkers();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        itiProgress.setPrefHeight(43.0);
        mapView.setUpBridge(this);
        MapConfig initialMap = new MapConfig(Arrays.asList(MapLayer.values()),
                new ZoomControlConfig(true, ControlPosition.BOTTOM_LEFT),
                new ScaleControlConfig(true, ControlPosition.BOTTOM_LEFT, true));
        CompletableFuture<Worker.State> cfMapLoadState = mapView.displayMap(initialMap);

        // display Berlin initially after map has been loaded
        cfMapLoadState.whenComplete((workerState, error) -> {
            if (workerState == Worker.State.SUCCEEDED) {
                mapView.setView(initialMap.getInitialCenter(), initialMap.getInitialZoom());
                //new Thread(new RouterRunnable(planningResource, mapView, rootPane, collapseRightPanelButton, buttonHandler, additionalLocalMarkers.getShipments())).start();
                //buttonHandler = true;
            }
        });
    }

    public void chooseMapFile(ActionEvent actionEvent) {
        Node node = (Node) actionEvent.getSource();
        Stage stage = (Stage) node.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            xmlMapFile = file.getAbsolutePath();
        }
    }

    public void chooseRequestFile(ActionEvent actionEvent) {
        // Cleaning local markers
        additionalLocalMarkers = new AdditionalLocalMarkers();
        firstCoordinate = null;
        firstAdded = false;
        Node node = (Node) actionEvent.getSource();
        Stage stage = (Stage) node.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            xmlRequestFile = file.getAbsolutePath();
        }
    }

    public void processItinerary() {
        if (xmlMapFile != null && xmlRequestFile != null) {
            mapResource = new MapResource(new File(xmlMapFile));
            planningResource = new PlanningResource(mapResource, new File(xmlRequestFile));
            firstCoordinate = null;
            firstAdded = false;
            topPane.getChildren().add(itiProgress);
            new Thread(new RouterRunnable(planningResource, mapView, rootPane, topPane, collapseRightPanelButton, buttonHandler, additionalLocalMarkers.getShipments())).start();
            buttonHandler = true;
        } else {
            if (xmlMapFile == null && xmlRequestFile != null) infoLabel.setText("Fichier de map non renseigné");
            else if (xmlMapFile != null) infoLabel.setText("Fichier de request non renseigné");
            else infoLabel.setText("Aucun fichier renseigné");
        }
    }

    public void addPoint(LatLong marker) {
        if (!firstAdded) {
            mapView.addMarker(marker, "Temp pickup", new PickupMarker("#555555", 0), 1, "Temp pickup", "temp-pickup");
            firstCoordinate = marker;
            firstAdded = true;
        } else {
            mapView.addMarker(marker, "Temp delivery", new DeliveryMarker("#555555", 0), 1, "Temp delivery", "temp-delivery");
            additionalLocalMarkers.addShipments(firstCoordinate, marker);
            topPane.getChildren().add(itiProgress);
            new Thread(new RouterRunnable(planningResource, mapView, rootPane, topPane, collapseRightPanelButton, buttonHandler, additionalLocalMarkers.getShipments())).start();
            firstCoordinate = null;
            firstAdded = false;
        }
    }

    public void deletePoint(String idMarker) {
        deleteShipment(idMarker);
        firstCoordinate = null;
        firstAdded = false;
        topPane.getChildren().add(itiProgress);
        new Thread(new RouterRunnable(planningResource, mapView, rootPane, topPane, collapseRightPanelButton, buttonHandler, additionalLocalMarkers.getShipments())).start();
    }

    public void deleteShipment(String idMarker) {
        ArrayList<Shipment> localShipments = planningResource.getShipments();
        ArrayList<Shipment> tempShipments = additionalLocalMarkers.getShipments();
        String idShipment = idMarker.replace("pickup", "").replace("delivery", "");
        localShipments.removeIf(shipment -> shipment.getId().equals(idShipment));
        tempShipments.removeIf(shipment -> shipment.getId().equals(idShipment));
    }

    public void movePoint(String idMarker, double lat, double lng) {
        String idShipment = idMarker.replace("pickup", "").replace("delivery", "");
        addMovedPoint(idMarker, lat, lng, idShipment, planningResource.getShipments());
        addMovedPoint(idMarker, lat, lng, idShipment, additionalLocalMarkers.getShipments());

        firstCoordinate = null;
        firstAdded = false;
        topPane.getChildren().add(itiProgress);
        new Thread(new RouterRunnable(planningResource, mapView, rootPane, topPane, collapseRightPanelButton, buttonHandler, additionalLocalMarkers.getShipments())).start();
    }

    private void addMovedPoint(String idMarker, double lat, double lng, String idShipment, ArrayList<Shipment> shipments) {
        Shipment newShipment;
        for (Shipment shipment : shipments) {
            if (shipment.getId().equals(idShipment)) {
                if (idMarker.startsWith("pickup")) {
                    newShipment = Shipment.Builder.newInstance(UUID.randomUUID().toString())
                            .setPickupLocation(Location.newInstance(lat, lng))
                            .setDeliveryLocation(Location.newInstance(
                                    shipment.getDeliveryLocation().getCoordinate().getX(),
                                    shipment.getDeliveryLocation().getCoordinate().getY()))
                            .setDeliveryServiceTime(shipment.getDeliveryServiceTime())
                            .setPickupServiceTime(shipment.getPickupServiceTime())
                            .build();
                    shipments.remove(shipment);
                    shipments.add(newShipment);
                    break;
                } else if (idMarker.startsWith("delivery")) {
                    newShipment = Shipment.Builder.newInstance(UUID.randomUUID().toString())
                            .setDeliveryLocation(Location.newInstance(lat, lng))
                            .setPickupLocation(Location.newInstance(
                                    shipment.getPickupLocation().getCoordinate().getX(),
                                    shipment.getPickupLocation().getCoordinate().getY()))
                            .setDeliveryServiceTime(shipment.getDeliveryServiceTime())
                            .setPickupServiceTime(shipment.getPickupServiceTime())
                            .build();
                    shipments.remove(shipment);
                    shipments.add(newShipment);
                    break;
                }
            }
        }
    }
}