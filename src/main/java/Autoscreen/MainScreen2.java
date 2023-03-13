package Autoscreen;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.json.JSONArray;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
public class MainScreen2 extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;
    private JLabel timeLabel;
    private JLabel projectLabel;
    private JToggleButton playButton;
    private static Timer timer;
    private ScheduledExecutorService scheduler;
    private int secondsCount = 0;
    private static int is_stop = 0;
    private static volatile boolean isCapturing = true;
    private static ArrayList<String> screenshotList2 = new ArrayList<>();
    private static ArrayList<String> projects = new ArrayList<>();

    private static long lastCaptureTime = 0;
    private static final String API_KEY = "8a3456acc27c4336a570379b2f7e849d";


    private static final String API_URL = "http://localhost:8080/screenshots";

    public MainScreen2(Integer user_id, String auth_token) {
        super("HubStaff");
        getProjects(user_id);
        initializeUI();
        initializeTimers();
        
    }

    private void initializeUI() {
        timeLabel = new JLabel("00:00:00");
        projectLabel = new JLabel("Project");
        timeLabel.setFont(new Font("Arial", Font.BOLD, 30));
        projectLabel.setFont(new Font("Arial", Font.BOLD, 20));
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timeLabel.setOpaque(true);
        timeLabel.setBackground(new java.awt.Color(51, 102, 255));
        timeLabel.setForeground(new java.awt.Color(255, 255, 255));
        timeLabel.setPreferredSize(new Dimension(40, 40));
        //  String[] items = {"Item 1", "Item 2", "Item 3"};
        JComboBox<String> dropdown = new JComboBox<>(projects.toArray(new String[0]));
        // JComboBox<String> dropdown = new JComboBox<>(items);
        dropdown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedItem = (String) dropdown.getSelectedItem();
                //set project label to selected item
                projectLabel.setText(selectedItem);
            }
        });
        dropdown.setPreferredSize(new Dimension(100, 20));
        dropdown.setForeground(new java.awt.Color(51, 102, 255));
        dropdown.setBackground(new java.awt.Color(255, 255, 255));
        playButton = new JToggleButton(new ImageIcon("src\\main\\resources\\PlayButton3.png"), false);
        playButton.setSelectedIcon(new ImageIcon("src\\main\\resources\\stop2.png"));
        playButton.setBorderPainted(false);
        playButton.setContentAreaFilled(false);
        playButton.setFocusPainted(false);
        playButton.addActionListener(this);
       

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        GridBagConstraints d = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        d.gridy = 0;
        d.gridx = 0;
        c.anchor = GridBagConstraints.CENTER;
        d.anchor = GridBagConstraints.CENTER;
        buttonsPanel.add(playButton, c);
        c.gridy = 3;
        d.gridy = 0;
        buttonsPanel.add(projectLabel, d);
        buttonsPanel.add(dropdown, c);

        int margin =40;
        add(timeLabel, BorderLayout.NORTH);
        add(buttonsPanel, BorderLayout.CENTER);
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(margin, margin, margin, margin));
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
                isCapturing = false; // Stop capturing screenshots
                is_stop = 1;
                timer.stop();
                notification("Your time has been paused!");
            } else {
                is_stop = 0;
                isCapturing = true; // Start capturing screenshots
                timer.start();
                startCapturing();
                notification("Your time has started!");
                String location = null; // Get the location of the system
                try {
                    location = getLocalIpAddress();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
                System.out.println(location);
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
            try {
                getLocalIpAddress();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            int numScreenshots = 3; // Number of screenshots to capture
            int targetInterval = 1 * 60 * 1000 / numScreenshots; // Target interval between screenshots (in milliseconds)
            int minInterval = targetInterval / 3; // Minimum interval between screenshots (in milliseconds)
            int maxInterval = targetInterval * 3 / 3; // Maximum interval between screenshots (in milliseconds)
            java.util.List<String> screenshotList2 = new ArrayList<>();
            long pauseStart = 0; // Time when the screenshot capture is paused
            long totalPauseTime = 0; // Total time that the screenshot capture is paused

            try {
                Robot robot = new Robot();
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                Rectangle screenRect = new Rectangle(screenSize);

                scheduler = Executors.newSingleThreadScheduledExecutor();
                scheduler.scheduleAtFixedRate(() -> {
                    if (!screenshotList2.isEmpty()) {
                        sendScreenshots(screenshotList2);
                        screenshotList2.clear();
                        notification("API called");
                    }
                }, 0, 1, TimeUnit.MINUTES); // Schedule the API call to run every minute

                long startTime = System.currentTimeMillis();
                while (isCapturing) {
                    if (is_stop == 0) { // Only take screenshots if isCapturing and is_stop are both true
                        long remainingTime = targetInterval - (System.currentTimeMillis() - startTime - totalPauseTime) % targetInterval;
                        for (int i = 0; i < numScreenshots; i++) {
                            long interval = minInterval + new Random().nextInt(maxInterval - minInterval);
                            Thread.sleep(interval);
                            if (isCapturing && is_stop == 0) { // Take a screenshot only if isCapturing and is_stop are both true
                                BufferedImage image = robot.createScreenCapture(screenRect);
                                String base64String = convertImageToBase64(image);
                                screenshotList2.add(base64String);
                                notification("Screenshot Captured");
                                System.out.println("Screenshot captured: " + i);
                            }
                        }

                        Thread.sleep(remainingTime); // Wait for the remaining time before taking the next set of screenshots
                    } else {
                        if (pauseStart == 0) { // If the screenshot capture is paused, record the time
                            pauseStart = System.currentTimeMillis();
                        }
                        Thread.sleep(1000); // Wait 1 second before checking again if isCapturing is true
                    }
                }
                scheduler.shutdown(); // Cancel the scheduled API call

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
    private void getProjects(Integer user_id){
        try {
            URL url = new URL("http://127.0.0.1:8000/api/get-project-by-user/"+user_id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
        
            // Get the response code
            int responseCode = conn.getResponseCode();
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Get the response body
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
        
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
        
                in.close();
                JSONObject resposeData = new JSONObject(response.toString());
                JSONArray  projectsArray = resposeData.getJSONArray("projects");
                for (int i = 0; i < projectsArray.length(); i++) {
                    JSONObject project = projectsArray.getJSONObject(i);
                    String project_name = project.getString("project_name");
                    projects.add(project_name);
                }
                System.out.println(projects);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    public void sendScreenshots(java.util.List<String> screenshotList2) {
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


        public static String getLocalIpAddress() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "";
        }
    }
    private static void notification(String message) {
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
    static void stopCapturing() {

        if (timer != null) {

            timer.stop();
            lastCaptureTime = System.currentTimeMillis(); // Record the time when the timer was stopped
        }
    }
    public void start() {
        start();
    }

    public static void main(String[] args) {
        MainScreen2 mainScreen2 = new MainScreen2(1, "Rafay");
        mainScreen2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainScreen2.setSize(400, 700);
        mainScreen2.setVisible(true);

    }
}