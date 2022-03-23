package com.insa.coliffimo;

import com.graphhopper.GraphHopper;
import com.insa.coliffimo.leaflet.*;
import com.insa.coliffimo.router.MapResource;
import com.insa.coliffimo.router.PlanningResource;
import com.insa.coliffimo.router.RouterRunnable;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class MainController implements Initializable {

    private static final String XML_MAP_RESOURCE_DIRECTORY_PATH = Paths.get("src", "main", "resources", "map").toAbsolutePath() + "/";
    private static final String XML_PLANNING_REQUEST_RESOURCE_DIRECTORY_PATH = Paths.get("src", "main", "resources", "planningRequest").toAbsolutePath() + "/";

    /**
     * the MapView containing the map
     */
    @FXML
    private LeafletMapView mapView;

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
                MapResource mapResource = new MapResource(new File(XML_MAP_RESOURCE_DIRECTORY_PATH + "mediumMap.xml"));
                PlanningResource planningResource = new PlanningResource(mapResource, new File(XML_PLANNING_REQUEST_RESOURCE_DIRECTORY_PATH + "requestsMedium5.xml"));
                new Thread(new RouterRunnable(planningResource, mapView)).start();
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
}