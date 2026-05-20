module com.example.cloudvmxclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.net.http;
    // CRITICAL FIX: Allow JavaFX to see your App and Controller
    opens com.example.cloudvmxclient to javafx.fxml, javafx.graphics;

    exports com.example.cloudvmxclient;

}