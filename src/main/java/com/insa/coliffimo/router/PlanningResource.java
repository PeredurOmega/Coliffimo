package com.insa.coliffimo.router;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.insa.coliffimo.business.PlanningRequest;
import com.insa.coliffimo.utils.XmlParser;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

public class PlanningResource {
    private final MapResource mapResource;
    private final PlanningRequest planningRequest;

    public PlanningResource(MapResource mapResource, File xmlPlanningRequestResourceFile) {
        this.mapResource = mapResource;
        XmlParser xmlParser = new XmlParser();
        planningRequest = xmlParser.convertXmlToPlanningRequest(xmlPlanningRequestResourceFile, mapResource.getIntersections());
    }

    public Vehicle getVehicle() {
        VehicleTypeImpl vehicleType = VehicleTypeImpl.Builder.newInstance("carType")
                .setCostPerDistance(0)
                .setCostPerTransportTime(1)
                .build();

        Location depotLocation = planningRequest.getDepot().asLocation();

        return VehicleImpl.Builder.newInstance(UUID.randomUUID().toString())
                .setStartLocation(depotLocation)
                .setType(vehicleType)
                .setEndLocation(depotLocation)
                .build();
    }

    public ArrayList<Shipment> getShipments() {
        return planningRequest.asShipments();
    }
}
