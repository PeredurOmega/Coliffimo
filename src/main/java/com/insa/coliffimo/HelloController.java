package com.insa.coliffimo;

import com.insa.coliffimo.leaflet.*;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

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

    public void loadRequest(String xmlMapResourceFileName, String xmlPlanningRequestResourceFileName){
        mapView.clearMarkersAndTracks();

        XmlParser xmlParser = new XmlParser();

        String xmlMapResourceFilePath = XML_MAP_RESOURCE_DIRECTORY_PATH + xmlMapResourceFileName;
        Map map = xmlParser.ConvertXmlToMap(new File(xmlMapResourceFilePath));

        String xmlPlanningRequestResourceFilePath = XML_PLANNING_REQUEST_RESOURCE_DIRECTORY_PATH + xmlPlanningRequestResourceFileName;
        PlanningRequest planningRequest = xmlParser.ConvertXmlToPlanningRequest(new File(xmlPlanningRequestResourceFilePath));

        HashMap<Long, Intersection> intersections = map.getListIntersections();
        ArrayList<Request> listRequests = planningRequest.getListRequests();

        IntStream.range(0, listRequests.size()).forEach((idx) -> {
            Intersection pickupCoord = intersections.get(listRequests.get(idx).getPickupAddress());
            Intersection deliveryCoord = intersections.get(listRequests.get(idx).getDeliveryAddress());

            mapView.addMarker(new LatLong(pickupCoord.getLatitude(), pickupCoord.getLongitude()),
                    "pickup"+idx, ColorMarker.GREY_MARKER, 0);
            mapView.addMarker(new LatLong(deliveryCoord.getLatitude(), deliveryCoord.getLongitude()),
                    "delivery"+idx, ColorMarker.GREY_MARKER, 0);
        });

    }
}