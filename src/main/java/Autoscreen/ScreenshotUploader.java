package Autoscreen;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenshotUploader {
    private static long totalElapsedTime = 0;
    private static long lastCaptureTime = 0;

    public static void main(String[] args) throws AWTException, IOException {
        captureScreenAndUpload();
    }

    static void captureScreenAndUpload() throws AWTException, IOException {
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage screenshot = new Robot().createScreenCapture(screenRect);

        // Compute the timestamp based on the total elapsed time
        long currentTime = System.currentTimeMillis();
        totalElapsedTime += currentTime - lastCaptureTime;
        lastCaptureTime = currentTime;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String timestamp = dateFormat.format(new Date(totalElapsedTime));
        String fileName = "screenshot_" + timestamp + ".png";

        // Create the HTTP client and request
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("https://jsonplaceholder.typicode.com/posts");

        // Build the multipart request body
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(screenshot, "png", baos);
        HttpEntity requestEntity = MultipartEntityBuilder.create()
                .addBinaryBody("file", baos.toByteArray(), ContentType.DEFAULT_BINARY, fileName)
                .build();
        httpPost.setEntity(requestEntity);

        // Execute the request and handle the response
        CloseableHttpResponse response = httpClient.execute(httpPost);
        try {
            System.out.println(response.getStatusLine());
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                System.out.println(responseEntity.getContent());
            }
        } finally {
            response.close();
        }
    }
}

