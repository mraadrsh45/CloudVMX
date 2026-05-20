package com.example.cloudvmxclient;

import javafx.application.Application;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) {
        // Use the programmatic UI from HelloController instead of FXML.
        // This avoids runtime errors from the sample FXML controller bindings.
        new HelloController().start(stage);
    }
}
