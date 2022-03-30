package com.insa.coliffimo;

import com.insa.coliffimo.business.AdditionalLocalMarkers;
import com.insa.coliffimo.leaflet.*;
import com.insa.coliffimo.leaflet.markers.DeliveryMarker;
import com.insa.coliffimo.leaflet.markers.DepotMarker;
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
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class MainController implements Initializable {

    private static final String XML_MAP_RESOURCE_DIRECTORY_PATH = Paths.get("src", "main", "resources", "map").toAbsolutePath() + "/";
    private static final String XML_PLANNING_REQUEST_RESOURCE_DIRECTORY_PATH = Paths.get("src", "main", "resources", "planningRequest").toAbsolutePath() + "/";

    private String xmlMapFile = XML_MAP_RESOURCE_DIRECTORY_PATH + "mediumMap.xml";
    private String xmlRequestFile = XML_PLANNING_REQUEST_RESOURCE_DIRECTORY_PATH + "requestsMedium5.xml";

    @FXML
    public BorderPane rootPane;

    @FXML
    public Label infoLabel;

    @FXML
    MFXButton collapseRightPanelButton;

    /**
     * the MapView containing the map
     */
    @FXML
    private LeafletMapView mapView;

    private final FileChooser fileChooser = new FileChooser();
    private AdditionalLocalMarkers additionalLocalMarkers = new AdditionalLocalMarkers();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mapView.setUpBridge(this);
        MapConfig initialMap = new MapConfig(Arrays.asList(MapLayer.values()),
                new ZoomControlConfig(true, ControlPosition.BOTTOM_LEFT),
                new ScaleControlConfig(true, ControlPosition.BOTTOM_LEFT, true));
        CompletableFuture<Worker.State> cfMapLoadState = mapView.displayMap(initialMap);

        // display Berlin initially after map has been loaded
        cfMapLoadState.whenComplete((workerState, error) -> {
            if (workerState == Worker.State.SUCCEEDED) {
                mapView.setView(initialMap.getInitialCenter(), initialMap.getInitialZoom());
                MapResource mapResource = new MapResource(new File(XML_MAP_RESOURCE_DIRECTORY_PATH + "mediumMap.xml"));
                PlanningResource planningResource = new PlanningResource(mapResource, new File(XML_PLANNING_REQUEST_RESOURCE_DIRECTORY_PATH + "requestsMedium5.xml"));
                new Thread(new RouterRunnable(planningResource, mapView, rootPane, collapseRightPanelButton, additionalLocalMarkers.getShipments())).start();
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
        Node node = (Node) actionEvent.getSource();
        Stage stage = (Stage) node.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            xmlRequestFile = file.getAbsolutePath();
        }
    }

    public void processItinerary(ActionEvent actionEvent) {
        if (xmlMapFile != null && xmlRequestFile != null) {
            MapResource mapResource = new MapResource(new File(xmlMapFile));
            PlanningResource planningResource = new PlanningResource(mapResource, new File(xmlRequestFile));
            new Thread(new RouterRunnable(planningResource, mapView, rootPane, collapseRightPanelButton, additionalLocalMarkers.getShipments())).start();
        } else {
            if (xmlMapFile == null && xmlRequestFile != null) infoLabel.setText("Fichier de map non renseigné");
            else if (xmlMapFile != null) infoLabel.setText("Fichier de request non renseigné");
            else infoLabel.setText("Aucun fichier renseigné");
        }
    }

    public void addPoint(LatLong marker) {
        if (additionalLocalMarkers.addCoordinate(marker)) {
            mapView.addMarker(marker, "Temp delivery", new DeliveryMarker("#555555"), 1, "Temp delivery", "temp-delivery");
            MapResource mapResource = new MapResource(new File(xmlMapFile));
            PlanningResource planningResource = new PlanningResource(mapResource, new File(xmlRequestFile));
            new Thread(new RouterRunnable(planningResource, mapView, rootPane, collapseRightPanelButton, additionalLocalMarkers.getShipments())).start();
        } else {
            mapView.addMarker(marker, "Temp pickup", new PickupMarker("#555555"), 1, "Temp pickup", "temp-pickup");
        }
    }
}