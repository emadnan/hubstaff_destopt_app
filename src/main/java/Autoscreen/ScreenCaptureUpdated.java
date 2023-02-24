package Autoscreen;

import javax.imageio.ImageIO;
import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ScreenCaptureUpdated {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");
    private static final String SERVER_URL = "http://example.com/screenshots/";

    private static int interval = 3; // 30 seconds in milliseconds
    private static Timer timer;
    private static Date startTime;
    private static Date endTime;

    public static void main(String[] args) {
        startCapturing();
    }

    private static void startCapturing() {
        startTime = new Date();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    captureScreenAndSendToServer();
                } catch (AWTException | IOException ex) {
                    ex.printStackTrace();
                }
            }
        }, 0, interval);
    }

    private static void stopCapturing() {
        endTime = new Date();
        timer.cancel();
        timer.purge();

        // TODO: Send the start time, end time, and other metadata to the server
    }

    private static void captureScreenAndSendToServer() throws AWTException, IOException {
        // Capture the screen using the Robot class
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage screenshot = new Robot().createScreenCapture(screenRect);

        // Convert the screenshot to a byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(screenshot, "png", baos);
        byte[] imageData = baos.toByteArray();

        // Create a new HTTP connection to the server and send the screenshot
        URL url = new URL(SERVER_URL + DATE_FORMAT.format(new Date()) + ".png");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "image/png");
        conn.setRequestProperty("Content-Length", String.valueOf(imageData.length));

        try (OutputStream out = conn.getOutputStream()) {
            out.write(imageData);
        }

        // Check the response code for errors
        int responseCode = conn.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            throw new IOException("HTTP error: " + responseCode);
        }

        conn.disconnect();
    }
}
