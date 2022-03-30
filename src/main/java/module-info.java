module com.insa.coliffimo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;
    requires MaterialFX;
    requires jsprit.core;
    requires kotlin.stdlib;
    requires graphhopper.web;
    requires jdk.jsobject;

    opens com.insa.coliffimo to javafx.fxml;
    opens com.insa.coliffimo.leaflet to javafx.fxml, javafx.web;
    exports com.insa.coliffimo;
    exports com.insa.coliffimo.leaflet;
}