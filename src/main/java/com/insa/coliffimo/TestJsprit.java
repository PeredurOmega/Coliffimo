package com.insa.coliffimo;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;


import java.util.Collection;

public class TestJsprit {

    public static void main(String[] args){



        Shipment shipment = Shipment.Builder.newInstance("shipmentId")
                .setName("myShipment")
                .setPickupLocation(Location.newInstance(2,7)).setDeliveryLocation(Location.newInstance(10,2))
                .addSizeDimension(0,9).addSizeDimension(1,50)
                .addRequiredSkill("loading bridge").addRequiredSkill("electric drill")
                .build();

        System.out.println(shipment);
        /*
        // specify type of both vehicles
        VehicleTypeImpl vehicleType = VehicleTypeImpl.Builder.newInstance("vehicleType")
                .addCapacityDimension(0,30).addCapacityDimension(1,100)
                .build();

// specify vehicle1 with different start and end locations
        VehicleImpl vehicle1 = VehicleImpl.Builder.newInstance("vehicle1Id")
                .setType(vehicleType)
                .setStartLocation(Location.newInstance(0,0)).setEndLocation(Location.newInstance(20,20))
                .addSkill("loading bridge").addSkill("electric drill")
                .build();

// specify vehicle2 with open end, i.e. end is determined by the algorithm
        VehicleImpl vehicle2 = VehicleImpl.Builder.newInstance("vehicle2Id")
                .setType(vehicleType)
                .setStartLocation(Location.newInstance(5,0))
                .setReturnToDepot(false)
                .addSkill("loading bridge")
                .build();

        VehicleRoutingProblem.Builder vrpBuilder =  VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addJob(service).addJob(shipment).addVehicle(vehicle1).addVehicle(vehicle2);
        vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);
        VehicleRoutingProblem problem =  vrpBuilder.build();

        // define an algorithm out of the box - this creates a large neighborhood search algorithm
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);

        // search solutions
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
// get best
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);

        System.out.println(bestSolution);*/
    }

}
