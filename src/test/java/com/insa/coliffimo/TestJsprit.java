package com.insa.coliffimo;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.util.FastVehicleRoutingTransportCostsMatrix;
import com.graphhopper.jsprit.core.util.Solutions;
import org.junit.jupiter.api.Test;


import java.util.Collection;

public class TestJsprit {

    @Test
    public void testJsprit() {
        Shipment shipment = Shipment.Builder.newInstance("shipmentId")
                .setName("myShipment")
                .setPickupLocation(Location.newInstance(2,7)).setDeliveryLocation(Location.newInstance(10,2))
                .addSizeDimension(0,9).addSizeDimension(1,50)
                .setDeliveryServiceTime(30).setPickupServiceTime(30)
                .build();

        System.out.println("shipment créé");
        System.out.println(shipment);

        // specify vehicle1 with different start and end locations
        VehicleImpl vehicle1 = VehicleImpl.Builder.newInstance("vehicle1Id")
                .setStartLocation(Location.newInstance(0,0)).setEndLocation(Location.newInstance(20,20))
                .build();
        System.out.println("véhicule créé");


        VehicleRoutingProblem problem = VehicleRoutingProblem.Builder.newInstance()
                .addJob(shipment).addVehicle(vehicle1)
                //.setRoutingCost(new FastVehicleRoutingTransportCostsMatrix(getLocations()))
                .build();

        // define an algorithm out of the box - this creates a large neighborhood search algorithm
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);

        // search solutions
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
        // get best
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);

        System.out.println(bestSolution);
    }

}
