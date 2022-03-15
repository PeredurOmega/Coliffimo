package com.insa.coliffimo;

//import com.graphhopper.graphhopper.*;
//graphhopper.GHRequest;
//import com.graphhopper.graphhopper.GHResponse;
//import com.graphhopper.graphhopper.GraphHopper;
//import com.graphhopper.graphhopper.ResponsePath;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;
//import com.graphhopper.util.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

public class TestJsprit {

    @Test
    public void testJsprit() {
        ArrayList<Shipment> shipments = new ArrayList<Shipment>();
        ArrayList<Location> locations = new ArrayList<Location>();
        locations.add(Location.newInstance(2,7));
        locations.add(Location.newInstance(10,2));

        Shipment shipment1 = Shipment.Builder.newInstance("shipmentId")
                .setName("myShipment")
                .setPickupLocation(Location.newInstance(2,7)).setDeliveryLocation(Location.newInstance(10,2))
                .addSizeDimension(0,9).addSizeDimension(1,50)
                .setDeliveryServiceTime(30).setPickupServiceTime(30)
                .build();
        shipments.add(shipment1);
        System.out.println("shipment1 créé");
        System.out.println(shipment1);

        // create vehicle1 with start location (and end location)
        VehicleImpl vehicle1 = VehicleImpl.Builder.newInstance("vehicle1Id")
                .setStartLocation(Location.newInstance(0,0)).setEndLocation(Location.newInstance(20,20))
                .build();
        System.out.println("véhicule créé");

        // Création de la matrice de coûts. isSymetric = False car il y a des sens de circulation
        VehicleRoutingTransportCostsMatrix.Builder costMatrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);

        // calcul des distances et temps entre chaque endroit considéré
        //graphhopper.GraphHopper hopper = new GraphHopper();

        for (int i=0 ; i< locations.size();i++){
            for (int j=0 ; j<locations.size();j++){
                // Chaque case de la matrice est une distance/temps entre deux endroits (i->j), pas forcément des endroits d'une même livraison
                // GHRequest(double fromLat, double fromLon, double toLat, double toLon)
                /*
                GHRequest req = new GHRequest(locations.get(i).getCoordinate().getX(), locations.get(i).getCoordinate().getX(), locations.get(j).getCoordinate().getX(),locations.get(j).getCoordinate().getY()).
                        // note that we have to specify which profile we are using even when there is only one like here
                                setProfile("car");
                //GHResponse : stocke les différents chemins possibles pour une GHRequest, et renvoie le meilleur avec getBest()
                GHResponse rsp = hopper.route(req);
                // handle errors
                if (rsp.hasErrors())
                    throw new RuntimeException(rsp.getErrors().toString());
                // use the best path, see the GHResponse class for more possibilities.
                ResponsePath path = rsp.getBest();

                // points, distance in meters and time in millis of the full path
                //PointList pointList = path.getPoints();
                double distance = path.getDistance();
                long timeInMs = path.getTime();
                */
                double distance = 12.0;
                long timeInMs = 5000;

                //On met ces valeurs dans la matrice de coûts
                costMatrixBuilder.addTransportDistance(Integer.toString(i), Integer.toString(j), distance);
                costMatrixBuilder.addTransportTime(Integer.toString(i), Integer.toString(j), timeInMs);
                System.out.println("meilleure distance entre " + locations.get(i) +" et "+ locations.get(j) + " : " + distance);
            }
        }
        System.out.println(costMatrixBuilder);

        // Calcule le meilleur itinéraire en se servant de la matrice de coûts costMatrixBuilder
        VehicleRoutingProblem problem = VehicleRoutingProblem.Builder.newInstance()
                .addJob(shipment1).addVehicle(vehicle1)
                .setRoutingCost(costMatrixBuilder.build())
                .build();
        System.out.println(problem);

        /*
        // define an algorithm out of the box - this creates a large neighborhood search algorithm
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);

        // search solutions
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
        // get best
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);

        System.out.println(bestSolution);

         */
    }

}
