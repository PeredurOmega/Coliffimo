package com.insa.coliffimo.router;

import com.graphhopper.jsprit.core.util.Coordinate;
import javafx.scene.paint.Paint;

public class ShipmentInfo {

    public ShipmentType shipmentType;
    public Paint color;

    public ShipmentInfo(Paint color, ShipmentType shipmentType) {
        this.shipmentType = shipmentType;
        this.color = color;
    }

    public enum ShipmentType {
        PICKUP,
        DELIVERY
    }
}
