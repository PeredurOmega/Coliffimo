package com.insa.coliffimo.utils;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class MapDownloadThread implements Runnable {
    VBox root;
    Label label;
    ProgressIndicator progress;
    String mapFilePath;
    Stage mapDownloadStage;

    public MapDownloadThread(Stage mapDownloadStage, VBox root, Label label, ProgressIndicator progress, String mapFilePath){
        this.root = root;
        this.label = label;
        this.progress = progress;
        this.mapFilePath = mapFilePath;
        this.mapDownloadStage = mapDownloadStage;
    }

    @Override
    public void run() {
        System.out.println("Téléchargement de la carte en cours...");

        InputStream inputStream = null;
        try {
            inputStream = new URL("http://download.geofabrik.de/europe/france/rhone-alpes-latest.osm.pbf").openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileOutputStream fileOS = null;
        try {
            fileOS = new FileOutputStream(mapFilePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Long i = null;
        try {
            assert inputStream != null;
            i = IOUtils.copyLarge(inputStream, fileOS);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(i);
        System.out.println("Téléchargement de la carte terminé");
        Platform.runLater(() -> {
            label.setText("La fenêtre va se fermer automatiquement");
            progress.setProgress(100);
            delay(() -> mapDownloadStage.close());
        });
    }

    private static void delay(Runnable continuation) {
        Task<Void> sleeper = new Task<>() {
            @Override
            protected Void call() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignored) {
                }
                return null;
            }
        };
        sleeper.setOnSucceeded(event -> continuation.run());
        new Thread(sleeper).start();
    }
}
