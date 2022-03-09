module com.insa.coliffimo {
    requires javafx.controls;
    requires javafx.fxml;
    requires MaterialFX;


    opens com.insa.coliffimo to javafx.fxml;
    exports com.insa.coliffimo;
}