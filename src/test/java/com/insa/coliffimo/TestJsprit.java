package com.insa.coliffimo;


import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.Profile;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.termination.IterationWithoutImprovementTermination;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;

public class TestJsprit {

    @Test
    public void testJsprit5() {
        ArrayList<Location> locations = new ArrayList<Location>();
        locations.add(Location.newInstance(45.747588, 4.833916));
        locations.add(Location.newInstance(45.733589, 4.87793));
        locations.add(Location.newInstance(45.70498, 4.887783));
        locations.add(Location.newInstance(45.705112, 4.960636));

        Shipment shipment1 = Shipment.Builder.newInstance("shipmentId")
                .setName("myShipment")
                .setPickupLocation(locations.get(1))
                .setDeliveryLocation(locations.get(2))
                .setDeliveryServiceTime(30)
                .setPickupServiceTime(60)
                .build();

        VehicleTypeImpl vehicleType = VehicleTypeImpl.Builder.newInstance("carType")
                /*.setCostPerDistance(0)
                .setCostPerTime(1)*/
                .build();

        VehicleImpl vehicle1 = VehicleImpl.Builder.newInstance("vehicle1Id")
                .setStartLocation(locations.get(0))
                .setType(vehicleType)
                .setEndLocation(locations.get(3))
                .build();

        VehicleRoutingTransportCostsMatrix.Builder costMatrixBuilder =
                VehicleRoutingTransportCostsMatrix.Builder.newInstance(false);

        GraphHopper hopper = new GraphHopper();
        hopper.setOSMFile("./resources/rhone-alpes-latest.osm.pbf");
        hopper.setGraphHopperLocation("./resources/routing-graph-cache");
        // see docs/core/profiles.md to learn more about profiles
        hopper.setProfiles(new Profile("car"));
        hopper.importOrLoad();

        for (int i = 0; i < locations.size(); i++) {
            for (int j = 0; j < locations.size(); j++) {
                // Chaque case de la matrice est une distance/temps entre deux endroits (i->j), pas forcément des endroits d'une même livraison
                // GHRequest(double fromLat, double fromLon, double toLat, double toLon)
                Coordinate from = locations.get(i).getCoordinate();
                Coordinate to = locations.get(j).getCoordinate();
                GHRequest req = new GHRequest(from.getX(), from.getY(), to.getX(), to.getY())
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
                System.out.println("point i : " + locations.get(i) + " et j : " + locations.get(j));
                System.out.println(rsp.toString());

                // points, distance in meters and time in millis of the full path
                //PointList pointList = path.getPoints();
                double distance = path.getDistance();
                long timeInMs = path.getTime();


                //On met ces valeurs dans la matrice de coûts
                System.out.println("DISTANCE FROM " + locations.get(i).getCoordinate());
                System.out.println("DISTANCE TO " + locations.get(j).getCoordinate());
                System.out.println("DISTANCE TEST " + timeInMs + " " + distance);
                /*costMatrixBuilder.addTransportDistance(Integer.toString(i), Integer.toString(j), distance);
                costMatrixBuilder.addTransportTime(Integer.toString(i), Integer.toString(j), timeInMs);*/
                costMatrixBuilder.addTransportDistance(from.toString(), to.toString(), distance);
                costMatrixBuilder.addTransportTime(from.toString(), to.toString(), timeInMs);
            }
        }

        // Calcule le meilleur itinéraire en se servant de la matrice de coûts costMatrixBuilder
        VehicleRoutingProblem problem = VehicleRoutingProblem.Builder.newInstance()
                .setRoutingCost(costMatrixBuilder.build())
                .addVehicle(vehicle1)
                .addJob(shipment1)
                .build();
        System.out.println("costMatrixBuilder");
        //System.out.println(costMatrixBuilder.build().getDistance("1", "2"));
        System.out.println(costMatrixBuilder.build().getDistance(locations.get(1), locations.get(2), 0.00, vehicle1));
        //System.out.println(costMatrixBuilder.build().getTransportTime("2","2"));

        // define an algorithm out of the box - this creates a large neighborhood search algorithm
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);
        algorithm.setPrematureAlgorithmTermination(new IterationWithoutImprovementTermination(100));
        // search solutions
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();

