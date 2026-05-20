package com.example.cloudvmxclient;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HelloController extends Application {

    private Stage window;
    private Scene loginScene, dashboardScene;
    private VBox vmContainer; // Container for VM cards
    private Label connectionStatus;

    // HTTP Client for API calls
    private final HttpClient client = HttpClient.newHttpClient();
    private final String API_URL = "http://localhost:8080/api/vms";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        window.setTitle("CloudVMX - Intelligent Cloud Virtual Machine");

        // Bypass login: go straight to the dashboard
        createDashboardScene();
        window.setScene(dashboardScene);
        window.show();
        initializeBackend();
        refreshVMList();
    }

    // --- 1. LOGIN SCREEN ---
    private void createLoginScene() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(40, 40, 40, 40));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-background-color: #2D3447;");

        Label headerLabel = new Label("CloudVMX Login");
        headerLabel.setTextFill(Color.WHITE);
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        grid.add(headerLabel, 0, 0, 2, 1);

        TextField userField = new TextField();
        userField.setPromptText("admin1234");
        PasswordField passField = new PasswordField();
        passField.setPromptText("admin@1234");

        Button loginButton = new Button("Access Cloud");
        loginButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");

        loginButton.setOnAction(e -> {
            createDashboardScene();
            window.setScene(dashboardScene);
            refreshVMList(); // Load data from backend
        });

        grid.add(userField, 0, 1);
        grid.add(passField, 0, 2);
        grid.add(loginButton, 0, 3);

        loginScene = new Scene(grid, 400, 300);
    }

    // --- 2. DASHBOARD SCREEN ---
    private void createDashboardScene() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #ECEFF1;");

        // Top Header
        HBox topMenu = new HBox();
        topMenu.setPadding(new Insets(15, 12, 15, 12));
        topMenu.setSpacing(10);
        topMenu.setStyle("-fx-background-color: #2D3447;");
        Label title = new Label("CloudVMX Controller");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        topMenu.getChildren().add(title);

        // Center: VM List
        vmContainer = new VBox(15);
        vmContainer.setPadding(new Insets(20));
        ScrollPane scrollPane = new ScrollPane(vmContainer);
        scrollPane.setFitToWidth(true);

        // Bottom: Status
        HBox statusBar = new HBox(20);
        statusBar.setPadding(new Insets(10));
        statusBar.setStyle("-fx-background-color: #CFD8DC;");
        connectionStatus = new Label("Status: Connecting...");
        statusBar.getChildren().addAll(connectionStatus, new Label("User: Luxman Kumar"));

        mainLayout.setTop(topMenu);
        mainLayout.setCenter(scrollPane);
        mainLayout.setBottom(statusBar);

        dashboardScene = new Scene(mainLayout, 800, 600);
    }

    // --- 3. LOGIC TO FETCH DATA ---
    private void refreshVMList() {
        vmContainer.getChildren().clear();
        vmContainer.getChildren().add(new Label("Loading Cloud Instances..."));

        // Creating dummy UI for demo immediately (simulating backend fetch for smooth UI)
        Platform.runLater(() -> {
            vmContainer.getChildren().clear();
            vmContainer.getChildren().add(new Label("Available Cloud Instances:"));

            // In a full implementation, you would parse the JSON response here.
            // For this prototype, we will manually create the cards that match the Backend data.
            vmContainer.getChildren().add(createVMCard("vm-001", "Windows 11 Enterprise", "Running", "Standard_D2s_v3", "Windows"));
            vmContainer.getChildren().add(createVMCard("vm-002", "Android 14 (Pixel Clone)", "Stopped", "ARM64_v8", "Android"));
        });
    }

    private void initializeBackend() {
        new Thread(() -> {
            try {
                HttpRequest healthReq = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL + "/health"))
                        .timeout(java.time.Duration.ofSeconds(5))
                        .GET()
                        .build();

                HttpResponse<String> resp = client.send(healthReq, HttpResponse.BodyHandlers.ofString());

                Platform.runLater(() -> {
                    if (resp.statusCode() == 200) {
                        System.out.println("[Frontend] Backend connected: " + resp.body());
                        connectionStatus.setText("Status: Connected");
                        connectionStatus.setTextFill(Color.GREEN);
                    } else {
                        System.out.println("[Frontend] Backend error: " + resp.statusCode());
                        connectionStatus.setText("Status: Error (" + resp.statusCode() + ")");
                        connectionStatus.setTextFill(Color.RED);
                    }
                });
            } catch (Exception e) {
                System.err.println("[Frontend] Backend offline: " + e.getMessage());
                Platform.runLater(() -> {
                    connectionStatus.setText("Status: Offline");
                    connectionStatus.setTextFill(Color.RED);
                });
            }
        }, "backend-checker").start();
    }

    private HBox createVMCard(String id, String vmName, String status, String specs, String type) {
        HBox card = new HBox(20);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0); -fx-background-radius: 5;");
        card.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(type.equals("Windows") ? "[WIN]" : "[DROID]");
        icon.setFont(Font.font("Monospaced", FontWeight.BOLD, 20));
        icon.setMinWidth(60);

        VBox info = new VBox(5);
        Label nameLbl = new Label(vmName);
        nameLbl.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        Label statusLbl = new Label("Status: " + status);
        statusLbl.setTextFill(status.equals("Running") ? Color.GREEN : Color.RED);
        info.getChildren().addAll(nameLbl, statusLbl, new Label("Config: " + specs));

        HBox controls = new HBox(10);
        Button btnStart = new Button("Start");
        Button btnStop = new Button("Stop");

        // Button Logic: Call Backend
        btnStart.setOnAction(e -> sendCommand(id, "start", statusLbl));
        btnStop.setOnAction(e -> sendCommand(id, "stop", statusLbl));

        controls.getChildren().addAll(btnStart, btnStop);
        controls.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(controls, Priority.ALWAYS);

        card.getChildren().addAll(icon, info, controls);
        return card;
    }

    // Inside HelloController.java

    // Paste this into HelloController.java (Replace the old sendCommand)
    private void sendCommand(String id, String action, Label statusLabel) {
        String fullUrl = API_URL + "/" + id + "/" + action;
        System.out.println("Sending: " + fullUrl);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> Platform.runLater(() -> {
                    String reply = response.body();
                    System.out.println("Backend Reply: " + reply);

                    if (response.statusCode() == 200 && reply.contains("SUCCESS")) {
                        if (action.equals("start")) {
                            statusLabel.setText("Status: Running");
                            statusLabel.setTextFill(Color.GREEN);

                            // --- MAGIC PART: Open Terminal Window ---
                            try {
                                // 1. Extract the Container ID from the response
                                // Reply format: "SUCCESS: VM Started (abc12345...)"
                                String containerId = reply.substring(reply.indexOf("(") + 1, reply.indexOf(")"));

                                System.out.println("Connecting to Cloud VM ID: " + containerId);

                                // 2. Open a new Windows CMD window connected to the VM
                                // "docker exec -it <ID> sh" logs you into Alpine Linux
                                String command = "cmd /c start cmd.exe /k \"docker exec -it " + containerId + " sh\"";

                                Runtime.getRuntime().exec(command);

                            } catch (Exception e) {
                                System.err.println("Failed to open terminal: " + e.getMessage());
                            }
                            // ----------------------------------------

                        } else {
                            statusLabel.setText("Status: Stopped");
                            statusLabel.setTextFill(Color.RED);

                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("CloudVMX");
                            alert.setHeaderText("Instance Stopped");
                            alert.setContentText("The VM has been shut down successfully.");
                            alert.show();
                        }
                    } else {
                        statusLabel.setText("Status: Error");
                        statusLabel.setTextFill(Color.ORANGE);
                    }
                }))
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        statusLabel.setText("Status: Offline");
                        statusLabel.setTextFill(Color.RED);
                    });
                    return null;
                });
    }}
