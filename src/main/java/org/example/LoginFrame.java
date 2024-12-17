package org.example;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginFrame {
    JFrame frame;
    JButton submit;
    JPasswordField passwordField;
    JTextField usernameField;

    public LoginFrame() {
        // Set JTattoo theme
        setLookAndFeel();

        // Initialize frame properties
        frame = new JFrame("College Complaint Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);

        JPanel loginPanel = new JPanel();
        frame.add(loginPanel);
        setupComponents(loginPanel);

        frame.setVisible(true);
    }

    private void setupComponents(JPanel panel) {
        panel.setLayout(null);
        panel.setBackground(Color.WHITE);

        // Title Label
        JLabel titleLabel = new JLabel("Login", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setBounds(0, 20, 600, 50);
        titleLabel.setForeground(new Color(50, 50, 50));
        panel.add(titleLabel);

        // Username Label and TextField
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        userLabel.setBounds(150, 100, 100, 25);
        panel.add(userLabel);

        usernameField = new JTextField();
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setBounds(260, 100, 180, 30);
        panel.add(usernameField);

        // Password Label and PasswordField
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        passwordLabel.setBounds(150, 150, 100, 25);
        panel.add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setBounds(260, 150, 180, 30);
        panel.add(passwordField);

        // Submit Button
        submit = new JButton("Login");
        submit.setFont(new Font("Segoe UI", Font.BOLD, 16));
        submit.setForeground(Color.WHITE);
        submit.setBackground(new Color(0, 102, 204));
        submit.setBounds(220, 220, 140, 40);
        submit.setFocusable(false);
        submit.setBorder(BorderFactory.createEmptyBorder());
        submit.addActionListener(e -> verify());
        panel.add(submit);
    }

    private void verify() {
        String username = usernameField.getText();
        String pass = new String(passwordField.getPassword());

        // SQL Server connection details
        String url = "jdbc:sqlserver://localhost:1433;databaseName=ComplaintDB;user=root;password=Abhil@141;trustServerCertificate=true;";
        String user = "root"; // SQL Server authentication username
        String password = "Abhil@141"; // SQL Server authentication password
        String query = "SELECT * FROM users WHERE user_id=? AND password=?;";

        try {
            // Load SQL Server JDBC driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver not found");
        }

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, pass);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                String userId = rs.getString("user_id"); // Fetch user_id
                frame.dispose();
                if ("student".equals(role) || "faculty".equals(role) || "staff".equals(role)) {
                    new ComplaintFrame(userId); // Pass userId to ComplaintFrame
                } else {
                    new AdminFrame(); // Admin dashboard for admin role
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid Username or Password", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();  // Print the detailed error to console
            JOptionPane.showMessageDialog(frame, "Database connection failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    private void setLookAndFeel() {
        try {
            // Set JTattoo theme
            UIManager.setLookAndFeel("com.jtattoo.plaf.acryl.AcrylLookAndFeel");
        } catch (Exception e) {
            System.out.println("JTattoo theme not applied");
        }
    }
}
