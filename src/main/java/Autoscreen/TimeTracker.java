package Autoscreen;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class TimeTracker extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;
    private final int INTERVAL = 10 * 60 * 1000; // 10 minutes in milliseconds
    private final int TOTAL_SCREENSHOTS = 3;
    private int screenshotsTaken = 0;
    private Timer timer;
    private JButton playButton;
    private JLabel timeLabel;

    private int secondsCount = 0;

    private Random random = new Random();


    public TimeTracker() {
        super("Time Counter");
        // Create and add components to the frame
        timeLabel = new JLabel("00:00:00");
        timeLabel.setFont(new Font("Arial", Font.BOLD, 40));
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        // Create the play button with a music icon
        playButton = new JButton(new ImageIcon("F:\\Rajkumar bala\\PlayButton.png"));
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
                takeScreenshotsAtRandomTime();
            }
        });
        timer.setInitialDelay(0);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == playButton) {
            if (timer.isRunning()) {
                // Pause the timer
                timer.stop();
                playButton.setText("Play");
            } else {
                // Resume the timer
                timer.start();
                playButton.setText("Pause");
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



    private void takeScreenshotsAtRandomTime() {
        if (timer == null || !timer.isRunning()) {
            // Start the timer if it's not already running
            timer.start();
        } else {
            // Cancel the current timer and start a new one
            timer.stop();
            timer = new Timer(1000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    secondsCount += 1;
                }
            });
            timer.setInitialDelay(0);
            timer.start();
        }

        // Check if the interval has elapsed
        if (secondsCount >= INTERVAL / 1000) {
            // Reset the secondsCount variable
            secondsCount = 0;
            screenshotsTaken = 0;

            // Take 3 screenshots
            for (int i = 0; i < TOTAL_SCREENSHOTS; i++) {
                // Generate a random delay between 1 and 60 seconds
                int randomSeconds = ThreadLocalRandom.current().nextInt(1, 61);

                // Schedule the screenshot to be taken after the random delay
                Timer screenshotTimer = new Timer(randomSeconds * 1000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // Take a screenshot and upload it to the server
                        try {
                            captureScreenAndUpload();
                            screenshotsTaken++;
                            if (screenshotsTaken >= TOTAL_SCREENSHOTS) {
                                // Cancel the timer after taking the required number of screenshots
                                ((Timer) e.getSource()).stop();
                            }
                        } catch (AWTException | IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                screenshotTimer.setInitialDelay(0);
                screenshotTimer.start();
            }
        }
    }


    private void captureScreenAndUpload() throws AWTException, IOException {
        // Capture the screen using the Robot class
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage screenshot = new Robot().createScreenCapture(screenRect);

        // Convert the screenshot to a byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(screenshot, "png", baos);
        byte[] imageData = baos.toByteArray();

        // Upload the screenshot to the server using the Autoscreen API
        AutoscreenClient client = new AutoscreenClient("<your API key>", "<your API secret>");
        String url = client.uploadScreenshot(imageData);

        JOptionPane.showMessageDialog(null, "Screenshot uploaded to " + url);
    }


    public static void main(String[] args) {
        // Create and show the GUI
        TimeTracker counter = new TimeTracker();
        counter.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        counter.setSize(400, 200);
        counter.setVisible(true);

    }
}