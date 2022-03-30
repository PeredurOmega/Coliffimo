module com.insa.coliffimo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;
    requires MaterialFX;
    requires jsprit.core;
    requires kotlin.stdlib;
    requires graphhopper.web;
    requires json;

    opens com.insa.coliffimo to javafx.fxml;
    opens com.insa.coliffimo.leaflet to javafx.fxml;
    exports com.insa.coliffimo;
}