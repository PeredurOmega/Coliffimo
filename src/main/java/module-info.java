module com.insa.coliffimo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;
    requires MaterialFX;
    requires com.dlsc.gmapsfx;

    opens com.insa.coliffimo to javafx.fxml;
    exports com.insa.coliffimo;
}