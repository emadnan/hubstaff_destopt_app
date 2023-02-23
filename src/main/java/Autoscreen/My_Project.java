package Autoscreen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class My_Project extends JFrame implements ActionListener {
    public static final long serialVersionUID = 1L;
    private final int Interval =10 * 60 * 1000; // 10 minutes in milliseconds
    private final int TOTAL_SCREENSHOTS = 3;
    private int screenshotsTaken = 0;
    private Timer timer;
    private JLabel timeLabel;
    private JButton playButton;
    private int secondsCount = 0;
    private Random random = new Random();

    public My_Project(){
        super("Time Counter");
        timeLabel=new JLabel("00:00:00");
        timeLabel.setFont(new Font("Arila",Font.BOLD,40));
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }

    public static void main(String[] args) {

    }


    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
