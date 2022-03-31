package com.insa.coliffimo.utils;

import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivities;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.Translation;
import com.insa.coliffimo.router.PlanningResource;
import com.insa.coliffimo.router.RhoneAlpesGraphHopper;
import com.insa.coliffimo.router.RouteInfo;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.Locale;

public class JsonParser {

    /**
     * Variable containing the path to the save file.
     */
    private static String PATH = "resources/savefiles/";


    /**
     * Constructor of the class.
     *
     * @param PATH
     */
    public JsonParser(String PATH) {
        JsonParser.PATH = PATH;
    }


    public static void sauvegarder(RouteInfo info, String filename, PlanningResource planningResource) {
        LocalTime depart = planningResource.getPlanningRequest().getDepotDepartureLocalTime();
        createPath(filename);
        JSONObject itineraire = new JSONObject();
        JSONArray trajets = new JSONArray();
        Translation tr = RhoneAlpesGraphHopper.getGraphHopper().getTranslationMap().getWithFallBack(Locale.FRANCE);
        String indication;
        long time = 0;
        for (InstructionList instructionList : info.instructionLists) {
            for (Instruction iti : instructionList) {
                indication = StringUtils.uncapitalize(iti.getTurnDescription(tr));
                int distance = (int) iti.getDistance();
                time = time + iti.getTime();
                if (indication.startsWith("continuez") && distance > 0) {
                    indication = indication + " pendant " + distance + " mètres";
                } else if (!indication.startsWith("arrivée") && distance > 0) {
                    indication = indication + " et continuez sur " + distance + " mètres";
                }
                JSONObject indicate = new JSONObject();
                indicate.put("Description", indication);
                if (indication.startsWith("arrivée")) {
                    depart = depart.plusMinutes(time / 60000);
                    double virg = time / (double) 60000;
                    virg = virg - Math.floor(virg);
                    depart = depart.plusSeconds((long) (virg * 100));
                    indicate.put("Heure d'arrivée", depart);
                    time = 0;
                }
                trajets.put(indicate);
            }
        }
        itineraire.put("Heure de départ", depart);
        itineraire.put("Trajet", trajets);
        if (!JsonParser.createPath(filename)) {
            File f = new File(JsonParser.PATH + filename);
            f.delete();
        }
        try (OutputStreamWriter ecriture =
                     new OutputStreamWriter(new FileOutputStream(JsonParser.PATH + filename), StandardCharsets.UTF_8)) {
            ecriture.write(itineraire.toString(4));
            ecriture.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Static method to create the save file.
     *
     * @return boolean
     */
    public static boolean createPath(String filename) {

        try {
            File file = new File(JsonParser.PATH + filename);
            file.mkdirs();
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private double activityTime(TourActivities tourActivity, String id){
        for (TourActivity a : tourActivity.getActivities()){
            if (a.getLocation().getId().equals(id)){
                return a.getArrTime()/1000;
            }
        }
        return 0.0;
    }
}
