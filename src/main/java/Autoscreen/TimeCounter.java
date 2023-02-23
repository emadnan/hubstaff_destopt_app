package Autoscreen;

import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeCounter extends JFrame implements ActionListener {
    private static final long serialVersionUID = 1L;
    private static int interval = 10000; // 10 seconds in milliseconds
    private JLabel timeLabel;
    private JToggleButton playButton;
    static Timer timer;
    private static Timer timer1;
    private int secondsCount = 0;
    private static long totalElapsedTime = 0;
    private static long lastCaptureTime = 0;

    public TimeCounter() {
        super("HubStaff");


        timeLabel = new JLabel("00:00:00");
        timeLabel.setFont(new Font("Arial", Font.BOLD, 40));
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timeLabel.setOpaque(true); // enable background color
        timeLabel.setBackground(Color.BLUE);
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
                // Show the time notification
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                JOptionPane.showMessageDialog(null, "Your time has been paused!", "Timer", JOptionPane.INFORMATION_MESSAGE);
                            }
                        });
                    }
                }).start();
            } else {
                // Resume the timer
                timer.start();
                startCapturing();
                // Show the time notification
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                JOptionPane.showMessageDialog(null, "Your time has started!", "Timer", JOptionPane.INFORMATION_MESSAGE);
                            }
                        });
                    }
                }).start();
            }

            // Create a new thread to close the message dialog after 3 seconds
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            JOptionPane.getRootFrame().dispose();

                        }
                    });
                }

            }).start();
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
                    captureScreenAndSaveToFile();
                    // Show message when screenshot is taken
                    JOptionPane screenshotNotification = new JOptionPane("Screenshot taken!", JOptionPane.INFORMATION_MESSAGE);
                    JDialog screenshotDialog = screenshotNotification.createDialog("Screenshot");
                    screenshotDialog.setModal(false);
                    screenshotDialog.setVisible(true);

                    // Create a Timer to hide the message after 2 seconds
                    Timer hideTimer = new Timer(2000, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            screenshotDialog.setVisible(false);
                            screenshotDialog.dispose();
                        }
                    });
                    hideTimer.setRepeats(false); // Only run once
                    hideTimer.start();
                } catch (AWTException | IOException ex) {
                    ex.printStackTrace();
                    System.out.println("Screenshot is taken");
                }
            }
        });
        timer1.start();
    }

    static void captureScreenAndSaveToFile() throws AWTException, IOException {
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage screenshot = new Robot().createScreenCapture(screenRect);



        // Choose the file location to save the screenshot
        String directory = "F:\\Autoscreenshot1"; // Replace with your desired directory
        File dir = new File(directory);
        if (!dir.exists()) {
            boolean created = dir.mkdir();
            if (!created) {
                System.out.println("Failed to create the directory");
                return;
            }
        }

        // Compute the timestamp based on the total elapsed time
        long currentTime = System.currentTimeMillis();
        totalElapsedTime += currentTime - lastCaptureTime;
        lastCaptureTime = currentTime;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String timestamp = dateFormat.format(new Date(totalElapsedTime));
        String fileName = "screenshot_" + timestamp + ".png";
        File file = new File(directory, fileName);

        // Save the screenshot to the specified file
        ImageIO.write(screenshot, "png", file);
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
        TimeCounter counter = new TimeCounter();
        counter.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        counter.setSize(400, 200);
        counter.setVisible(true);
    }
    }

