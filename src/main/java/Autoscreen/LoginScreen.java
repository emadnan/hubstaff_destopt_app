package Autoscreen;
import javax.swing.*;
import java.awt.event.*;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginScreen extends JFrame implements ActionListener {
    
    JLabel userLabel, passLabel, statusLabel;
    JTextField userText;
    JPasswordField passText;
    JButton loginButton;
    
    public LoginScreen() {
        userLabel = new JLabel("Email:");
        passLabel = new JLabel("Password:");
        statusLabel = new JLabel("");
        userText = new JTextField();
        passText = new JPasswordField();
        loginButton = new JButton("Login");
        
        userLabel.setBounds(10, 10, 80, 25);
        passLabel.setBounds(10, 40, 80, 25);
        userText.setBounds(100, 10, 160, 25);
        passText.setBounds(100, 40, 160, 25);
        loginButton.setBounds(10, 80, 80, 25);
        statusLabel.setBounds(100, 80, 160, 25);
        
        add(userLabel);
        add(passLabel);
        add(userText);
        add(passText);
        add(loginButton);
        add(statusLabel);
        
        loginButton.addActionListener(this);
        
        setTitle("Login Screen");
        setSize(300, 150);
        setLayout(null);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    public void actionPerformed(ActionEvent e) {
        String username = userText.getText();
        String password = new String(passText.getPassword());
        
        // Call the API to authenticate the user
        boolean authenticated = authenticateUser(username, password);
        
        if (authenticated) {
            // Redirect to main application screen
            // JOptionPane.showMessageDialog(this, "Login successful!");
            // System.out.println("Login successful!");
            // this.setVisible(false);
            // TODO: Add code to navigate to main application screen
            // hide the login screen and show the main application screen
            this.setVisible(false);
            MainScreen2 mainScreen2 = new MainScreen2();
            mainScreen2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainScreen2.setSize(400, 700);
            mainScreen2.setVisible(true);
            System.out.println("Login successful!");
        } else {
            // Display an error message and clear the password field
            statusLabel.setText("Invalid username or password");
            passText.setText("");
        }
    }
    
    public boolean authenticateUser(String username, String password) {
        // TODO: Call the API to authenticate the user
        // Return true if authentication is successful, false otherwise
        try {
            URL url = new URL("http://127.0.0.1:8000/api/login");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setDoOutput(true);
            String requestBody = "{\"email\":\""+username+"\",\"password\":\""+password+"\"}";
            OutputStream os = con.getOutputStream();
            os.write(requestBody.getBytes());
            os.flush();
            os.close();
            System.out.println("HTTP Status: " + con.getResponseCode());
            if(con.getResponseCode() == 200) {
                return true;
            }
            else {
                return false;
            }
        }
        catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }
    
    public static void main(String[] args) {
        new LoginScreen();
    }
}
