module com.insa.coliffimo {
    requires javafx.controls;
    requires javafx.fxml;
    requires MaterialFX;
    requires virtualizedfx;


    opens com.insa.coliffimo to javafx.fxml;
    exports com.insa.coliffimo;
}