module com.insa.coliffimo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires MaterialFX;
    requires com.sothawo.mapjfx;


    opens com.insa.coliffimo to javafx.fxml;
    exports com.insa.coliffimo;
}