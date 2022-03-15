package com.insa.coliffimo;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.util.FastVehicleRoutingTransportCostsMatrix;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import com.graphhopper.util.*;
import org.junit.jupiter.api.Test;


import java.util.Collection;
import java.util.Locale;

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

        // Create matrix
        GraphHopper hopper = new GraphHopper();
        // Pour chaque couple de destinations = chaque case de la matrice
        // simple configuration of the request object
        GHRequest req = new GHRequest(42.508552, 1.532936, 42.507508, 1.528773).
                // note that we have to specify which profile we are using even when there is only one like here
                        setProfile("car").
                // define the language for the turn instructions
                        setLocale(Locale.US);
        GHResponse rsp = hopper.route(req);

        // handle errors
        if (rsp.hasErrors())
            throw new RuntimeException(rsp.getErrors().toString());

        // use the best path, see the GHResponse class for more possibilities.
        ResponsePath path = rsp.getBest();

        // points, distance in meters and time in millis of the full path
        PointList pointList = path.getPoints();
        double distance = path.getDistance();
        long timeInMs = path.getTime();

        Translation tr = hopper.getTranslationMap().getWithFallBack(Locale.UK);
        InstructionList il = path.getInstructions();
        // iterate over all turn instructions
        for (Instruction instruction : il) {
            // System.out.println("distance " + instruction.getDistance() + " for instruction: " + instruction.getTurnDescription(tr));
        }
        assert il.size() == 6;
        assert Helper.round(path.getDistance(), -2) == 900;



        VehicleRoutingTransportCostsMatrix.Builder costMatrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);

        VehicleRoutingProblem problem = VehicleRoutingProblem.Builder.newInstance()
                .addJob(shipment).addVehicle(vehicle1)
                .setRoutingCost(costMatrixBuilder.build())
                .build();
        System.out.println(problem);

        // define an algorithm out of the box - this creates a large neighborhood search algorithm
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);

        // search solutions
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
        // get best
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);

        System.out.println(bestSolution);
    }

}
