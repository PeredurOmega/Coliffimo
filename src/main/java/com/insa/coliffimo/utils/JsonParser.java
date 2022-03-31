package com.insa.coliffimo.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.insa.coliffimo.router.PlanningResource;
import com.insa.coliffimo.router.RouteInfo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

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


    public static void sauvegarder(RouteInfo info, String filename) {
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeSpecialFloatingPointValues().create();
        String json = gson.toJson(info.routes);

        if (!JsonParser.createPath(filename)) {
            File f = new File(JsonParser.PATH + filename);
            f.delete();
        }
        try (OutputStreamWriter ecriture =
                     new OutputStreamWriter(new FileOutputStream(JsonParser.PATH + filename), StandardCharsets.UTF_8)) {
            ecriture.write(json);
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
}
