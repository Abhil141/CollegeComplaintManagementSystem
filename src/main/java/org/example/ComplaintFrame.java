package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class ComplaintFrame {
    private JFrame frame;
    private JComboBox<String> categoryBox;
    private JTextArea descriptionArea;
    private JButton submitButton, trackButton;
    private String userId; // User ID passed from LoginFrame

    public ComplaintFrame(String userId) {
        this.userId = userId;

        frame = new JFrame("Complaint Registration");
        frame.setSize(600, 400);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        frame.add(panel);
        setupComponents(panel);

        frame.setVisible(true);
    }

    private void setupComponents(JPanel panel) {
        panel.setLayout(null);

        JLabel titleLabel = new JLabel("Register Complaint", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setBounds(0, 20, 600, 30);
        panel.add(titleLabel);

        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setBounds(50, 80, 100, 25);
        panel.add(categoryLabel);

        String[] categories = {"Maintenance", "IT Support", "Facilities", "Other"};
        categoryBox = new JComboBox<>(categories);
        categoryBox.setBounds(150, 80, 200, 30);
        panel.add(categoryBox);

        JLabel descriptionLabel = new JLabel("Description:");
        descriptionLabel.setBounds(50, 130, 100, 25);
        panel.add(descriptionLabel);

        descriptionArea = new JTextArea();
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        scrollPane.setBounds(150, 130, 400, 100);
        panel.add(scrollPane);

        submitButton = new JButton("Submit");
        submitButton.setBounds(150, 270, 100, 30);
        submitButton.addActionListener(e -> registerComplaint());
        panel.add(submitButton);

        trackButton = new JButton("Track Complaints");
        trackButton.setBounds(300, 270, 150, 30);
        trackButton.addActionListener(e -> openTrackingFrame());
        panel.add(trackButton);


    }

    private void registerComplaint() {
        String category = (String) categoryBox.getSelectedItem();
        String description = descriptionArea.getText();

        String url = "jdbc:mysql://localhost:3306/oom";
        String dbUser = "root";
        String dbPassword = "Abhil@141";

        String query = "INSERT INTO complaints (user_id, category, description, status, created_at, updated_at) VALUES (?, ?, ?, 'Pending', NOW(), NOW())";

        try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword)) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, userId);
            preparedStatement.setString(2, category);
            preparedStatement.setString(3, description);
            preparedStatement.executeUpdate();
            JOptionPane.showMessageDialog(frame, "Complaint registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error registering complaint.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openTrackingFrame() {
        frame.dispose(); // Close current frame
        new ComplaintTrackingFrame(userId, frame); // Open tracking frame and pass current frame
    }
}
