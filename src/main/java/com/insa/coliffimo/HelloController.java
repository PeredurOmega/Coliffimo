package com.insa.coliffimo;

import com.sothawo.mapjfx.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HelloController {

    //private static final Logger logger = LoggerFactory.getLogger(Controller.)
    @FXML
    private Label welcomeText;

    /**
     * the MapView containing the map
     */
    @FXML
    private MapView mapView;

    public void initMapAndControls() {
        /*Projection projection = Projection.WGS_84;
        mapView.setMapType(MapType.OSM);*/
        mapView.setCenter(new Coordinate(45.764043, 4.835659));
        mapView.initialize(Configuration.builder()
                .projection(Projection.WGS_84)
                .interactive(true)
                .showZoomControls(true)
                .build());
        //mapView.addC
    }

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}