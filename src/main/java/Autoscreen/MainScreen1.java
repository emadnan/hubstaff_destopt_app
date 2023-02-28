package Autoscreen;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;

public class MainScreen1 extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;
    private JLabel timeLabel;
    private JToggleButton playButton;
    private static Timer timer;
    private static Timer timer1;
    private static Timer apiTimer;
    private int secondsCount = 0;
    private static ArrayList<String> screenshotList = new ArrayList<>();
    private static int is_stop=0;
    private static long lastCaptureTime = 0;

    private static final String API_URL = "http://your.api.url/screenshots";
    public MainScreen1() {
        super("HubStaff 1");
        initializeUI();
        initializeTimers();
    }

    private void initializeUI() {
        timeLabel = new JLabel("00:00:00");
        timeLabel.setFont(new Font("Arial", Font.BOLD, 40));
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timeLabel.setOpaque(true);
        timeLabel.setBackground(Color.CYAN);
        timeLabel.setPreferredSize(new Dimension(40, 40));

        playButton = new JToggleButton(new ImageIcon("src\\main\\resources\\PlayButton.png"), false);
        playButton.setSelectedIcon(new ImageIcon("src\\main\\resources\\stop.png"));
        playButton.setBorderPainted(false);
        playButton.setContentAreaFilled(false);
        playButton.setFocusPainted(false);
        playButton.addActionListener(this);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout());
        buttonsPanel.add(playButton);

        add(timeLabel, BorderLayout.NORTH);
        add(buttonsPanel, BorderLayout.CENTER);
    }

    private void initializeTimers() {
        timer = new Timer(1000, e -> {
            secondsCount++;
            updateTimeLabel();
        });
        timer.setInitialDelay(0);
    }

    @Override
    public void actionPerformed(@NotNull ActionEvent e) {
        if (e.getSource() == playButton) {
            if (timer.isRunning()) {
                timer.stop();
                stopCapturing();
                notification("Your time has been paused!");
            } else {
                timer.start();
                is_stop=0;
                startCapturing();
                notification("Your time has started!");
            }
        }
    }

    private void updateTimeLabel() {
        int hours = secondsCount / 3600;
        int minutes = (secondsCount % 3600) / 60;
        int seconds = secondsCount % 60;
        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        timeLabel.setText(timeString);
    }

    private void startCapturing() {
       new Thread(() -> {
            int numScreenshots = 3; // Number of screenshots to capture
            int targetInterval = 10 * 60 * 1000 / numScreenshots; // Target interval between screenshots (in milliseconds)
            int minInterval = targetInterval / 3; // Minimum interval between screenshots (in milliseconds)
            int maxInterval = targetInterval * 3 / 3; // Maximum interval between screenshots (in milliseconds)
            java.util.List<String> screenshotList = new ArrayList<>();
            // boolean apiCalled = false; // Flag to track whether an API call has been made

            try {
                Robot robot = new Robot();
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                Rectangle screenRect = new Rectangle(screenSize);
               
                long startTime = System.currentTimeMillis();
                while (true) {
                    if(is_stop==1){
                        break;
                    }
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    if (elapsedTime >= 10 * 60 * 1000) { // Check if 1 minute has passed and an API call has not been made
                        sendScreenshots(screenshotList);
                        screenshotList.clear();
                        notification("API called");
                        // apiCalled = true; // Set flag to true after making the API call
                        startTime = System.currentTimeMillis(); // Reset the start time
                        elapsedTime = 0; // Reset the elapsed time
                    //    startCapturing();
                    }

                    for (int i = 0; i < numScreenshots; i++) {
                        if(is_stop==1){
                            break;
                        }
                        long interval = minInterval + new Random().nextInt(maxInterval - minInterval);
                        Thread.sleep(interval);
                        BufferedImage image = robot.createScreenCapture(screenRect);
                        String base64String = convertImageToBase64(image);
                        screenshotList.add(base64String);
                        notification("Screenshot Captured");
                        System.out.println("Screenshot captured: " + i);
                        System.out.println(is_stop);
                    }

                    long remainingTime = targetInterval - (System.currentTimeMillis() - startTime) % targetInterval;
                    Thread.sleep(remainingTime); // Wait for the remaining time before taking the next set of screenshots

                
            }
            } catch (AWTException | IOException | InterruptedException ex) {
                notification("Screenshot captured Failed");
            }
        }).start();
    }

    private String convertImageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }


    public void sendScreenshots(List<String> screenshotList2) {
        try {
            URL url = new URL("http://127.0.0.1:8000/api/take_screen_shot");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            JSONObject json = new JSONObject();
            json.put("screenshot", screenshotList2);
            String requestBody = json.toString();
//            String requestBody = "{\"screenshot\":\""+base64Encoded+"\"}";
            OutputStream os = con.getOutputStream();
            os.write(requestBody.getBytes());
            os.flush();
            os.close();

            int status = con.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            System.out.println("HTTP Status: " + status);
            System.out.println("Response: " + response.toString());

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void notification(String message) {
        {
            if (SystemTray.isSupported()) {
                // Get the system tray instance
                SystemTray tray = SystemTray.getSystemTray();
                // Create a notification icon
                Image icon = Toolkit.getDefaultToolkit().getImage("src\\main\\resources\\PlayButton.png");

                // Create a popup menu
                PopupMenu menu = new PopupMenu();
                MenuItem item = new MenuItem("Exit");
                //hide automatically after 3 seconds
                menu.add(item);

                // Create a notification object
                TrayIcon notification = new TrayIcon(icon, "Rafay", menu);

                // Add the notification to the system tray
                try {
                    tray.add(notification);
                } catch (AWTException e) {
                    e.printStackTrace();
                }

                // Display the notification message
                notification.displayMessage(message, "", TrayIcon.MessageType.INFO);
            } else {
                System.out.println("System tray is not supported");
            }
        }
    }
    private static void stopCapturing() {

        is_stop=1;
        if (timer1 != null) {
            timer1.stop();
            
            //stop Thread till start button is pressed
//            Thread.currentThread().interrupt();
            lastCaptureTime = System.currentTimeMillis(); // Record the time when the timer was stopped
        }
    }

    public void start() {
        start();
    }

    public static void main(String[] args) {
        MainScreen1 mainScreen1 = new MainScreen1();
        mainScreen1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainScreen1.setSize(400, 700);
        mainScreen1.setVisible(true);

    }
}