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
import java.util.Base64;

public class MainScreen extends JFrame implements ActionListener {
    
    private static final long serialVersionUID = 1L;
    private JLabel timeLabel;
    private JToggleButton playButton;
    static Timer timer;
    private static Timer timer1;
    private int secondsCount = 0;

    private static long lastCaptureTime = 0;

    public MainScreen() {
        super("HubStaff");


        timeLabel = new JLabel("00:00:00");
        timeLabel.setFont(new Font("Arial", Font.BOLD, 40));
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timeLabel.setOpaque(true); // enable background color
        timeLabel.setBackground(Color.CYAN);
        timeLabel.setPreferredSize(new Dimension(40, 40));

        // Create the play button with a music icon
        playButton = new JToggleButton(new ImageIcon("src\\main\\resources\\PlayButton.png"),false);
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

        // Initialize the timer
        timer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                secondsCount++;
                updateTimeLabel();
            }
        });
        timer.setInitialDelay(0);
    }

    public void actionPerformed(@NotNull ActionEvent e) {
        if (e.getSource() == playButton) {
            if (timer.isRunning()) {
                // Pause the timer
                timer.stop();
                stopCapturing();
                notification("Your time has been paused!");
            } else {
                // Resume the timer
                timer.start();
                startCapturing();
                // Show the time notification
                notification("Your time has started!");
            }
        }
    }


    private void updateTimeLabel() {
        int hours = secondsCount / 3600;
        int minutes = (secondsCount % 3600) / 60;
        int seconds = secondsCount % 60;
        String hoursStr = (hours < 10 ? "0" : "") + hours;
        String minutesStr = (minutes < 10 ? "0" : "") + minutes;
        String secondsStr = (seconds < 10 ? "0" : "") + seconds;
        timeLabel.setText(hoursStr + ":" + minutesStr + ":" + secondsStr);
    }

    static void startCapturing() {
        // Create the Timer to take screenshots at the specified interval
        timer1 = new Timer(10000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    captureScreenAndEncodeToBase64();
                    notification("Screenshot taken!");
                } catch (AWTException | IOException ex) {
                    ex.printStackTrace();
                    System.out.println("Screenshot is taken");
                }
            }
        });
        timer1.start();
    }

    static void captureScreenAndEncodeToBase64() throws AWTException, IOException {
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage screenshot = new Robot().createScreenCapture(screenRect);

        // Encode the screenshot to Base64
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(screenshot, "png", outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        String base64Encoded = Base64.getEncoder().encodeToString(imageBytes);
        post(base64Encoded);


    }
    static void post(String base64Encoded) {
        try {
            URL url = new URL("http://127.0.0.1:8000/api/take_screen_shot");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            JSONObject json = new JSONObject();
            json.put("screenshot", base64Encoded);
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
    static void notification(String msg) 
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
            notification.displayMessage(msg , "", TrayIcon.MessageType.INFO);
        } else {
            System.out.println("System tray is not supported");
        }
    }

    private static void stopCapturing() {
        if (timer1 != null) {
            timer1.stop();
            lastCaptureTime = System.currentTimeMillis(); // Record the time when the timer was stopped
        }
    }


    public void start() {
        start();
    }

    public static void main(String[] args) {
        // Create and show the GUI
        MainScreen frame = new MainScreen();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 700);
        frame.setVisible(true);
    }
}

