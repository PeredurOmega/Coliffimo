package com.insa.coliffimo.business;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.insa.coliffimo.leaflet.LatLong;

import java.util.ArrayList;
import java.util.UUID;

public class AdditionalLocalMarkers {

    private final ArrayList<LatLong> coordinates = new ArrayList<>();

    public boolean addCoordinate(LatLong coordinate) {
        coordinates.add(coordinate);
        return coordinates.size() % 2 == 0;
    }

    public ArrayList<Shipment> getShipments() {
        ArrayList<Shipment> shipments = new ArrayList<>();
        int i = 0;
        while (i + 1 < coordinates.size()) {
            LatLong pickup = coordinates.get(i);
            i++;
            LatLong delivery = coordinates.get(i);
            i++;
            Shipment.Builder shipmentBuilder = Shipment.Builder.newInstance(UUID.randomUUID().toString())
                    .setPickupLocation(Location.newInstance(pickup.getLatitude(), pickup.getLongitude()))
                    .setDeliveryServiceTime(30 * 1000) // TODO ALLOW MODIFICATION
                    .setPickupServiceTime(30 * 1000)
                    .setDeliveryLocation(Location.newInstance(delivery.getLatitude(), delivery.getLongitude())); //TODO ALLOW MODIFICATION
            shipments.add(shipmentBuilder.build());
        }
        return shipments;
    }
}
