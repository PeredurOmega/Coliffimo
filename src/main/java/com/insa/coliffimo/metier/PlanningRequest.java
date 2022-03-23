package com.insa.coliffimo.metier;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Class representing a pickup&delivery program.
 */
public class PlanningRequest {
    /**
     * Variable containing the identifiant of the deliverer departure intersection.
     */
    private Long depotAddress;

    /**
     * Variable containing the LocalTime of the deliverer departure.
     */
    private LocalTime depotDepartureLocalTime;

    /**
     * Variable containing the list of Requests for the program.
     */
    private ArrayList<Request> listRequests;

    public PlanningRequest() {
        this.depotAddress = null;
        this.depotDepartureLocalTime = null;
        this.listRequests = new ArrayList<>();
    }

    public Long getDepotAddress() {
        return depotAddress;
    }

    public void setDepotAddress(Long depotAddress) {
        this.depotAddress = depotAddress;
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
     * @param r : the request to add.
     */
    public void addRequest(Request r){
        this.listRequests.add(r);
    }

    @Override
    public String toString() {
        return "PlanningRequest{" +
                "depotAddress=" + depotAddress +
                ", depotDepartureLocalTime=" + depotDepartureLocalTime +
                ", listRequest=" + listRequests +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlanningRequest that = (PlanningRequest) o;
        return Objects.equals(depotAddress, that.depotAddress) && Objects.equals(depotDepartureLocalTime, that.depotDepartureLocalTime) && Objects.equals(listRequests, that.listRequests);
    }

    @Override
    public int hashCode() {
        return Objects.hash(depotAddress, depotDepartureLocalTime, listRequests);
    }

    public void updateCoordinatesRequests(Map m){
        for(int i = 0; i < this.listRequests.size(); i++){
            this.listRequests.get(i).updateCoordinates(m);
        }
    }

}
