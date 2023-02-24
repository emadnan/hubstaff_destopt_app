package Autoscreen;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TimerExample extends Application {

    private int seconds = 0;
    private int minutes = 0;
    private int hours = 0;
    private boolean isRunning = false;
    private Timeline timeline;

    @Override
    public void start(Stage primaryStage) {
        Text timeText = new Text("00:00:00");
        timeText.setFont(new Font(40));
        timeText.setStyle("-fx-fill: green;");

        Button startButton = new Button("Start");
        startButton.setStyle("-fx-background-color: green;");

        startButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!isRunning) {
                    timeline.play();
                    isRunning = true;
                    startButton.setText("Stop");
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Timer");
                    alert.setHeaderText(null);
                    alert.setContentText("Timer started!");
                    alert.show();
                    scheduleCloseAlert(alert);
                } else {
                    timeline.pause();
                    isRunning = false;
                    startButton.setText("Start");
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Timer");
                    alert.setHeaderText(null);
                    alert.setContentText("Timer stopped!");
                    alert.show();
                    scheduleCloseAlert(alert);
                }
            }
        });

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                seconds++;
                if (seconds == 60) {
                    seconds = 0;
                    minutes++;
                    if (minutes == 60) {
                        minutes = 0;
                        hours++;
                    }
                }
                String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                timeText.setText(timeString);

                // Take a screenshot and send it to an API every 10 seconds
                if (seconds % 10 == 0) {
                    try {
                        Robot robot = new Robot();
                        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
                        BufferedImage screenCapture = robot.createScreenCapture(screenRect);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(screenCapture, "png", baos);
                        byte[] imageData = baos.toByteArray();

                        URL url = new URL("http://127.0.0.1:8000/api/screen_shot");
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Content-Type", "image/png");
                        connection.setDoOutput(true);
                        OutputStream os = connection.getOutputStream();
                        os.write(imageData);
                        os.flush();
                        os.close();
                        int responseCode = connection.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            System.out.println("Screenshot sent successfully.");
                            Alert alert = new Alert(AlertType.INFORMATION);
                            alert.setTitle("Screenshot");
                            alert.setHeaderText(null);
                            alert.setContentText("Screenshot taken!");
                            alert.show();
                            scheduleCloseAlert(alert);
                        } else {
                            System.out.println("Failed to send screenshot. Response code: " + responseCode);
                        }
                    } catch (Exception e) {
                        System.out.println("Error taking screenshot or sending to API: " + e.getMessage());
                    }
                }
            }
        }));

        timeline.setCycleCount(Animation.INDEFINITE);

        VBox root = new VBox(20); // 20 is the spacing between elements
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(timeText, startButton);
        Scene scene = new Scene(root, 400, 400);

        primaryStage.setTitle("Timer Example");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void scheduleCloseAlert(Alert alert) {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
            alert.hide();
        }));
        timeline.play();
    }
}
