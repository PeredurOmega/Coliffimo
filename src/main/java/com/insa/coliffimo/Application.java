package com.insa.coliffimo;

import com.insa.coliffimo.utils.MapDownloadThread;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException{
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

    public static void autoDownloadMap(){
        // auto download map if file does not exists
        String map_file_path = "./resources/rhone-alpes-latest.osm.pbf";
        File rhone_alpes_map = new File(map_file_path);
        rhone_alpes_map.getParentFile().mkdirs();
        if (!rhone_alpes_map.exists()) {
            Stage mapDownloadStage = new Stage();
            mapDownloadStage.setTitle("Téléchargement de la carte");
            mapDownloadStage.getIcons().add(new Image("file:src/main/resources/img/logo.png"));
            mapDownloadStage.initStyle(StageStyle.UNDECORATED);
            ProgressIndicator progressBar = new ProgressIndicator();
            Label downloadLabel = new Label("Téléchargement de la carte en cours...");
            VBox root = new VBox(downloadLabel, progressBar);
            root.setAlignment(Pos.CENTER);
            Scene scene = new Scene(root, 350, 75);
            mapDownloadStage.setScene(scene);
            new Thread(new MapDownloadThread(mapDownloadStage, root, downloadLabel, progressBar, map_file_path)).start();
            mapDownloadStage.showAndWait();

        }
    }
}