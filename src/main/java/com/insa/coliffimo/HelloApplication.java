package com.insa.coliffimo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        //stage.setMaximized(true);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {

        System.setProperty("javafx.platform", "desktop");
        System.setProperty("http.agent", "Gluon Mobile/1.0.3");
        /*
        // define service for desktop
        StorageService storageService = new StorageService() {
            @Override
            public Optional<File> getPrivateStorage() {
                // user home app config location (linux: /home/[yourname]/.gluonmaps)
                return Optional.of(new File(System.getProperty("user.home")));
            }

            @Override
            public Optional<File> getPublicStorage(String subdirectory) {
                // this should work on desktop systems because home path is public
                return getPrivateStorage();
            }

            @Override
            public boolean isExternalStorageWritable() {
                //noinspection ConstantConditions
                return getPrivateStorage().get().canWrite();
            }

            @Override
            public boolean isExternalStorageReadable() {
                //noinspection ConstantConditions
                return getPrivateStorage().get().canRead();
            }
        };

        // define service factory for desktop
        ServiceFactory<StorageService> storageServiceFactory = new ServiceFactory<StorageService>() {

            @Override
            public Class<StorageService> getServiceType() {
                return StorageService.class;
            }

            @Override
            public Optional<StorageService> getInstance() {
                return Optional.of(storageService);
            }

        };
        // register service
        Services.registerServiceFactory(storageServiceFactory);*/
        launch();
    }
}