        // get best
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);
        solutions.forEach((solution) -> {
            System.out.println("SOLUTION" + solution.getCost());
        });
        bestSolution.getRoutes().forEach(vehicleRoute -> {
            System.out.println("ROUTE " + vehicleRoute.getDepartureTime() + " " + vehicleRoute.getStart() + " " + vehicleRoute.getEnd());

            vehicleRoute.getActivities().forEach(tourActivity -> {
                System.out.println("ACTIVITY " + tourActivity.toString());
            });
        });
        SolutionPrinter.print(bestSolution);
        /*System.out.println("bestSolution");
        System.out.println(bestSolution);*/
    }

    @Test
    @Disabled
    public void testJsprit() {
        ArrayList<Shipment> shipments = new ArrayList<Shipment>();
        ArrayList<Location> locations = new ArrayList<Location>();
        locations.add(Location.newInstance(45.7785, 4.871));      //départ livraison
        locations.add(Location.newInstance(45.7785, 4.8704588)); //arrivée livraison
        locations.add(Location.newInstance(45.778, 4.89273));  //départ livreur
        locations.add(Location.newInstance(45.7785, 4.89273));   //arrivée livreur

        Shipment shipment1 = Shipment.Builder.newInstance("shipmentId")
                .setName("myShipment")
                .setPickupLocation(locations.get(0)).setDeliveryLocation(locations.get(1))
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

        for (int i = 0; i < locations.size(); i++) {
            for (int j = 0; j < locations.size(); j++) {
                // Chaque case de la matrice est une distance/temps entre deux endroits (i->j), pas forcément des endroits d'une même livraison
                // GHRequest(double fromLat, double fromLon, double toLat, double toLon)
                GHRequest req = new GHRequest(locations.get(i).getCoordinate().getX(), locations.get(i).getCoordinate().getY(), locations.get(j).getCoordinate().getX(), locations.get(j).getCoordinate().getY())
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
                System.out.println("point i : " + locations.get(i) + " et j : " + locations.get(j));
                System.out.println(rsp.toString());

                // points, distance in meters and time in millis of the full path
                //PointList pointList = path.getPoints();
                double distance = path.getDistance() + 10;
                long timeInMs = path.getTime() + 10;


                //On met ces valeurs dans la matrice de coûts
                System.out.println("DISTANCE FROM " + locations.get(i).getCoordinate());
                System.out.println("DISTANCE TO " + locations.get(j).getCoordinate());
                System.out.println("DISTANCE TEST " + timeInMs + " " + distance);
                /*costMatrixBuilder.addTransportDistance(Integer.toString(i), Integer.toString(j), distance);
                costMatrixBuilder.addTransportTime(Integer.toString(i), Integer.toString(j), timeInMs);*/
                costMatrixBuilder.addTransportDistance(locations.get(i).getCoordinate().toString(), locations.get(j).getCoordinate().toString(), distance);
                costMatrixBuilder.addTransportTime(locations.get(i).getCoordinate().toString(), locations.get(j).getCoordinate().toString(), timeInMs);
            }
        }

        // Calcule le meilleur itinéraire en se servant de la matrice de coûts costMatrixBuilder
        VehicleRoutingProblem problem = VehicleRoutingProblem.Builder.newInstance()
                .addJob(shipment1).addVehicle(vehicle1)
                .setRoutingCost(costMatrixBuilder.build())
                .build();
        System.out.println("costMatrixBuilder");
        //System.out.println(costMatrixBuilder.build().getDistance("1", "2"));
        System.out.println(costMatrixBuilder.build().getDistance(locations.get(1), locations.get(2), 0.00, vehicle1));
        //System.out.println(costMatrixBuilder.build().getTransportTime("2","2"));

        // define an algorithm out of the box - this creates a large neighborhood search algorithm
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);
        System.out.println(algorithm.toString());
        // search solutions
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
        // get best
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);
        solutions.forEach((solution) -> {
            System.out.println("SOLUTION" + solution.getCost());
        });
        System.out.println("bestSolution");
        System.out.println(bestSolution);
    }

    @Test
    @Disabled
    public void testJsprit3() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, 1).setCostPerDistance(1).setCostPerTime(2).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("vehicle1")
                .setStartLocation(Location.newInstance("0")).setType(type).build();
        VehicleImpl vehicle2 = VehicleImpl.Builder.newInstance("vehicle2")
                .setStartLocation(Location.newInstance("3")).setType(type).build();

        Service s1 = Service.Builder.newInstance("1").addSizeDimension(0, 1).setLocation(Location.newInstance("1")).build();
        Service s2 = Service.Builder.newInstance("2").addSizeDimension(0, 1).setLocation(Location.newInstance("2")).build();
        Service s3 = Service.Builder.newInstance("3").addSizeDimension(0, 1).setLocation(Location.newInstance("3")).build();
        Service s4 = Service.Builder.newInstance("4").addSizeDimension(0, 1).setLocation(Location.newInstance("1")).build();


        /*
         * Assume the following symmetric distance-matrix
         * from,to,distance
         * 0,1,10.0
         * 0,2,20.0
         * 0,3,5.0
         * 1,2,4.0
         * 1,3,1.0
         * 2,3,2.0
         *
         * and this time-matrix
         * 0,1,5.0
         * 0,2,10.0
         * 0,3,2.5
         * 1,2,2.0
         * 1,3,0.5
         * 2,3,1.0
         */
        //define a matrix-builder building a symmetric matrix
        VehicleRoutingTransportCostsMatrix.Builder costMatrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        costMatrixBuilder.addTransportDistance("0", "1", 10.0);
        costMatrixBuilder.addTransportDistance("0", "2", 20.0);
        costMatrixBuilder.addTransportDistance("0", "3", 5.0);
        costMatrixBuilder.addTransportDistance("1", "2", 4.0);
        costMatrixBuilder.addTransportDistance("1", "3", 1.0);
        costMatrixBuilder.addTransportDistance("2", "3", 2.0);

        costMatrixBuilder.addTransportTime("0", "1", 10.0);
        costMatrixBuilder.addTransportTime("0", "2", 20.0);
        costMatrixBuilder.addTransportTime("0", "3", 5.0);
        costMatrixBuilder.addTransportTime("1", "2", 4.0);
        costMatrixBuilder.addTransportTime("1", "3", 1.0);
        costMatrixBuilder.addTransportTime("2", "3", 2.0);

        VehicleRoutingTransportCosts costMatrix = costMatrixBuilder.build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().setFleetSize(VehicleRoutingProblem.FleetSize.INFINITE).setRoutingCost(costMatrix)
                .addVehicle(vehicle).addVehicle(vehicle2).addJob(s1).addJob(s2).addJob(s3).addJob(s4).build();

        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);

        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        VehicleRoutingProblemSolution best = Solutions.bestOf(solutions);
        SolutionPrinter.print(Solutions.bestOf(solutions));
    }

    @Test
    @Disabled
    public void testJsprit2() {
        /*
         * get a vehicle type-builder and build a type with the typeId "vehicleType" and one capacity dimension, i.e. weight, and capacity dimension value of 2
         */
        final int WEIGHT_INDEX = 0;
        VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType").addCapacityDimension(WEIGHT_INDEX, 2);
        VehicleType vehicleType = vehicleTypeBuilder.build();

        /*
         * get a vehicle-builder and build a vehicle located at (10,10) with type "vehicleType"
         */
        VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance("vehicle");
        vehicleBuilder.setStartLocation(Location.newInstance(45.7785, 4.871));
        vehicleBuilder.setType(vehicleType);
        VehicleImpl vehicle = vehicleBuilder.build();

        /*
         * build services at the required locations, each with a capacity-demand of 1.
         */
        Service service1 = Service.Builder.newInstance("1").setPriority(1).addSizeDimension(WEIGHT_INDEX, 1).setLocation(Location.newInstance(45.7785, 4.8704588)).build();
        Service service2 = Service.Builder.newInstance("2").addSizeDimension(WEIGHT_INDEX, 1).setLocation(Location.newInstance(45.778, 4.89273)).build();

        Service service3 = Service.Builder.newInstance("3").addSizeDimension(WEIGHT_INDEX, 1).setLocation(Location.newInstance(45.7785, 4.89273)).build();
        //Service service4 = Service.Builder.newInstance("4").setPriority(1).addSizeDimension(WEIGHT_INDEX, 1).setLocation(Location.newInstance(15, 13)).build();


        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addVehicle(vehicle);
        vrpBuilder.addJob(service1).addJob(service2).addJob(service3).setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);

        VehicleRoutingProblem problem = vrpBuilder.build();

        /*
         * get the algorithm out-of-the-box.
         */
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);

        /*
         * and search a solution
         */
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();

        /*
         * get the best
         */
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);

        SolutionPrinter.print(problem, bestSolution, SolutionPrinter.Print.VERBOSE);
    }
}
