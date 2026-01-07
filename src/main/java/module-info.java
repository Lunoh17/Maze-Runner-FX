module ve.edu.ucab.mazerunnerfx.mazerunnerfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.google.gson;

    opens ve.edu.ucab.mazerunnerfx to javafx.fxml;
    exports ve.edu.ucab.mazerunnerfx;
}