package com.insa.coliffimo;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        autoDownloadMap();
        stage.getIcons().add(new Image("file:src/main/resources/img/logo.png"));
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("coliffimo.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setMaximized(true);
        stage.setTitle("Coliffimo");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        System.setProperty("javafx.platform", "desktop");
        System.setProperty("http.agent", "Gluon Mobile/1.0.3");
        launch();
    }

    public static void autoDownloadMap() {
        // auto download map if file does not exists
        String map_file_path = "./resources/rhone-alpes-latest.osm.pbf";
        File rhone_alpes_map = new File(map_file_path);
        rhone_alpes_map.mkdirs();
        if (!rhone_alpes_map.exists()) {
            System.out.println("Téléchargement de la carte en cours...");

            InputStream inputStream = null;
            try {
                inputStream = new URL("http://download.geofabrik.de/europe/france/rhone-alpes-latest.osm.pbf").openStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileOutputStream fileOS = null;
            try {
                fileOS = new FileOutputStream(map_file_path);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Long i = null;
            try {
                i = IOUtils.copyLarge(inputStream, fileOS);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(i);
            System.out.println("Téléchargement de la carte terminé");
        }
    }
}