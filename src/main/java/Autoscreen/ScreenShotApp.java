package Autoscreen;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScreenShotApp extends Application {

    private static final String REST_API_URL = "http://127.0.0.1:8000/api/screen_shot";
    private static final int INTERVAL_SECONDS = 10;

    private ScheduledExecutorService executor;
    private ProgressBar progressBar;
    private Button startButton, stopButton;
    private Label statusLabel;

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Screenshot App");

        BorderPane root = new BorderPane();

        progressBar = new ProgressBar();
        progressBar.setProgress(0);

        startButton = new Button("Start");
        startButton.setOnAction(event -> startTimer());

        stopButton = new Button("Stop");
        stopButton.setDisable(true);
        stopButton.setOnAction(event -> stopTimer());

        statusLabel = new Label("Ready");

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setSpacing(10);
        buttonBox.getChildren().addAll(startButton, stopButton);

        VBox centerBox = new VBox();
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setSpacing(10);
        centerBox.getChildren().addAll(progressBar, buttonBox, statusLabel);

        root.setCenter(centerBox);

        Scene scene = new Scene(root, 300, 150);
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    private void startTimer() {
        statusLabel.setText("Running");
        startButton.setDisable(true);
        stopButton.setDisable(false);

        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    takeScreenshotAndSend();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (AWTException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }, 0, INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private void stopTimer() {
        statusLabel.setText("Stopped");
        startButton.setDisable(false);
        stopButton.setDisable(true);

        if (executor != null) {
            executor.shutdownNow();
        }
    }

    private void takeScreenshotAndSend() throws IOException, AWTException {
        Robot robot = new Robot();
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage image = robot.createScreenCapture(screenRect);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] bytes = baos.toByteArray();

        URL url = new URL(REST_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content Type", "image/png");
        conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));
        conn.getOutputStream().write(bytes);
        if (conn.getResponseCode() == 200) {
            Platform.runLater(() -> {
                progressBar.setProgress(progressBar.getProgress() + 0.1);
            });
        }

        conn.disconnect();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

