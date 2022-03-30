package com.insa.coliffimo.business;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.insa.coliffimo.leaflet.LatLong;

import java.util.ArrayList;
import java.util.UUID;

public class AdditionalLocalMarkers {

    private final ArrayList<Shipment> shipments = new ArrayList<>();

    public void addShipments(LatLong pickup, LatLong delivery) {
        Shipment.Builder shipmentBuilder = Shipment.Builder.newInstance(UUID.randomUUID().toString())
                .setPickupLocation(Location.newInstance(pickup.getLatitude(), pickup.getLongitude()))
                .setDeliveryServiceTime(30 * 1000)
                .setPickupServiceTime(30 * 1000)
                .setDeliveryLocation(Location.newInstance(delivery.getLatitude(), delivery.getLongitude()));
        shipments.add(shipmentBuilder.build());

    }

    public ArrayList<Shipment> getShipments() {
        return shipments;
    }
}
