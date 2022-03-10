package com.insa.coliffimo.metier;

import java.time.LocalTime;
import java.util.ArrayList;

/**
 * Class representing a pickup&delivery program.
 */
public class PlanningRequest {
    /**
     * Variable containing the identifiant of the deliverer departure intersection.
     */
    private int depotAddress;

    /**
     * Variable containing the LocalTime of the deliverer departure.
     */
    private LocalTime depotDepartureLocalTime;

    /**
     * Variable containing the list of Requests for the program.
     */
    private ArrayList<Request> listRequest;

    public PlanningRequest(int depotAddress, LocalTime depotDepartureLocalTime, ArrayList<Request> listRequest) {
        this.depotAddress = depotAddress;
        this.depotDepartureLocalTime = depotDepartureLocalTime;
        this.listRequest = listRequest;
    }

    public int getDepotAddress() {
        return depotAddress;
    }

    public void setDepotAddress(int depotAddress) {
        this.depotAddress = depotAddress;
    }

    public LocalTime getDepotDepartureLocalTime() {
        return depotDepartureLocalTime;
    }

    public void setDepotDepartureLocalTime(LocalTime depotDepartureLocalTime) {
        this.depotDepartureLocalTime = depotDepartureLocalTime;
    }

    public ArrayList<Request> getListRequest() {
        return listRequest;
    }

    public void setListRequest(ArrayList<Request> listRequest) {
        this.listRequest = listRequest;
    }

    @Override
    public String toString() {
        return "PlanningRequest{" +
                "depotAddress=" + depotAddress +
                ", depotDepartureLocalTime=" + depotDepartureLocalTime +
                ", listRequest=" + listRequest +
                '}';
    }
}
