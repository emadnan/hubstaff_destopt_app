package Autoscreen;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class JsonPlaceholderApp extends JFrame {
    private JTable table;

    public JsonPlaceholderApp() {
        setTitle("JSONPlaceholder App");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        String[] columns = {"ID", "Title", "Body"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        try {
            URL url = new URL("https://jsonplaceholder.typicode.com/posts");
            Scanner scanner = new Scanner(url.openStream());
            String response = scanner.useDelimiter("\\Z").next();
            scanner.close();

            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                int id = jsonObject.getInt("id");
                String title = jsonObject.getString("title");
                String body = jsonObject.getString("body");
                Object[] row = {id, title, body};
                model.addRow(row);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JButton addButton = new JButton("Add Post");
        addButton.addActionListener(e -> post());
        add(addButton, BorderLayout.SOUTH);

        setVisible(true);
    }

    public void post() {
        try {
            URL url = new URL("https://jsonplaceholder.typicode.com/posts");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");

            JSONObject json = new JSONObject();
            json.put("title", "New Post");
            json.put("body", "This is a new post.");
            json.put("userId", 1);

            String requestBody = json.toString();
            con.setDoOutput(true);
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_CREATED) {
                System.out.println("Post created successfully.");
            } else {
                System.out.println("Error creating post. Response code: " + responseCode);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new JsonPlaceholderApp();
    }
}
