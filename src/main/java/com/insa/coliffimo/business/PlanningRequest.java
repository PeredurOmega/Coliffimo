package com.insa.coliffimo.business;

import com.graphhopper.jsprit.core.problem.job.Shipment;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Class representing a pickup&delivery program.
 */
public class PlanningRequest {
    /**
     * Departure intersection.
     */
    private Intersection depot;

    /**
     * Variable containing the LocalTime of the deliverer departure.
     */
    private LocalTime depotDepartureLocalTime;

    /**
     * Variable containing the list of Requests for the program.
     */
    private final ArrayList<Request> listRequests = new ArrayList<>();

    public PlanningRequest() {
        this.depot = null;
        this.depotDepartureLocalTime = null;
    }

    public Intersection getDepot() {
        return depot;
    }

    public void setDepot(Intersection depot) {
        this.depot = depot;
    }

    public LocalTime getDepotDepartureLocalTime() {
        return depotDepartureLocalTime;
    }

    public void setDepotDepartureLocalTime(LocalTime depotDepartureLocalTime) {
        this.depotDepartureLocalTime = depotDepartureLocalTime;
    }

    public ArrayList<Request> getListRequests() {
        return listRequests;
    }

    /**
     * Add request to the list of requests.
     *
     * @param r : the request to add.
     */
    public void addRequest(Request r) {
        this.listRequests.add(r);
    }

    public ArrayList<Shipment> asShipments() {
        ArrayList<Shipment> shipments = new ArrayList<>();
        listRequests.forEach((request) -> {
            shipments.add(request.asShipment());
        });
        return shipments;
    }

    @Override
    public String toString() {
        return "PlanningRequest{" +
                "depot=" + depot +
                ", depotDepartureLocalTime=" + depotDepartureLocalTime +
                ", listRequest=" + listRequests +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlanningRequest that = (PlanningRequest) o;
        return Objects.equals(depot, that.depot) && Objects.equals(depotDepartureLocalTime, that.depotDepartureLocalTime) && Objects.equals(listRequests, that.listRequests);
    }

    @Override
    public int hashCode() {
        return Objects.hash(depot, depotDepartureLocalTime, listRequests);
    }
}
