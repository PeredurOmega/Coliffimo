package com.insa.coliffimo;


import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.Profile;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
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
        locations.add(Location.newInstance(45.7785,4.871));      //départ livraison
        locations.add(Location.newInstance(45.7785,4.8704588)); //arrivée livraison
        locations.add(Location.newInstance(45.778,4.89273));  //départ livreur
        locations.add(Location.newInstance(45.7785,4.89273));   //arrivée livreur

        Shipment shipment1 = Shipment.Builder.newInstance("shipmentId")
                .setName("myShipment")
                .setPickupLocation(locations.get(0)).setDeliveryLocation(locations.get(1))
                .addSizeDimension(0,9).addSizeDimension(1,50)
                .setDeliveryServiceTime(30).setPickupServiceTime(30)
                .build();
        shipments.add(shipment1);
        System.out.println("shipment1 créé");
        System.out.println(shipment1);

        // create vehicle1 with start location (and end location)
        VehicleImpl vehicle1 = VehicleImpl.Builder.newInstance("vehicle1Id")
                .setStartLocation(locations.get(2)).setEndLocation(locations.get(3))
                .build();
        System.out.println("véhicule créé");

        // Création de la matrice de coûts. isSymetric = False car il y a des sens de circulation
        VehicleRoutingTransportCostsMatrix.Builder costMatrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);

        // calcul des distances et temps entre chaque endroit considéré
        GraphHopper hopper = new GraphHopper();
        hopper.setOSMFile("src/test/resources/map.osm");
        hopper.setGraphHopperLocation("./resources/routing-graph-cache");
        // see docs/core/profiles.md to learn more about profiles
        hopper.setProfiles(new Profile("car").setVehicle("car").setWeighting("fastest").setTurnCosts(false));
        hopper.importOrLoad();
        System.out.println(locations.toString());

        for (int i=0 ; i< locations.size();i++){
            for (int j=0 ; j<locations.size();j++){
                // Chaque case de la matrice est une distance/temps entre deux endroits (i->j), pas forcément des endroits d'une même livraison
                // GHRequest(double fromLat, double fromLon, double toLat, double toLon)
                GHRequest req = new GHRequest(locations.get(i).getCoordinate().getX(), locations.get(i).getCoordinate().getY(), locations.get(j).getCoordinate().getX(),locations.get(j).getCoordinate().getY())
                        // note that we have to specify which profile we are using even when there is only one like here
                    .setProfile("car");
                //GHResponse : stocke les différents chemins possibles pour une GHRequest, et renvoie le meilleur avec getBest()
                GHResponse rsp = hopper.route(req);
                // handle errors
                if (rsp.hasErrors()) {
                    //System.out.println("point i : "+ locations.get(i) + " et j : "+ locations.get(j));
                    throw new RuntimeException(rsp.getErrors().toString());
                }
                // use the best path, see the GHResponse class for more possibilities.
                ResponsePath path = rsp.getBest();
                System.out.println("point i : "+ locations.get(i) + " et j : "+ locations.get(j));
                System.out.println(rsp.toString());

                // points, distance in meters and time in millis of the full path
                //PointList pointList = path.getPoints();
                double distance = path.getDistance();
                long timeInMs = path.getTime();


                //On met ces valeurs dans la matrice de coûts
                costMatrixBuilder.addTransportDistance(Integer.toString(i), Integer.toString(j), distance);
                costMatrixBuilder.addTransportTime(Integer.toString(i), Integer.toString(j), timeInMs);
            }
        }

        // Calcule le meilleur itinéraire en se servant de la matrice de coûts costMatrixBuilder
        VehicleRoutingProblem problem = VehicleRoutingProblem.Builder.newInstance()
                .addJob(shipment1).addVehicle(vehicle1)
                .setRoutingCost(costMatrixBuilder.build())
                .build();

        System.out.println(costMatrixBuilder.build().getDistance("2","2"));
        //System.out.println(costMatrixBuilder.build().getTransportTime(locations.get(2),locations.get(2),0.00,new Driver(),new Vehicle()));
        //System.out.println(costMatrixBuilder.build().getTransportTime("2","2"));

        // define an algorithm out of the box - this creates a large neighborhood search algorithm
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);
/*
        System.out.println(algorithm.toString());
        // search solutions
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
/*
        // get best
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);

        System.out.println(bestSolution);

         */


    }

}
